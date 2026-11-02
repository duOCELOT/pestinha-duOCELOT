# Pestinha Android 0.11 (experimental)

Adaptação independente de mille-okws/pestinha. Interface Android em Java/Canvas;
rede neural e memória originais em Python, integradas por Chaquopy. Sem Termux,
Ubuntu, Qt ou VNC no aparelho. Código de desktop preservado.


## Material descritivo do APK

[Apresentação](../docs/android/README.md) ·
[Guia das telas e funções](../docs/android/GUIA-DO-APK.md) ·
[Referência técnica](../docs/android/REFERENCIA-TECNICA.md)

O guia inclui imagens renderizadas, o mapa das telas e a descrição das funções
implementadas, com instruções de uso, memória, permissões e limitações.


## RSS e pausas conscientes (0.7)

No Assistente, **Escolher fontes oficiais por categoria** oferece 14 feeds de
notícias, economia, bolsa, cripto, tecnologia e ciência. A seleção aceita até dez
fontes ativas e só consulta ao tocar Atualizar. Endereços verificados com HTTPS
200 e RSS com itens em 02/11/2026. [Lista e referências dos editores](../docs/android/FONTES-RSS.md).

**Pausas conscientes** acompanha apps escolhidos, com autorização especial de
Acesso ao uso (`PACKAGE_USAGE_STATS`) e opção desligada por padrão. Pode comentar
o tempo da sessão atual ou o total de hoje, usando o tom do pet selecionado.
O histórico de recados é local e limitado a 40 registros, sem LLM. O monitor
consulta a cada cerca de 30 segundos, somente com o pet solto e tela desbloqueada.
Você escolhe até 20 apps, limite, intervalo e silêncio temporário, e pode
desativar/apagar os dados locais. Não usa acessibilidade nem lê conteúdo.
[Guia e limitações das medições](../docs/android/PAUSAS-CONSCIENTES.md).

Estatísticas, recados e seleção do acompanhamento ficam nas preferências `usage`,
separadas de feeds e memória neural; não entram no backup JSON do cérebro.

## Mimir e personagens (0.6)

Em **Personagem e aparência**, escolha Bryan, Jorge ou Mimir. Guarde o pet e
aguarde o salvamento antes de trocar; depois use Soltar. A seleção fica salva,
a prévia muda na hora, e cada personagem mantém memória, rede, episódios e
imagem de meme separados. A validação de backup usa a semente do JSON do
personagem selecionado, sem confundir Mimir com Jorge ou Bryan.

Mimir mantém o corpo e as expressões originais, com paleta azul-gelo, olhos
claros e elmo viking de metal, detalhes de latão e chifres estilizados. O elmo
é desenhado no Canvas do Android e tem espaço para o balanço sem cortar as
pontas. Acessórios não substituem nem cobrem as expressões dos olhos.

Personalidade solicitada: **sábio, resmungão, inteligente no tom e com referências
nórdicas**. Isso é implementado por temperamento neural e bilhetes/cartazes
próprios, com humor seco e alusões a Odin, Yggdrasil, Thor e às Nornas. Não há
LLM nem conversa livre: “inteligente” descreve a personalidade escrita, não um
novo modelo de linguagem. A definição está em `pets/mimir.json` (semente 104729).
No desktop, o JSON usa o mecanismo original de pets; o elmo novo é renderizado
pela adaptação Android, sem alterar o desenho Qt original.

## Interface e menu rápido (0.5)

O rosto Android é uma portagem nativa de `pestinha/body/desenho_rosto.py`:
corpo 120 × 90, moldura verde `#63e56f`, tela `#171e19`, olhos `#3ee877`,
brilho `#c8ffd8` e os recortes de pálpebra do original. Os formatos por emoção
vêm de `EYE_SHAPES` no núcleo Python, com transição suave na interface. Jorge
usa as cores e acessórios de seu JSON. Não depende de screenshots ou sprites.

