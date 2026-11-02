# Meu radar — interesses do dia e manchetes com o pet

Disponível no APK **0.10 experimental**.

## A pergunta da primeira abertura

Ao abrir o app pela primeira vez no dia, aparece a caixa **“O que desperta sua curiosidade hoje?”**.
Os cartões são selecionáveis e mostram a fonte/idioma. Você pode escolher vários assuntos:

| Assunto | Fonte configurada |
| --- | --- |
| Notícias | Agência Brasil — últimas (português) |
| Mundo | BBC World (inglês) |
| Economia | Agência Brasil — economia (português) |
| Bolsa | InfoMoney (português) |
| Cripto | Livecoins (português) |
| Tecnologia | BBC Technology (inglês) |
| Ciência | NASA (inglês) |

**Salvar meu radar** ajusta as fontes oficiais aos temas escolhidos, uma por assunto.
As URLs personalizadas externas à biblioteca são preservadas. O limite continua sendo
dez feeds; se as fontes personalizadas excederem esse limite junto dos temas, o app
informa e não altera a seleção anterior. As fontes gerais podem misturar assuntos;
não há classificador por IA para separar cada matéria individualmente.

Os cartões, sozinhos, não fazem requisições nem alteram assinaturas. O botão de salvar
explica o uso da internet e traz a opção **Pet comenta as manchetes automaticamente**.
A opção aparece marcada na primeira configuração, mas só é aplicada ao salvar.
Sem seleção e confirmação, a instalação continua sem feeds/consultas automáticas.
Salvar sem temas desativa os comentários automáticos.

**Agora não**, voltar ou fechar a caixa adia a pergunta até o próximo dia e mantém as
escolhas anteriores. A seleção persiste: não expira à meia-noite. Desligue comentários
para pausar as notícias. A pergunta diária pode ser desligada em **Meu radar**.
Você pode mudar os assuntos a qualquer momento pela tela principal ou pelo Assistente.

## Quando o pet traz uma notícia

Com comentários automáticos ativados, pet solto e tela ligada/desbloqueada:

- O app atualiza os RSS aproximadamente a cada hora; falhas têm espera mínima de cinco minutos antes de nova tentativa.
- A primeira manchete pode aparecer após a primeira consulta concluída, respeitando outros avisos e interações.
- O padrão é um comentário a cada **20 minutos**, ajustável para 10, 20, 30 ou 60 minutos.
- A consulta e a apresentação são verificadas aproximadamente a cada 30 segundos; não são horários exatos.
- As fontes são intercaladas para variar os assuntos. Até 150 manchetes apresentadas são lembradas para evitar repetição.
- O radar evita abrir balões durante o menu rápido, arrasto ou escolha de interesses e aguarda após avisos do assistente/tarefas.
- Guardar o pet ou apagar a tela interrompe o monitor. Nenhum serviço de notícias separado é iniciado.

São **balões de texto**, com uma frase do personagem, fonte e manchete do editor.
Mimir usa seu tom resmungão; não altera o fato publicado. Não há leitura em voz alta,
tradução ou geração de resumo por LLM. Os trechos exibidos no cartão vêm do próprio feed.
Uma manchete em cache não é garantia de notícia recém-publicada; o cartão mostra a data
informada pelo veículo e a última consulta. Cache com mais de 24 horas não é apresentado
automaticamente, mas continua consultável na tela.

## Ler e controlar

Toque no balão para abrir o cartão da notícia em **Meu radar**. Ele mostra fonte,
data, trecho e botão **Ler matéria no site**. O navegador só é aberto quando você escolhe
esse botão. **Pet comenta esta manchete** permite pedir um comentário manual com o pet solto.
**Atualizar notícias agora** é uma consulta manual; funciona sem comentários automáticos.
**Ativar / pausar comentários do pet** controla o monitor automático.

O Assistente mantém os controles avançados de fontes oficiais, URLs personalizadas e
cache. Uma mudança feita ali também muda os feeds lidos pelo radar. As assinaturas
mostradas no radar refletem temas escolhidos; se você editar feeds manualmente, a lista
avançada do Assistente é a referência dos endereços ativos.

## Dados e implementação

Tudo usa as preferências privadas existentes `assistant`: temas, consentimento,
intervalo, data da pergunta, cache e até 150 identificadores de manchetes apresentadas.
Não há perfil baseado no uso do celular. As escolhas são explícitas. As consultas HTTPS
não enviam memória neural, tarefas, lembretes ou estatísticas de outros aplicativos;
os veículos recebem IP/cabeçalhos normais de uma consulta HTTP.
Os dados do radar não fazem parte do backup neural JSON dos pets.

`InterestsDialog` desenha os cartões nativos; `NewsRepository` configura as fontes,
coordena consultas únicas, intercala dados e aplica intervalo/deduplicação.
`NewsMonitor` acompanha o ciclo de vida do pet e `NewsActivity` apresenta as matérias.
Mudanças de seleção invalidam consultas pendentes e limpam o cache antigo.
As respostas respeitam os limites do parser RSS/Atom: HTTPS, tamanho máximo de 1,5 MB,
sem DTD ou execução de conteúdo. Há até 100 itens no cache do radar e 30 na lista inicial.

Os testes verificam seleção explícita, mapeamento para fontes oficiais, preservação
personalizada, limite de dez, pausas, deduplicação, intervalo, cache antigo e uma
prévia visual nativa dos cartões. A compilação não substitui a verificação no aparelho.

[Fontes RSS e referências](FONTES-RSS.md) · [Guia das telas](GUIA-DO-APK.md)
