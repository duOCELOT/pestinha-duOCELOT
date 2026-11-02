"""Publish an unchanged, approved CI APK. Credentials are supplied by GitHub Actions."""
import argparse
import hashlib
import json
import os
from pathlib import Path
import shutil
import struct
import subprocess
import tempfile
import zipfile

CONFIG = Path('android-release.json')
REPOSITORY = 'duOCELOT/pestinha-duOCELOT'


def config():
    data = json.loads(CONFIG.read_text())
    if data['repository'] != REPOSITORY:
        raise ValueError('Unexpected publication repository')
    if os.environ.get('GITHUB_REPOSITORY', REPOSITORY) != REPOSITORY:
        raise ValueError('Publication is restricted to the intended repository')
    if not Path(data['notes']).is_file():
        raise ValueError('Release notes are missing')
    return data


def api(route):
    result = subprocess.run(['gh', 'api', f'repos/{REPOSITORY}/{route}'],
                            capture_output=True, text=True)
    if result.returncode:
        if 'HTTP 404' in result.stderr:
            return None
        raise RuntimeError(result.stderr.strip())
    return json.loads(result.stdout)


def check_build(data):
    run = api(f"actions/runs/{data['run_id']}")
    if not run or run['status'] != 'completed' or run['conclusion'] != 'success':
        raise ValueError('The approved Android build has not completed successfully')
    if run['head_sha'] != data['source_sha'] or run['head_branch'] != 'android':
        raise ValueError('Build commit/branch does not match the approved source')
    if run['name'] != 'Android APK (experimental)':
        raise ValueError('This is not the Android APK workflow')
    artifacts = api(f"actions/runs/{data['run_id']}/artifacts")['artifacts']
    selected = [a for a in artifacts if a['id'] == data['artifact_id']
                and a['name'] == data['artifact_name'] and not a['expired']]
    if len(selected) != 1:
        raise ValueError('Approved artifact is missing, expired or mismatched')
    output = os.environ.get('GITHUB_OUTPUT')
    if output:
        with open(output, 'a') as stream:
            stream.write(f"run_id={data['run_id']}\nartifact_name={data['artifact_name']}\n")
    print(f"Approved source: {data['source_sha']}; run {data['run_id']}")


def manifest_values(raw):
    strings, values, offset = [], {}, 8
    while offset < len(raw):
        kind, header, size = struct.unpack_from('<HHI', raw, offset)
        if size < header or not size or offset + size > len(raw):
            raise ValueError('Invalid binary manifest chunk')
        if kind == 1:
            count, _, flags, start, _ = struct.unpack_from('<IIIII', raw, offset + 8)
            indexes = struct.unpack_from('<' + 'I' * count, raw, offset + header)
            for index in indexes:
                pos = offset + start + index
                if flags & 0x100:
                    length = raw[pos]
                    pos += 1
                    if length & 0x80:
                        pos += 1
                    length = raw[pos]
                    pos += 1
                    if length & 0x80:
                        length = ((length & 0x7f) << 8) | raw[pos]
                        pos += 1
                    text = raw[pos:pos + length].decode('utf-8')
                else:
                    length = struct.unpack_from('<H', raw, pos)[0]
                    pos += 2
                    if length & 0x8000:
                        length = ((length & 0x7fff) << 16) | struct.unpack_from('<H', raw, pos)[0]
                        pos += 2
                    text = raw[pos:pos + length * 2].decode('utf-16le')
                strings.append(text)
        elif kind == 0x102:
            ext = offset + 16
            _, name, start, stride, count = struct.unpack_from('<IIHHH', raw, ext)
            tag = strings[name]
            for index in range(count):
                attr = ext + start + index * stride
                _, key, _ = struct.unpack_from('<III', raw, attr)
                value_type = raw[attr + 15]
                value = struct.unpack_from('<I', raw, attr + 16)[0]
                if value_type == 3:
                    value = strings[value]
                elif value_type == 0x12:
                    value = bool(value)
                if tag in ('manifest', 'uses-sdk') or (tag == 'application' and strings[key] == 'debuggable'):
                    values[f'{tag}.{strings[key]}'] = value
        offset += size
    return values


