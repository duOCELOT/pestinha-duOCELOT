# Publicar e manter o download Android

O canal público é [Releases](https://github.com/duOCELOT/pestinha-duOCELOT/releases),
com um arquivo de nome constante: **`Pestinha-Android.apk`**. Assim, este endereço
continua apontando para a Release marcada como mais recente:

```text
https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk
```

A Release 0.13 usa a tag `android-v0.13.0`, apontando para o commit que produziu
o APK. A branch `main` apresenta o projeto; o app é desenvolvido na `android`.
Atualizar somente documentação não exige publicar novamente o binário.

## Fluxo automatizado recomendado

O [workflow Publish Android Release](../../.github/workflows/publish-android.yml)
publica o APK de uma compilação já aprovada, sem recompilar ou reassinar.
A [configuração](../../android-release.json) fixa versão, commit, run, artifact e SHA-256.
O script confere pacote, versionCode/versionName, Android mínimo e arquiteturas,
verifica os anexos ainda em rascunho e só então torna a Release pública.
Uma Release já publicada com os mesmos anexos é preservada.

Para publicar outra versão, atualize `android-release.json` e suas notas na branch
`android`, após a compilação aprovada. O push da configuração inicia a publicação.
Também pode executar **Actions → Publish Android Release → Run workflow**, escolhendo `android`.
O job usa `GITHUB_TOKEN` temporário do próprio repositório, com permissões de conteúdos/escrita
para Releases e ações/leitura para recuperar o artefato. Nenhum token privado é versionado.

Validação local do arquivo, antes de enviar:

```sh
python .github/scripts/publish_android.py --verify-only pasta-com-o-apk
```

## Para as próximas versões

1. Atualize `versionName` e aumente `versionCode` em `android/app/build.gradle`.
2. Envie o código para `android` e aguarde o workflow Android concluir com sucesso.
3. Confira que o run corresponde ao commit desejado e que o job `apk` passou.
4. Baixe o artefato, confira o pacote/versão/arquiteturas e use o APK aprovado sem modificá-lo.
5. Copie como `Pestinha-Android.apk`; gere `SHA256SUMS.txt`.
6. Atualize README, changelog, guias e notas da nova Release.
7. Crie uma **Release em rascunho**, com tag nova apontando para o commit da compilação.
8. Anexe o APK e o arquivo de hash. Confira nomes, tamanhos e origem.
9. Publique e marque como mais recente para atualizar o link permanente.
10. Abra o link público e confira os assets/notas. Não substitua silenciosamente um APK já publicado: use versão e tag novas.

No GitHub, isso pode ser feito por **Releases → Draft a new release**.
As Releases públicas não exigem login para baixar assets e não têm a expiração dos artefatos de Actions.
O link permanece válido enquanto o repositório e a Release/asset estiverem disponíveis.

## Exemplo com GitHub CLI

Use o `gh` autenticado com sua conta autorizada a publicar no repositório.
Não coloque tokens, senhas ou chaves privadas nos comandos/documentação.

```sh
# Substitua pelos dados da nova compilação; não reutilize o tag da 0.13.
ANDROID_RUN_ID=123456789
ANDROID_SOURCE_SHA=COMMIT_COMPLETO_DA_COMPILACAO
ANDROID_RELEASE_TAG=android-v0.14.0

gh run view "$ANDROID_RUN_ID" --repo duOCELOT/pestinha-duOCELOT --json conclusion,headSha,headBranch
gh run download "$ANDROID_RUN_ID" --repo duOCELOT/pestinha-duOCELOT --name pestinha-android-debug --dir release-download

# Após conferir o APK e sua versão:
cp release-download/app-debug.apk Pestinha-Android.apk
sha256sum Pestinha-Android.apk > SHA256SUMS.txt
gh release create "$ANDROID_RELEASE_TAG" Pestinha-Android.apk SHA256SUMS.txt \
  --repo duOCELOT/pestinha-duOCELOT --target "$ANDROID_SOURCE_SHA" --draft \
  --title 'Pestinha Android 0.14 — experimental' --notes-file notas-da-release.md

# Confira os assets antes de publicar:
gh release view "$ANDROID_RELEASE_TAG" --repo duOCELOT/pestinha-duOCELOT --json tagName,isDraft,assets
gh release edit "$ANDROID_RELEASE_TAG" --repo duOCELOT/pestinha-duOCELOT --draft=false --latest
```

Se um artefato tiver expirado, gere uma nova compilação validada do código correspondente;
não trate um arquivo sem origem verificável como o mesmo APK aprovado.

## Assinatura e compatibilidade

As versões atuais são APKs de desenvolvimento com chave pública fixa desde 0.2.
Isso preserva atualizações compatíveis, mas não equivale a uma chave privada de produção.
Não publique uma nova assinatura como se fosse uma atualização compatível.
Para uma distribuição de produção, planeje a assinatura/migração e informe claramente
como os usuários preservam seus dados. Não inclua chaves privadas no repositório.

[Instalação e backup](GUIA-DO-APK.md) · [Novidades](../../CHANGELOG.md)
