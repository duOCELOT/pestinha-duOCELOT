# Pestinha Android

**Seu pet na tela do celular — com personalidade, memória e um pouco de caos.**

Bryan, Jorge e Mimir ficam sobre os seus aplicativos. Você pode brincar, receber
lembretes, acompanhar tarefas e notícias ou jogar Arkanoid usando o próprio pet como plataforma.

[![Baixar APK Android](https://img.shields.io/badge/BAIXAR_APK-Android_8%2B-63e56f?style=for-the-badge&logo=android&logoColor=white)](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk)
[![Última versão](https://img.shields.io/github/v/release/duOCELOT/pestinha-duOCELOT?label=APK&color=63e56f)](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest)

**[Baixar o APK diretamente](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk)** ·
[Ver a Release e novidades](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest) ·
[Guia completo](docs/android/GUIA-DO-APK.md) · [Links para divulgar](docs/android/DIVULGACAO.md)

**Atual: 0.13 experimental.** O arquivo já é um `.apk`: não precisa extrair ZIP nem entrar no GitHub.
Android **8.0 ou superior**, ARM64 ou x86_64. Não precisa de Termux, Ubuntu ou VNC.

## Instalar e atualizar

1. No celular, toque em **Baixar o APK diretamente** e abra `Pestinha-Android.apk`.
2. Se solicitado, autorize a instalação de apps desconhecidos para o navegador ou gerenciador que abriu o arquivo.
3. Instale, abra **Pestinha** e autorize **Aparecer sobre outros apps**.
4. Escolha o personagem e toque em **Soltar o pet**.

Para atualizar, instale a versão nova **sobre a atual**, mantendo os dados.
As versões 0.2+ compartilham a assinatura de desenvolvimento. Antes de desinstalar
ou limpar dados, exporte a memória em **Memória e cérebro**: a desinstalação apaga os dados locais.
Versões 0.1 podem ter assinatura incompatível; veja a [migração e backup](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/README.md#memória).

O APK é experimental e assinado com uma chave pública **de desenvolvimento**;
ainda não é uma distribuição de produção/Play Store. A Release inclui
`SHA256SUMS.txt` para conferir a integridade do arquivo.

## Veja a interface

<p>
  <img src="docs/android/images/menu-rapido.png" width="230" alt="Menu ao segurar o pet, com Fixar no lugar e Arkanoid">
  <img src="docs/android/images/radar.png" width="230" alt="Meu radar: cartões para escolher assuntos e fontes de notícias">
  <img src="docs/android/images/arkanoid.png" width="230" alt="Arkanoid com blocos, placar e Mimir como plataforma; fundo transparente">
</p>

**Menu rápido · Interesses do dia · Arkanoid transparente.**
Prévias da interface real renderizadas em testes, com dados de exemplo; não são capturas de um aparelho.
No Arkanoid, as áreas vazias da imagem são transparentes: o app aberto aparece por baixo.
[Mais imagens e origem](docs/android/images/README.md).

## O que dá para fazer

| Função | Como funciona |
| --- | --- |
| **Pet sobre os apps** | Arraste, cutuque, faça carinho com dois toques e segure para abrir os atalhos. |
| **Três personagens** | Bryan, Jorge e Mimir, com aparência, temperamento e memória próprios. Mimir usa elmo viking e tem humor sábio e resmungão. |
| **Fixar no lugar** | Segure → Fixar no lugar. Ele fica onde você soltar, sem gravidade ou passeio. Use Soltar posição para voltar. |
| **Arkanoid** | Segure → Arkanoid. O pet vira a plataforma horizontal: três vidas, fases, reações e recorde por personagem, sobre um campo transparente. |
| **Tarefas e prazos** | Conte o que precisa fazer, escolha a entrega e receba perguntas de andamento. Você marca quando começou/concluiu ou muda o prazo. |
| **Água e foco** | Lembretes de água em horários/intervalos configuráveis, registro manual de copos, timers de foco e pausas. |
| **Meu radar** | Escolha assuntos em cartões na primeira abertura do dia. RSS oficiais são configurados e o pet pode comentar manchetes por balões. |
| **Pausas conscientes** | Com autorização de Acesso ao uso, acompanha apps escolhidos e comenta o tempo estimado da sessão ou do dia. Vem desligado. |
| **Memória e cérebro** | Painel por pet com necessidades, temperamento, motores, interações, aprendizado e contadores, além de exportar/restaurar memória. |
| **Brincadeiras e sensores** | Bolinha, notebook, bilhetes, imagem personalizada, protestos, cantinho e inclinação/giroscópio opcionais. |

### Conheça os pets

| Bryan | Jorge | Mimir |
| --- | --- | --- |
| Verde, curioso e brincalhão; o visual original. | Dourado e satírico, com gravata e papo de coach fictício. | Azul-gelo, elmo viking, inteligente, resmungão e com referências nórdicas. |

<img src="docs/android/images/mimir.png" width="220" alt="Mimir, com olhos azul-gelo e elmo viking">

Para trocar de personagem, guarde o pet e abra **Personagem e aparência**.
Trocar não apaga a memória dos demais.

## Privacidade e permissões

- A memória, as tarefas, os registros e as preferências ficam no aparelho. O backup neural não inclui todos os dados do assistente/jogo; veja o guia antes de migrar.
- **Sobreposição** permite desenhar o pet e o jogo sobre os apps; não é captura de tela.
- **Notificações** são usadas para o serviço e os lembretes. Horários precisos têm autorização opcional.
- **Acesso ao uso** é opcional, só para os apps que você escolher acompanhar. O recurso não lê mensagens, senhas ou conteúdo das telas.
- Notícias consultam os RSS escolhidos, manualmente ou pelo radar opcional, com pet solto e tela desbloqueada. Os veículos recebem a conexão HTTPS normal; não recebem sua memória, tarefas ou estatísticas de uso.
- Agenda e relógio abrem suas próprias telas para você revisar e salvar. O Pestinha não lê compromissos.

Os personagens usam a rede neural pulsada e a memória do projeto original.
**Não há LLM nem conversa livre:** falas são programadas e notícias usam manchetes/trechos dos veículos.
Os comentários atuais são em texto, sem leitura em voz alta. O app não precisa de internet para o pet, o jogo ou os lembretes locais.

## Guias

- [Instalação, telas, gestos e backup](docs/android/GUIA-DO-APK.md)
- [Arkanoid: controles, vidas, recordes e transparência](docs/android/ARKANOID.md)
- [Tarefas e acompanhamento](docs/android/TAREFAS.md)
- [Meu radar e interesses do dia](docs/android/MEU-RADAR.md)
- [Lista de 14 RSS oficiais](docs/android/FONTES-RSS.md)
- [Tempo nos apps e pausas conscientes](docs/android/PAUSAS-CONSCIENTES.md)
- [Arquitetura, dados e permissões](docs/android/REFERENCIA-TECNICA.md)
- [Novidades por versão](CHANGELOG.md)

## Dúvidas rápidas

**O celular aceita o APK?** Android 8+ com sistema ARM64 ou x86_64. Não há pacote ARM de 32 bits nesta versão.

**O pet sumiu ou parou?** Confira a permissão de sobreposição e as restrições de bateria do Pestinha nas configurações do Android. O pet pausa com a tela apagada; algumas telas do sistema ocultam sobreposições. [Soluções comuns](docs/android/GUIA-DO-APK.md#10-problemas-comuns).

**Posso usar o app de baixo enquanto jogo?** O fundo é transparente, mas a camada do jogo recebe os toques. Feche no × para usar o app de baixo; o pet volta à posição e ao movimento anteriores.

**Atualizar apaga a memória?** A instalação sobre uma versão compatível mantém os dados. Desinstalar ou limpar dados apaga. Exporte cada personagem antes disso.

**É igual ao desktop?** O cérebro original é reaproveitado, mas a interface e as funções são adaptadas ao Android. O pet não observa as janelas dos outros apps nem pousa em suas barras.

## Código, desenvolvimento e créditos

O app fica na **[branch android](https://github.com/duOCELOT/pestinha-duOCELOT/tree/android)**.
Java/Canvas + Python via Chaquopy. [Como compilar, testar e migrar versões antigas](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/README.md).
Os APKs em Releases são os arquivos validados pelo [workflow Android](https://github.com/duOCELOT/pestinha-duOCELOT/actions/workflows/android.yml).
Os testes automáticos não substituem a verificação em aparelhos reais; contribuições e relatos com versão do APK/Android ajudam a reproduzir um problema. [Pull requests](https://github.com/duOCELOT/pestinha-duOCELOT/pulls) são bem-vindos.

Adaptação independente por duOCELOT a partir de **[mille-okws/pestinha](https://github.com/mille-okws/pestinha)**.
[Documentação original de desktop](README-DESKTOP.md) · [GPL-3.0-or-later](LICENSE).