def verify_apk(data, folder):
    apks = list(Path(folder).rglob('*.apk'))
    if len(apks) != 1:
        raise ValueError('Expected exactly one APK in the approved artifact')
    apk = apks[0]
    checksum = hashlib.sha256(apk.read_bytes()).hexdigest()
    if checksum != data['apk_sha256']:
        raise ValueError('APK SHA-256 does not match the approved file')
    with zipfile.ZipFile(apk) as archive:
        if archive.testzip() is not None:
            raise ValueError('APK ZIP integrity failed')
        info = manifest_values(archive.read('AndroidManifest.xml'))
        abis = {name.split('/')[1] for name in archive.namelist()
                if name.startswith('lib/') and name.endswith('.so')}
    expected = {'manifest.package': 'org.pestinha.android',
                'manifest.versionCode': data['version_code'],
                'manifest.versionName': data['version_name'],
                'uses-sdk.minSdkVersion': 26,
                'application.debuggable': True}
    if any(info.get(key) != value for key, value in expected.items()):
        raise ValueError(f'APK identity/version mismatch: {info}')
    if abis != {'arm64-v8a', 'x86_64'}:
        raise ValueError(f'Unexpected architectures: {abis}')
    print(f"Verified APK: {data['version_name']}; {apk.stat().st_size} bytes; SHA-256 {checksum}")
    return apk


def command(*arguments):
    subprocess.run(['gh', *arguments], check=True)


def check_assets(release, expected):
    assets = {a['name']: a for a in release['assets']}
    for name, (size, digest) in expected.items():
        asset = assets.get(name)
        if not asset or asset['size'] != size or asset['state'] != 'uploaded':
            raise ValueError(f'Release asset missing or incomplete: {name}')
        if asset.get('digest') and asset['digest'] != 'sha256:' + digest:
            raise ValueError(f'Release asset hash mismatch: {name}')


def publish(data, folder):
    check_build(data)
    apk = verify_apk(data, folder)
    with tempfile.TemporaryDirectory(prefix='pestinha-release-') as tmp:
        staged = Path(tmp) / 'Pestinha-Android.apk'
        shutil.copyfile(apk, staged)
        sums = Path(tmp) / 'SHA256SUMS.txt'
        sums.write_text(data['apk_sha256'] + '  Pestinha-Android.apk\n')
        expected = {p.name: (p.stat().st_size, hashlib.sha256(p.read_bytes()).hexdigest())
                    for p in (staged, sums)}
        matches = [r for r in api('releases?per_page=100') if r['tag_name'] == data['tag']]
        if len(matches) > 1:
            raise ValueError('Ambiguous duplicate Release drafts; review them before publishing')
        release = matches[0] if matches else None
        if release and not release['draft']:
            check_assets(release, expected)
            print('Release already public with the expected assets; leaving it unchanged.')
            return
        if release:
            if release['target_commitish'] != data['source_sha'] or data['source_sha'] not in release['body']:
                raise ValueError('Existing draft does not match the approved source')
        else:
            command('release', 'create', data['tag'], '--repo', REPOSITORY,
                    '--target', data['source_sha'], '--draft', '--title', data['title'],
                    '--notes-file', data['notes'])
        matches = [r for r in api('releases?per_page=100') if r['tag_name'] == data['tag']]
        if len(matches) != 1:
            raise ValueError('Expected exactly one Release draft after creation')
        route = f"releases/{matches[0]['id']}"
        command('release', 'edit', data['tag'], '--repo', REPOSITORY,
                '--title', data['title'], '--notes-file', data['notes'])
        command('release', 'upload', data['tag'], str(staged), str(sums),
                '--repo', REPOSITORY, '--clobber')
        release = api(route)
        check_assets(release, expected)
        command('release', 'edit', data['tag'], '--repo', REPOSITORY,
                '--draft=false', '--prerelease=false', '--latest')
        release = api(route)
        if release['draft'] or release['prerelease']:
            raise ValueError('Release did not become publicly available')
        reference = api(f"git/ref/tags/{data['tag']}")['object']
        if reference['type'] == 'tag':
            reference = api(f"git/tags/{reference['sha']}")['object']
        if reference['type'] != 'commit' or reference['sha'] != data['source_sha']:
            raise ValueError('Release tag points at an unexpected source')
        print(release['html_url'])


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('--check-config', action='store_true')
    parser.add_argument('--verify-only', metavar='APK_FOLDER')
    parser.add_argument('--publish', metavar='APK_FOLDER')
    args = parser.parse_args()
    data = config()
    if args.check_config:
        check_build(data)
    elif args.verify_only:
        verify_apk(data, args.verify_only)
    elif args.publish:
        publish(data, args.publish)
    else:
        parser.error('Choose --check-config, --verify-only or --publish')
