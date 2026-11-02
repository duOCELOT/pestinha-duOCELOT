# Biblioteca de RSS dos próprios veículos

**Verificação: 2 de novembro de 2026.** A biblioteca contém 14 fontes. “Oficial” aqui significa que o feed é servido no domínio do próprio veículo/editor, em vez de ser uma cópia produzida por um agregador de terceiros.

Na consulta de verificação, os endereços abaixo responderam por HTTPS com status 200 e XML RSS com itens. A disponibilidade pode mudar; o app mostra erros durante a atualização. A data é uma verificação pontual, não monitoramento permanente dos servidores.

## Lista por assunto

| Categoria | Veículo | Idioma | RSS | Página do editor |
| --- | --- | --- | --- | --- |
| Notícias | Agência Brasil — últimas | pt-BR | [Adicionar este RSS](https://agenciabrasil.ebc.com.br/rss/ultimasnoticias/feed.xml) | [Site / referência do editor](https://agenciabrasil.ebc.com.br/feed/) |
| Notícias | BBC News Brasil | pt-BR | [Adicionar este RSS](https://feeds.bbci.co.uk/portuguese/rss.xml) | [Site / referência do editor](https://www.bbc.com/portuguese) |
| Notícias | g1 — notícias | pt-BR | [Adicionar este RSS](https://g1.globo.com/rss/g1/) | [Site / referência do editor](https://g1.globo.com/) |
| Mundo | BBC — mundo | en | [Adicionar este RSS](https://feeds.bbci.co.uk/news/world/rss.xml) | [Site / referência do editor](https://www.bbc.com/news) |
| Economia | Agência Brasil — economia | pt-BR | [Adicionar este RSS](https://agenciabrasil.ebc.com.br/rss/economia/feed.xml) | [Site / referência do editor](https://agenciabrasil.ebc.com.br/feed/) |
| Economia | g1 — economia | pt-BR | [Adicionar este RSS](https://g1.globo.com/rss/g1/economia/) | [Site / referência do editor](https://g1.globo.com/economia/) |
| Bolsa e mercados | InfoMoney | pt-BR | [Adicionar este RSS](https://www.infomoney.com.br/feed/) | [Site / referência do editor](https://www.infomoney.com.br/) |
| Bolsa e mercados | Money Times | pt-BR | [Adicionar este RSS](https://www.moneytimes.com.br/feed/) | [Site / referência do editor](https://www.moneytimes.com.br/) |
| Bolsa e mercados | Bloomberg Markets | en | [Adicionar este RSS](https://feeds.bloomberg.com/markets/news.rss) | [Site / referência do editor](https://www.bloomberg.com/markets) |
| Criptomoedas | CoinDesk | en | [Adicionar este RSS](https://www.coindesk.com/arc/outboundfeeds/rss/) | [Site / referência do editor](https://www.coindesk.com/coindesk-news/2021/09/17/coindesk-rss) |
| Criptomoedas | Cointelegraph | en | [Adicionar este RSS](https://cointelegraph.com/rss) | [Site / referência do editor](https://cointelegraph.com/) |
| Criptomoedas | Livecoins | pt-BR | [Adicionar este RSS](https://livecoins.com.br/feed/) | [Site / referência do editor](https://livecoins.com.br/) |
| Tecnologia | BBC — tecnologia | en | [Adicionar este RSS](https://feeds.bbci.co.uk/news/technology/rss.xml) | [Site / referência do editor](https://www.bbc.com/news/technology) |
| Ciência | NASA | en | [Adicionar este RSS](https://www.nasa.gov/feed/) | [Site / referência do editor](https://www.nasa.gov/) |

## Como escolher no app

1. Abra **Assistente → Notícias → Escolher fontes oficiais por categoria**.
2. Marque as fontes desejadas. O app permite até dez feeds ativos no total, incluindo URLs adicionadas manualmente.
3. Salve a seleção. Nenhuma fonte é assinada automaticamente na instalação.
4. Toque em **Atualizar notícias agora** para consultar os servidores.
5. Cada notícia mantém a fonte, o trecho disponibilizado e o link para a matéria.

A seleção preserva as URLs personalizadas que não pertencem à biblioteca. Remover fontes limpa o cache para evitar exibir material de uma seleção antiga. A lista possui fontes em português e inglês; o APK ainda não traduz nem resume notícias por LLM.

## Bolsa e cripto: o que estas fontes oferecem

InfoMoney, Money Times e Bloomberg Markets oferecem cobertura editorial de mercados, empresas e economia. CoinDesk, Cointelegraph e Livecoins cobrem criptomoedas. Alguns feeds gerais misturam assuntos. São notícias e trechos publicados pelos veículos, **não um serviço de cotações em tempo real ou execução de investimentos**.

Endereços antigos que retornaram erro, conteúdo que não era XML ou nenhum item não foram incluídos nesta seleção. O feed antigo em português do Cointelegraph não foi usado; a opção incluída é o feed em inglês que respondeu com itens.

## Dados e manutenção

O catálogo fica em [`feed-catalog.json`](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/app/src/main/assets/feed-catalog.json). Cada entrada tem nome, categoria, idioma, URL, referência do editor e data de verificação. Os testes verificam URLs HTTPS únicas e o pacote exige a presença do catálogo.

A leitura manual continua disponível. No APK 0.10, o radar opcional configura fontes por assunto e pode consultar automaticamente com o pet solto e a tela desbloqueada. A atualização não envia estatísticas de uso de apps, memória do pet ou lembretes aos veículos. O servidor recebe a conexão HTTP normal do leitor, incluindo IP e cabeçalhos.

[Guia do APK](GUIA-DO-APK.md) · [Acompanhamento de aplicativos](PAUSAS-CONSCIENTES.md)
