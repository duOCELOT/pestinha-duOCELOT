# Tarefas e acompanhamento com o pet — APK 0.9

## Me conte o que precisa fazer

Com o pet solto e a tela desbloqueada, ele pode perguntar uma vez por dia:
**“O que você tem para fazer hoje?”**. O convite aparece após cerca de 20 segundos;
se a tela estiver bloqueada ou o pet ainda estiver iniciando, ele tenta depois.
Toque no balão para escrever uma tarefa, escolher a data/hora de entrega e
selecionar a frequência das perguntas pelo andamento. O título aceita até 160 caracteres.

Você também pode abrir **Tarefas e prazos** na tela principal ou em **Assistente**.
A opção **Pedir ao pet para perguntar agora** mostra o convite na hora.
Desmarque **O pet pode me perguntar uma vez por dia** para desligar o convite espontâneo.
Isso não cancela tarefas nem seus avisos.

## Como ele acompanha

- **A fazer → Em andamento:** toque em **Já comecei** ou **Comecei** na notificação.
- **Concluída:** toque em **Concluí a tarefa** ou **Concluir** na notificação. Os avisos são cancelados.
- Perguntas periódicas: a cada 1, 2, 4 ou 24 horas, restritas a 08h–22h no fuso local.
- **Só avisos do prazo:** dispensa as perguntas periódicas; mantém avisos do prazo e de atraso.
- Um aviso cerca de **uma hora antes**, se a tarefa foi cadastrada com mais de uma hora de antecedência.
- Um aviso **no prazo** e, caso continue pendente, um aviso de atraso por dia, em período diurno.
- **Adiar só o aviso por 30 min** silencia o próximo aviso sem mudar a entrega.
- **Editar tarefa / mudar prazo** altera a data, o título ou o intervalo e invalida avisos antigos.
- **Apagar tarefa** pede confirmação e cancela o acompanhamento.

Os horários de uma hora antes e do prazo são os escolhidos para a entrega;
podem cair à noite. Uma entrega vencida não provoca uma sequência de perguntas antigas:
o app avisa do prazo vencido e agenda o próximo acompanhamento.
O painel mostra o tempo restante/atrasado e as tarefas concluídas ficam consultáveis
até você apagá-las. Há limite de 50 tarefas armazenadas.

A tarefa registra o personagem que a recebeu. O acompanhamento identifica esse pet;
o convite e o formulário continuam acessíveis mesmo se outro personagem for escolhido.
As tarefas são do usuário, não são aprendizagem ou memória neural dos pets.
O pet não verifica arquivos, tela, agenda ou outros aplicativos para decidir se você terminou:
**somente você marca o progresso**. Não há interpretação por LLM.

## Avisos e persistência

As notificações funcionam com o pet guardado e não precisam de internet.
Com o pet solto e a tela desbloqueada, o aviso também pode aparecer junto dele;
toque para abrir o acompanhamento. O Android pode atrasar alarmes em economia de bateria.
A autorização de horário preciso em **Assistente** continua opcional.

Autorize notificações na tela de tarefas para receber os avisos de fundo.
Você pode cadastrar tarefas sem a permissão, mas será avisado de que elas apenas ficam salvas.
Reiniciar o aparelho rearma as tarefas. Depois de forçar a parada pelo Android,
reabra o Pestinha para retomar. Limpar dados ou desinstalar apaga as tarefas.
O backup neural JSON não inclui tarefas, prazos nem configurações de avisos.

## Implementação e validação

Preferências privadas `tasks`, arquivo lógico JSON com ID, personagem, título,
prazo, estado, frequência, próxima pergunta e token de versão. Um alarme por tarefa
é encaminhado ao receptor interno; broadcasts e ações de notificações com tokens
antigos não modificam tarefas editadas, concluídas ou apagadas. As notificações
usam um identificador por tarefa para substituir o aviso anterior.

Os testes cobrem persistência, estados explícitos, edição de prazo, duplicatas,
avisos antigos, adiamento sem mudar o prazo, exclusão, janela diurna e convite diário.

[Guia das telas](GUIA-DO-APK.md) · [Documentação Android](README.md)
