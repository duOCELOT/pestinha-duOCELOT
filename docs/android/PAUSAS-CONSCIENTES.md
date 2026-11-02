# Pausas conscientes: tempo de uso e recados do pet

**Disponível no APK 0.7 experimental.** A habilidade usa estatísticas fornecidas
pelo Android para comentar o tempo em aplicativos que você selecionar. Ela vem
**desligada por padrão** e exige sua autorização de **Acesso ao uso**.

## Ativar

1. Abra **Pausas conscientes** na tela principal ou em **Assistente → Uso de aplicativos e recados do pet**.
2. Toque em **Autorizar Acesso ao uso no Android** e conceda acesso ao Pestinha.
3. Volte ao app e toque em **Escolher aplicativos**. Selecione até 20 apps deste perfil.
4. Configure o limite em minutos e o intervalo entre recados.
5. Escolha **Tempo nesta sessão** ou **Tempo acumulado hoje**.
6. Ative **Receber recados automáticos do pet** e deixe o pet solto.

O limite inicial é 20 minutos, com intervalo mínimo inicial de 15 minutos entre
recados. O limite aceita 5 a 180 minutos; o intervalo, 5 a 120. Esse intervalo é
global: não surge um recado para cada aplicativo ao mesmo tempo.

## Sessão ou total diário

| Modo | Significado | Exemplo |
| --- | --- | --- |
| Sessão | Tempo estimado contínuo no aplicativo, desde a entrada ou a meia-noite | “Você já está há cerca de 20 min no Instagram nesta sessão…” |
| Total de hoje | Soma dos períodos de uso do aplicativo no dia atual | “Hoje você já somou cerca de 50 min no Instagram…” |

Trocar de aplicativo ou bloquear a tela interrompe a sessão. Pequenas trocas de
atividades dentro do mesmo app podem ser tratadas como continuidade. O total do
dia usa o histórico que o Android disponibiliza, inclusive períodos anteriores
a você abrir o painel ou ativar os avisos.

Ao abrir o painel do Pestinha, ele próprio passa a ocupar o primeiro plano;
por isso o painel pode não indicar mais o Instagram como app atual. Use
**Pedir resumo de hoje ao pet** para consultar os totais dos apps escolhidos.

## Recados com a personalidade escolhida

| Personagem | Tom de exemplo |
| --- | --- |
| Bryan | “Você já está há cerca de 20 min no Instagram nesta sessão. Que tal dar uma pausa e brincar um pouco comigo?” |
| Jorge | “Essa rolagem está rendendo? Bora investir numa pausa.” |
| Mimir | “Hmpf. Você já está há cerca de 20 min no Instagram nesta sessão. Pelos galhos de Yggdrasil, já está bom, não?” |

O nome do app e o tempo são preenchidos a partir da leitura. Os recados aparecem
junto do pet e em **Recados do pet**, com personagem e horário. Esse histórico
fica limitado aos 40 recados mais recentes, incluindo os resumos pedidos.

Ainda não é conversa livre com LLM. São comentários programados no tom do
personagem, usando os dados medidos. O pet não bloqueia nem fecha aplicativos.

## Quando ele acompanha

- Avisos automáticos exigem a opção ativa, Acesso ao uso autorizado e pet solto.
- O monitor consulta em intervalos de cerca de 30 segundos, com a tela ligada e desbloqueada.
- Guardar o pet, bloquear/apagar a tela ou encerrar o serviço pausa os avisos automáticos.
- **Atualizar medições agora** é uma consulta manual que depende da autorização, mesmo se os avisos estiverem desligados.
- **Silenciar os recados por 30 minutos** suspende comentários; **Retomar recados** remove esse silêncio, mantendo o intervalo mínimo configurado.

## Precisão e limitações

Os tempos são **estimativas dos eventos do Android**, não um cronômetro exato.
A fabricante, a disponibilidade do histórico, a troca de atividades, tela
dividida e picture-in-picture podem alterar os resultados. O acompanhamento
se limita ao perfil de usuário do Android em que o Pestinha está instalado.

O app identifica aplicativos por pacote, não páginas dentro deles. Instagram
aberto no navegador conta como navegador se ele estiver selecionado; o Pestinha
não lê endereços visitados ou conteúdo da página. Navegadores e apps diferentes
precisam ser escolhidos separadamente.

O painel mostra o horário da última leitura e indica quando a fotografia é de
outro dia. Volte a atualizar para obter uma leitura recente. Ausência de eventos
pode produzir valores zerados ou impedir uma leitura útil.

## Privacidade e controle

O Android concede uma permissão ampla de estatísticas de uso. A implementação
usa eventos temporariamente para identificar transições, mas persiste somente
a seleção, os totais dos apps escolhidos, uma fotografia da leitura, os recados
e as configurações. Não salva a sequência bruta de eventos dos demais apps.

Não há leitura de mensagens, senhas, posts, tela, câmera ou microfone. O recurso
não usa acessibilidade. Os dados não são enviados aos feeds ou à internet e
não são acrescentados ao backup JSON do cérebro dos personagens.

Para parar, desligue **Receber recados automáticos** ou revogue Acesso ao uso no
Android. **Desativar e apagar dados locais** pede confirmação, interrompe o
monitor e limpa seleção, medições e recados. Isso não apaga o histórico que o
próprio Android mantém nem a memória neural dos pets. Uma consulta manual nova
poderá voltar a mostrar os dados que o sistema disponibilizar.

## Referências técnicas

- [UsageStatsManager — Android Developers](https://developer.android.com/reference/android/app/usage/UsageStatsManager)
- [UsageEvents.Event — Android Developers](https://developer.android.com/reference/android/app/usage/UsageEvents.Event)
- [`UsageLedger`](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/app/src/main/java/org/pestinha/android/UsageLedger.java): cálculo de sessões e totais.
- [`UsageRepository`](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/app/src/main/java/org/pestinha/android/UsageRepository.java): autorização, leitura, seleção, histórico e comentários.
- [`UsageMonitor`](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/app/src/main/java/org/pestinha/android/UsageMonitor.java): ciclo de vida junto ao serviço do pet.

[Guia do APK](GUIA-DO-APK.md) · [Fontes RSS](FONTES-RSS.md)