**Segurar o pet abre os ícones rápidos**, sem sair para o app. Carinho, bolinha,
dormir/acordar, notebook, memória, assistente, painel e guardar estão disponíveis.
O botão Painel abre a tela completa. Toque fora ou no X para fechar. O pet fica
parado enquanto o menu está aberto; rotação, tela apagada ou guardar fecham o
menu. Não captura a tela inteira e não bloqueia os toques fora da janela.

A tela principal usa a paleta do pet, cartões para Assistente e Memória e seções
recolhidas para brincadeiras, personagem, movimento e privacidade. Controles dos
outros painéis seguem a mesma paleta, com margens para as barras do Android.

## Funções

- Bryan, Jorge e Mimir, com sementes, temperamentos e memórias separados.
- Pet transparente sobre apps, gravidade, quique, arraste e arremesso por toque.
- Toque para cutucar, toque duplo para carinho, segurar para abrir os ícones rápidos.
- Bolinha arrastável, chute por proximidade, notebook para trabalhar junto.
- Bilhetes e memes de uma imagem escolhida pelo usuário; dispensar ensina uma
  reação negativa, deixar o bilhete na tela ensina uma reação positiva.
- Rede LIF, emoções, energia, estimulação e carência originais.
- Condicionamento original de PRESS, aprendizado por carinho e habituação.
- Memória episódica/preferências originais e consolidação durante o sono.
- Protestos e tristeza com cartazes; cantinho adaptado com saída automática ou
  por atividade do motor FUGIR; carinho encerra o cantinho.
- Fogos nativos na transição para feliz; ajustes de tamanho e velocidade.
- Contextos manuais livre, trabalho, jogo, música e social. Modo de teste tem
  tempos mais curtos, é temporário e deve ser usado só para experimentar.
- Monitor ao vivo e última fotografia salva do cérebro; exportação/restauração
  de backup JSON por personagem. O backup não inclui a imagem personalizada.

As travessuras automáticas são opcionais. A seleção aprende entre bilhete,
atravessar, nada e meme (se houver imagem). O contexto é escolhido pelo usuário,
não inferido de outros apps. Não há leitura de tela, captura ou acessibilidade.
O acompanhamento opcional de uso é um módulo separado, autorizado pelo usuário,
que consulta eventos do Android e gera recados sem entregar conteúdo ao cérebro. A rede decide as emoções; algumas ações solicitadas pelo
painel e os controles de movimento são adaptações explícitas para o Android.

## Limites em relação ao desktop

Não é paridade completa: não lê janelas/cursor/teclado globais nem pousa em
barras de outros apps. Não reproduz todo o controlador Qt do cantinho, as
revoluções com múltiplos objetos, as brincadeiras do notebook ou o monitor
espacial de cada neurônio. O cantinho Android usa os canais neurais originais,
mas não porta o controlador Qt. Os memes originais não vêm no APK: escolha uma
imagem sua. Não há pet sobre telas protegidas que bloqueiam overlays.

A animação e rede pausam com tela apagada; necessidades recuperam energia sem
wake-lock. O Android ainda pode encerrar o serviço. Não reinicia sozinho após
reboot; use Soltar o pet. Notificação tem botão Guardar.

## Física do celular (0.3)

Ative **Física por inclinação do celular** no painel. A translação usa o sensor
GRAVITY ou acelerômetro filtrado, não integração do giroscópio (que acumularia
erro). Giroscópio é opcional e acrescenta rotação visual ao corpo. Sensor ausente
não impede o uso do app. Amostragem normal SENSOR_DELAY_GAME, sem permissão
especial de alta frequência. Nenhum dado bruto é gravado ou transmitido.

Incline devagar: pet e bolinha aceleram na direção da gravidade na tela, com
atrito, teto de velocidade e colisões. Calibrar define a posição atual como
neutra para usar inclinação relativa; Restaurar gravidade natural retira o
zero. A calibração é temporária, zerada ao girar a orientação da tela. Arraste
tem prioridade; notebook/cantinho mantêm o pet parado. Em modo inclinação o pet
pode deslizar mesmo dormindo, sem acordar só por inclinar. Movimentos bruscos
podem alimentar o canal MOTION da rede, com intervalo mínimo entre eventos.

