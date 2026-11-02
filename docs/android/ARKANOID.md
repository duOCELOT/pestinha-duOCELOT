# Pet Arkanoid — jogo sobre os outros apps (0.12)

## Abrir e jogar

1. Solte o pet normalmente, com a permissão de aparecer sobre os apps.
2. **Segure o boneco → Arkanoid**. Também existe o botão em **Brincar e interagir** no painel.
3. O próprio pet vai para a parte de baixo e vira a plataforma. Os blocos aparecem no topo.
4. Arraste o pet, ou o dedo pela área do jogo, para mover a plataforma **somente na horizontal**.
5. Solte o dedo para lançar a bola. Rebata com o corpo do pet para quebrar os blocos.

Não é uma barra genérica com uma imagem sobre ela: a janela do pet que já estava solto
é usada como plataforma, com a aparência de Bryan, Jorge ou Mimir.
O ângulo do rebote muda conforme o ponto em que a bola toca a plataforma.

## Regras e reações

- Três vidas por partida; a bola que passar por baixo gasta uma vida.
- Quatro linhas e sete colunas: 28 blocos por fase. Linhas superiores valem mais pontos.
- Quebrar todos os blocos encerra a fase. Toque para preparar a próxima, com bola mais rápida.
- Sem vidas, aparece **Fim de jogo**; toque para reiniciar com três vidas e placar zerado.
- O pet reage ao rebater, perder vida, vencer uma fase e terminar a partida.
- Há partículas ao quebrar blocos/vencer, rastro da bola e animação de impacto no pet.
- O recorde é local e separado por personagem. É salvo ao pontuar/fechar o jogo.

O cérebro registra ações reais da partida: início, rebatidas, blocos quebrados,
vidas perdidas e fases vencidas. Elas aparecem entre as interações do painel de memória.
O jogo fornece estímulos de brincadeira sem mudar o contexto manual escolhido pelo usuário
ou fingir que uma derrota foi uma rejeição/carinho. Iniciar a partida acorda o pet.
Não há LLM, rede, ranking online, anúncios ou sons nesta primeira versão.

## Pausar, sair e usar o celular

**Pausar / Continuar** e **× Fechar** ficam no topo do jogo.
A camada do jogo cobre a área dos aplicativos e recebe os toques enquanto a partida
está aberta. Para tocar no app que está por baixo, feche o jogo no ×. A navegação e
os controles do sistema continuam sob controle do Android; alguns apps do sistema
podem impedir ou esconder sobreposições.

Apagar a tela pausa e esconde o jogo. Ao voltar, ele continua pausado: toque para retomar.
Girar o aparelho reorganiza o campo e também pausa para não gastar uma vida durante a mudança.
A partida atual existe apenas em memória; fechar o jogo, guardar o pet ou encerrar o
processo termina essa sessão, preservando o recorde já salvo.

Ao fechar, o pet retorna à posição, tamanho e modo de movimento anteriores, inclusive
**Fixar no lugar** e a preferência de inclinação. Se a área disponível mudou, a posição
é ajustada aos limites visíveis. A bolinha-brinquedo fica escondida/pausada durante o
jogo e volta ao sair. A física do Arkanoid é independente da gravidade/giroscópio do pet.

Notícias, convites de tarefas e bilhetes não abrem balões sobre a partida.
O acompanhamento de notícias/uso pausa durante o jogo. Lembretes de tarefas, água
ou foco continuam podendo aparecer nas notificações do Android. O encerramento de
um timer de foco ainda é processado pelo cérebro, sem cobrir a partida com um balão.

## Arquitetura e verificação

`ArkanoidModel` é Java puro: estados READY, PLAYING, PAUSED, WON e OVER, rebotes,
vidas, blocos e placar. Passos internos pequenos evitam atravessar blocos em alta velocidade.
`ArkanoidOverlay` desenha campo, partículas, placar e controles nativos; o `PetService`
mantém o `PetView` original em cima do campo e restringe sua posição à plataforma.
O jogo usa a permissão de sobreposição existente e não captura nem lê a tela de outros apps.

Preferências `arcade.best_<personagem>` guardam recordes e não entram no backup neural.
Os contadores de interação do cérebro usam os arquivos e backup neural existentes.
Testes cobrem rebotes, bordas, perda de vidas, fases, pausa, mudança de dimensões,
controle horizontal, fechamento, recordes separados e restauração de posição/modo fixo.
Uma prévia visual nativa do campo e do pet é produzida nos testes de interface.
A compilação e os testes simulados não substituem experimentar a sobreposição no aparelho.

[Guia das telas](GUIA-DO-APK.md) · [Documentação Android](README.md)


### Campo transparente (0.13)

O Arkanoid agora deixa o fundo totalmente transparente: os aplicativos abertos
continuam visíveis por baixo, sem uma camada escura cobrindo a tela. Blocos, bola,
pet e controles continuam visíveis. Apenas textos/controles e os cartões temporários
de instrução ou pausa têm fundos locais para manter a leitura.
Os toques continuam controlando o jogo enquanto ele está aberto; use × para voltar ao app de baixo.
