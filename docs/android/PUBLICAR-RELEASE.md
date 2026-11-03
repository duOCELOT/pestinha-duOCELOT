# Publicar e manter o download Android

[Projeto](../../README.md) · [Catálogo de versões](versions/README.md)

## Um APK por versão

Cada nova Release publica um arquivo **`Pestinha-Android-<versão>.apk`** e seu `SHA256SUMS.txt`. Exemplo: `Pestinha-Android-0.22.0.apk`. Não existe uma segunda cópia com nome fixo.

O botão **Baixar última versão** mantém seu texto, mas aponta para a URL exata do arquivo atual. O nome externo não interfere na atualização: o Android verifica o pacote, a assinatura e o versionCode do APK.

Para divulgar um endereço estável, use:

```text
https://github.com/duOCELOT/pestinha-duOCELOT#readme
https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest
```

O primeiro abre a apresentação com o botão; o segundo abre a página da Release atual. Um link direto de arquivo versionado continua ligado àquela versão, mesmo depois de sair outra.

## Fluxo automatizado

O [workflow de publicação](../../.github/workflows/publish-android.yml) recupera um APK de uma compilação aprovada, verifica sua identidade e integridade e o publica sem recompilar ou reassinar. A [configuração](../../android-release.json) fixa tag, versão, commit, run, artefato e SHA-256; `asset_name` pode explicitar o nome, mas deve corresponder à tag e à versão interna.

1. Atualize versionName e aumente versionCode no aplicativo.
2. Envie o código para a branch `android` e aguarde o workflow Android passar.
3. Atualize `android-release.json` com os dados reais da compilação e suas notas.
4. O push inicia o publisher, que valida o APK e cria a Release em rascunho.
5. O script usa o nome versionado, gera o checksum com esse nome e publica somente após verificar os anexos.
6. Depois de confirmar a Release, atualize o destino do botão no README, os guias atuais e o catálogo de versões nas branches `main` e `android`.

O script tem testes de nome, versão, idempotência e preservação do APK. O job usa o token temporário do GitHub Actions; nenhum segredo de publicação é versionado.

A atualização dos links da documentação faz parte da publicação: eles não descobrem sozinhos o nome do próximo arquivo. O destino deve ser `https://github.com/duOCELOT/pestinha-duOCELOT/releases/download/<tag>/<nome-versionado>.apk`.

## Integridade e arquivos existentes

O publisher confere pacote, versionCode/versionName, Android mínimo, arquiteturas e SHA-256. Uma Release existente correta é preservada. APK divergente ou dois APKs na mesma Release bloqueiam a operação, sem apagar arquivos automaticamente.

A migração da 0.22 renomeou o asset existente por API, mantendo seu ID, bytes, tamanho e SHA-256. Apenas o arquivo de checksums mudou para mencionar o novo nome. Não houve recompilação, nova assinatura nem segunda cópia do APK. As versões 0.21 e anteriores mantêm os nomes originais para preservar seus links.

Renomear um asset muda sua URL: links antigos diretos para `Pestinha-Android.apk` na 0.22 deixam de funcionar. Por isso o endereço permanente de divulgação é a página do projeto ou da Release atual, e não um caminho `/latest/download/` com nome fixo.

## Verificações locais

```sh
python -m unittest discover -s .github/scripts/tests -v
python .github/scripts/publish_android.py --verify-only pasta-com-o-apk
```

No arquivo de configuração, não reutilize hash ou commit de outra compilação. Se o artefato expirar, obtenha uma nova compilação validada, sem tratar um binário de origem desconhecida como o APK aprovado.

## Registro de cada versão

Crie `docs/android/versions/<versão>/README.md` e `release.json`, com tag, commit, requisitos, tamanho, hash e URLs reais dos assets. Preserve as prévias identificadas em `images/`, com o run que as produziu.

Atualize `versions/README.md`, `CHANGELOG.md`, README principal e guias pertinentes. O texto do botão permanece **Baixar última versão**; o destino muda e a versão anterior entra na lista recolhível do README. As linhas históricas continuam apontando para os arquivos específicos de cada Release. Os APKs ficam nas Releases, sem cópias no Git.

## Assinatura e atualização

As versões atuais usam a assinatura pública de desenvolvimento fixa desde 0.2. Mudar o nome do arquivo não altera essa assinatura. Não substitua silenciosamente o conteúdo de um APK já publicado: alterações no aplicativo exigem nova versão, versionCode e tag.

Para distribuição de produção, planeje a assinatura e a migração de dados separadamente. [Instalação e backup](GUIA-DO-APK.md).
