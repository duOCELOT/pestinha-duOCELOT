# Pestinha Android 0.13 — experimental

**Baixe `Pestinha-Android.apk` nos Assets abaixo e abra no celular.**
Não precisa de ZIP, login no GitHub, Termux, Ubuntu ou VNC.

[Baixar este APK](https://github.com/duOCELOT/pestinha-duOCELOT/releases/download/android-v0.13.0/Pestinha-Android.apk) · [Guia de instalação e uso](https://github.com/duOCELOT/pestinha-duOCELOT/blob/main/docs/android/GUIA-DO-APK.md)

## Novidade desta versão

O Arkanoid agora tem **fundo totalmente transparente**, para jogar sobre o app aberto.
O próprio pet é a plataforma horizontal. Blocos, bola, placar e controles ficam visíveis;
as áreas vazias não escurecem o aplicativo de baixo.

## O que o APK já inclui

- Bryan, Jorge e Mimir, com aparência e memória independentes.
- Menu ao segurar, posição fixa, carinho, brinquedos e sensores opcionais.
- Arkanoid com três vidas, fases, reações e recorde por personagem.
- Tarefas com entrega e acompanhamento, água, foco e lembretes locais.
- Radar de interesses e RSS, com manchetes em balões e fontes visíveis.
- Tempo estimado nos apps escolhidos, opcional e com Acesso ao uso.
- Gráficos e backup da memória por personagem.

## Instalar ou atualizar

Android **8.0/API 26 ou superior**, sistema ARM64 ou x86_64. Não há suporte ARM de 32 bits neste APK.

1. Baixe e abra `Pestinha-Android.apk`.
2. Se o Android solicitar, permita instalar apps para o navegador/gerenciador que abriu o arquivo.
3. Abra Pestinha, autorize aparecer sobre outros apps e toque em **Soltar o pet**.
4. Segure o boneco para encontrar **Fixar no lugar** e **Arkanoid**.

Para atualizar, instale sobre a versão atual. As versões 0.2+ compartilham a assinatura
de desenvolvimento. Desinstalar ou limpar dados apaga os registros locais; exporte a
memória antes disso. O backup neural não inclui tarefas, configurações do assistente,
feeds, registros de uso/água nem recordes. Versões 0.1 podem ter assinatura incompatível.

Este é um **APK experimental de desenvolvimento** (`debuggable=true`), assinado com a
chave pública de desenvolvimento do repositório. Não é uma versão de produção/Play Store.
A memória, o jogo e os lembretes locais não precisam de internet. Notícias consultam apenas
as fontes escolhidas. Não há LLM, voz ou leitura de conteúdo da tela/agenda.

## Origem e integridade

- Versão no APK: `0.13.0-alpha`; versionCode **14**; pacote `org.pestinha.android`.
- [Compilação validada](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41541064297), com build, lint, testes e verificação do pacote aprovados.
- [Código-fonte correspondente](https://github.com/duOCELOT/pestinha-duOCELOT/tree/android-v0.13.0).
- Commit: `9a9d79a63946d7030bb7e4f2248e2b93084b3ff3`.
- O APK é o mesmo arquivo da compilação validada, sem reempacotar ou reassinar.
- SHA-256: `e39356425d1f6fc8439397044395f9357c977f1a41c8af94abad41f6b1cb85ba`.

`SHA256SUMS.txt` contém o hash do APK. No computador: `sha256sum -c SHA256SUMS.txt`.

[Novidades por versão](https://github.com/duOCELOT/pestinha-duOCELOT/blob/main/CHANGELOG.md) · [Projeto e guias](https://github.com/duOCELOT/pestinha-duOCELOT)

Adaptação independente por duOCELOT de [mille-okws/pestinha](https://github.com/mille-okws/pestinha), sob GPL-3.0-or-later.