Os listeners param quando o modo é desativado, a tela apaga ou o serviço acaba.
A tela volta a 30 fps com física ativa para manter resposta uniforme. O giroscópio
não é usado para coletar deslocamentos ou atividades fora da interação com o pet.
Aumente a sensibilidade aos poucos; não é necessário sacudir forte o aparelho.

## Assistente prático (0.4)

Abra **Assistente • foco, lembretes, agenda e notícias** no painel.

- Foco de 25 minutos, pausa de 5 e foco personalizado (1 a 180 minutos).
  O timer é salvo e não depende do pet visível. Enquanto o pet está solto,
  foco coloca o notebook; o final aparece no pet e em uma notificação.
- Lembretes únicos com título, data e hora; lista para consultar e cancelar.
  São persistidos e reagendados no boot, mudança de horário e abertura do
  painel. Forçar parada, limpar dados ou desinstalar pode impedir os avisos;
  abra o app novamente para reagendar os pendentes após uma parada forçada.
- Notificações precisam estar permitidas. Sem acesso especial a alarmes exatos,
  usa o agendamento aproximado do Android, que pode atrasar em repouso. O botão
  **Autorizar horário preciso** abre a configuração opcional do sistema.
  Mesmo alarmes exatos podem ser silenciados por Não Perturbe/configuração do
  canal. Para alarmes do sistema, use o atalho do relógio.
- Agenda: preenche evento de uma hora e abre o calendário para revisão e
  salvamento. Não lê os compromissos e não pede READ_CALENDAR/WRITE_CALENDAR.
- Relógio: abre o app do sistema para confirmar um alarme ou timer; requer um
  aplicativo compatível instalado. Não usa confirmação automática/skip UI.
- RSS/Atom: até dez feeds HTTPS escolhidos por você, com biblioteca opcional de 14 fontes dos próprios veículos; nenhuma assinatura é ativada automaticamente.
  A atualização pode ser manual ou automática no radar opcional, com pet solto e tela desbloqueada. Cada resposta
  tem timeout, limite de 1,5 MB, até 30 itens, e redirecionamentos só para HTTPS.
  Não aceita credenciais na URL, DTDs ou links que executam ações no aparelho.
  Trechos são texto do próprio feed, não resumos por IA. Mostra fonte, data
  informada e horário da última consulta; a cópia salva pode ser lida offline.
  Pode trazer a manchete com o pet ou abrir a matéria no navegador por toque.

Não há LLM, busca geral na internet, leitura de agenda, envio de mensagens ou
execução de ordens contidas em notícias. Os pedidos de rede incluem o endereço
IP e os cabeçalhos normais ao servidor do feed, sem enviar memória do pet,
lembretes ou agenda. Remover um feed limpa a cópia de notícias salva. O radar opcional atualiza aproximadamente a cada hora enquanto o pet está solto e a tela desbloqueada. Timers, lista de feeds e cache ficam
nas preferências privadas do app; **não estão no backup JSON do cérebro**.
A atualização normal do APK conserva esses dados, pois usa a mesma assinatura
fixa de desenvolvimento desde 0.2.

## Memória

Abra **Memória e cérebro** no painel. Mostra energia, estimulação, carência,
humor, motores em Hz, pesos aprendidos, habituação, condicionamento, preferências
e últimos episódios. Pesos numéricos não são pensamentos, frases ou conversa.
Os arquivos ficam em `files/memory/bryan/`, `files/memory/jorge/` ou `files/memory/mimir/`, na área
privada do aplicativo. Salva a cada 15 s, ao apagar a tela e ao guardar.

Exportar grava um JSON no destino escolhido pelo seletor do Android; quando o
pet está ativo, a exportação primeiro salva a memória no mesmo worker do cérebro.
Restaurar exige pet guardado, formato e personagem compatíveis; prepara a cópia
antes de substituir e mantém rollback se a troca de pasta falhar. Apagar pede
confirmação e afeta somente os arquivos de memória do personagem selecionado.
A imagem personalizada não está incluída no backup de memória.

