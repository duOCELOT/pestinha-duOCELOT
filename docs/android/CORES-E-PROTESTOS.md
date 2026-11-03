# Cores, ícone e protestos — Android 0.22

[Projeto](../../README.md) · [Guia](GUIA-DO-APK.md) · [Versões](versions/README.md)

## Escolher uma cor

1. Abra **Personalização → Cores e acessórios**.
2. Escolha **Cor da moldura** ou **Cor dos olhos**.
3. Confira a amostra e a prévia do personagem.
4. Ajuste os controles ou escreva `#RRGGBB`; toque em **Salvar** para aplicar.

| Controle | Intervalo e efeito |
| --- | --- |
| HEX | Seis dígitos com `#`; aceita letras maiúsculas ou minúsculas |
| Matiz | 0–360°, para escolher o tom |
| Saturação | 0–100%, do cinza à cor saturada |
| Luminosidade HSL | 0–100%, do preto ao branco, passando pela cor |
| R, G, B | 0–255 em cada canal de luz |
| C, M, Y | 0–100%; complementos dos canais RGB, sem preto (K) |

RGB aparece como o primeiro grupo aberto. Matiz/luminosidade e CMY podem ser abertos pelos cabeçalhos. Os controles são sincronizados: mudar um recalcula os demais. A luminosidade é a componente L do modelo HSL; CMY é uma conversão simples de RGB, sem perfil de impressão. As seções **+ / −** permitem recolher os grupos que você não usa.

**Copiar** envia a cor ao clipboard do aparelho. **Cancelar** não muda o visual salvo. A prévia só é aplicada ao pet ao salvar; moldura e olhos continuam independentes por personagem. O campo HEX inválido bloqueia o salvamento e informa o formato esperado.

## Aplicativos acompanhados

Em **Rotina → Pausas conscientes → Escolher aplicativos**, a seleção mostra **ícone e nome**, com busca pelo nome e contador de até 20 selecionados. Os identificadores de pacote ficam na configuração interna, sem aparecer como uma segunda linha na lista.

A busca não desfaz escolhas que ficaram fora do filtro. Só **Salvar seleção** confirma as mudanças; **Cancelar** preserva a seleção anterior. Apps já escolhidos que deixaram de estar disponíveis aparecem com ícone genérico e indicação de indisponibilidade, para você poder removê-los.

A lista é consultada no perfil Android atual. Esta mudança não concede Acesso ao uso nem ativa o acompanhamento: a autorização e a opção de recados continuam separadas.

## Novo ícone

O ícone do launcher usa uma cabeça verde com acabamento brilhante, rosto escuro e olhos claros. É adaptativo para as máscaras dos launchers e tem versão monocromática para ícones temáticos em Android compatível. A identidade do pacote continua a mesma; não é necessário desinstalar.

O launcher pode manter o desenho antigo em cache por algum tempo. A aparência temática depende do launcher e da configuração do Android.

## Cartazes, faixas e bandeiras

| Peça | Movimento e uso |
| --- | --- |
| Cartaz | Placa com haste e balanço suave |
| Faixa | Tecido apoiado, com ondulação discreta |
| Bandeira | Tecido ondulando preso a um mastro |

As peças usam arquivos vetoriais próprios (`protest_placard.xml`, `protest_banner.xml`, `protest_flag.xml`), texto do protesto e detalhes na cor da moldura do personagem. O tecido é renderizado uma vez e animado por uma malha; não há vídeo ou GIF em execução.

Para experimentar, mostre o pet e abra **Jogos e interações → Bandeiras e faixas • prévia**. A prévia é visual e não altera as necessidades ou a memória neural.

Nos protestos espontâneos, com **Travessuras automáticas** ativadas, o cérebro escolhe cartaz, faixa ou bandeira conforme a fase do protesto. A peça acompanha o pet e pode ser recolhida com um toque. Ela também desaparece após cerca de 19 segundos, ou pode ser substituída por outro aviso. O jogo recolhe a peça ao abrir.

A animação pausa com a tela apagada e respeita a opção global do Android de desativar animações. As peças ocupam uma pequena janela própria; não precisam ler a tela ou os apps que estão por baixo.
