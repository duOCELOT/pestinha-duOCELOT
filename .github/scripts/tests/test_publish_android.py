import importlib.util
from pathlib import Path
from unittest import TestCase
from unittest.mock import patch

spec = importlib.util.spec_from_file_location('publisher', Path(__file__).parents[1] / 'publish_android.py')
publisher = importlib.util.module_from_spec(spec)
spec.loader.exec_module(publisher)


class FilenameTest(TestCase):
    def setUp(self):
        self.data = {'tag': 'android-v0.22.0', 'version_name': '0.22.0-alpha', 'apk_sha256': 'a' * 64}
        self.name = 'Pestinha-Android-0.22.0.apk'
        self.sums = Path('/tmp/SHA256SUMS.txt')
        self.expected = {self.name: (100, 'a' * 64), 'SHA256SUMS.txt': (99, 'b' * 64)}
        self.apk = {'id': 10, 'name': 'Pestinha-Android.apk', 'size': 100,
                    'digest': 'sha256:' + 'a' * 64, 'state': 'uploaded'}
        self.final = {'id': 1, 'assets': [dict(self.apk, name=self.name),
                     {'name': 'SHA256SUMS.txt', 'size': 99, 'digest': 'sha256:' + 'b' * 64, 'state': 'uploaded'}]}

    def test_versioned_name_and_mismatched_version_are_checked(self):
        self.assertEqual(publisher.apk_name(self.data), self.name)
        with self.assertRaises(ValueError):
            publisher.apk_name(dict(self.data, asset_name='../other.apk'))
        with self.assertRaises(ValueError):
            publisher.apk_name(dict(self.data, version_name='0.21.0-alpha'))

    def test_renames_in_place_and_uploads_only_checksum(self):
        release = {'id': 1, 'assets': [self.apk], 'body': 'Download Pestinha-Android.apk'}
        with patch.object(publisher, 'api', return_value=self.final), patch.object(publisher.subprocess, 'run') as run, patch.object(publisher, 'command') as command:
            publisher.refresh_public_assets(release, self.data, Path('/tmp') / self.name, self.sums, self.expected)
            self.assertIn('releases/assets/10', run.call_args_list[0].args[0][4])
            self.assertIn(self.name, run.call_args_list[0].kwargs['input'])
            command.assert_called_once_with('release', 'upload', self.data['tag'], str(self.sums), '--repo', publisher.REPOSITORY, '--clobber')

    def test_already_migrated_release_is_idempotent(self):
        with patch.object(publisher, 'api', return_value=self.final), patch.object(publisher.subprocess, 'run') as run, patch.object(publisher, 'command') as command:
            publisher.refresh_public_assets(dict(self.final, body=self.name), self.data, Path('/tmp') / self.name, self.sums, self.expected)
            run.assert_not_called()
            command.assert_not_called()

    def test_wrong_hash_or_duplicate_apks_prevent_writes(self):
        for assets in [[dict(self.apk, digest='sha256:' + 'c' * 64)], [self.apk, dict(self.apk, name=self.name)]]:
            with patch.object(publisher.subprocess, 'run') as run, patch.object(publisher, 'command') as command:
                with self.assertRaises(ValueError):
                    publisher.refresh_public_assets({'id': 1, 'assets': assets}, self.data, Path('/tmp') / self.name, self.sums, self.expected)
                run.assert_not_called()
                command.assert_not_called()