Desinstalar/limpar dados apaga a memória. As versões 0.1 usavam uma assinatura
de debug diferente a cada runner do GitHub. A partir de 0.2 há uma chave pública
**somente de desenvolvimento** no repositório para manter a assinatura estável.
Não use essa chave para distribuição comercial/Play Store. Uma atualização de
0.1 para 0.2 pode ser recusada pelo Android por assinatura incompatível.

Para preservar a memória da versão 0.1, que não tem exportação na interface,
é possível usar um computador com ADB e depuração USB autorizada, antes de
qualquer desinstalação (APK debug permite run-as):

```sh
adb exec-out run-as org.pestinha.android tar -C files -cf - memory > pestinha-memoria.tar
```

Depois de instalar a nova versão, abra o app, mantenha o pet guardado e restaure:

```sh
adb shell run-as org.pestinha.android mkdir -p files
adb shell -T run-as org.pestinha.android tar -C files -xf - < pestinha-memoria.tar
```

O Android continuará usando a área privada do app; o Termux não a acessa sem
ADB/root. Não é necessário root para o procedimento ADB no APK debug.

## Compilar e verificar

Java 17+, Gradle 8.13, SDK 35, Build Tools 35.0.0 e Python 3.11 no computador.
Defina `ANDROID_HOME` ou `sdk.dir` em `local.properties`. Na pasta `android/`:

```sh
gradle assembleDebug lintDebug verifyDebugPayload testDebugUnitTest
PYTHONPATH=..:app/src/main/python python -m unittest discover -s tests -v
```

`syncBrain` empacota só módulos headless originais; `syncPetAssets` copia os
JSONs de personagens e licença. `verifyDebugPayload` abre o APK e exige esses
arquivos e a aplicação Python antes do upload. Saída:
`app/build/outputs/apk/debug/app-debug.apk`. Android 8+; ARM64 e x86_64.

O workflow `.github/workflows/android.yml` faz testes do adaptador, compilação,
lint, testes RSS/Atom e agendamento em ambiente Android simulado (Robolectric), e verificação do conteúdo. A compilação não substitui testes no aparelho:
permissão, rotação, arraste/arremesso, imagens, exportar/restaurar, guardar/soltar,
tela apagada, revogação da permissão e manutenção de memória após atualização.

## Licença

GNU GPL-3.0-or-later, como o código original. Veja `../LICENSE`, incluída no APK.
A adaptação desenha as próprias formas e não empacota as imagens de `assets/`.


### Novidades Android 0.8

Lembretes recorrentes de água com intervalo e janela diária, registro manual de copos,
gráficos do tempo de uso e monitor de personalidade/memória separado por personagem.
As barras mostram dados reais da rede, necessidades e novos tempos acumulados do pet;
o temperamento inicial é identificado separadamente do aprendizado.
Consulte o [guia das telas](../docs/android/GUIA-DO-APK.md) para ativar e entender os contadores.


## Tarefas e prazos (0.9)

O pet pergunta o que você tem para fazer. Toque no convite, escreva a tarefa,
defina a entrega e escolha a frequência de acompanhamento. O painel permite
começar, concluir, adiar o aviso e mudar o prazo. Veja o [guia das tarefas](../docs/android/TAREFAS.md).


## Meu radar (0.10)

Seleção diária de interesses em cartões, RSS configurados por assunto e manchetes
comentadas pelo pet, com fonte e link para a matéria. Veja o [guia do radar](../docs/android/MEU-RADAR.md).


## Fixar o pet no lugar (0.11)

Segure o boneco e toque em **Fixar no lugar** no menu rápido.
Agora você pode arrastá-lo e soltá-lo: ele permanece nessa posição, sem cair,
passear, girar com a inclinação ou continuar o impulso de um arremesso.
Segure novamente e escolha **Soltar posição** para restaurar o movimento normal.
A opção e a posição são mantidas ao guardar/abrir o pet e valem para o personagem solto.

O rosto, as reações, os lembretes, as notícias e a interação continuam ativos.
A bolinha mantém sua própria física, inclusive inclinação quando habilitada.
A preferência de inclinação é preservada para quando você soltar a posição.
Se a orientação, o tamanho ou a área da tela mudar, a posição pode ser ajustada
para que o boneco continue dentro dos limites visíveis.
