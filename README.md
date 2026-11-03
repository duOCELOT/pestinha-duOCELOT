<div align="center">

# Pestinha Android

**Um pequeno companheiro de tela, com personalidade, memória e suas próprias travessuras.**

Pets sobre os aplicativos · Cuidados · Tarefas · Notícias · Quebra-blocos transparente

[**Baixar APK**](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk) · [Guia de uso](docs/android/GUIA-DO-APK.md) · [Versões](docs/android/versions/README.md)

**0.19 experimental** · Android 8.0 ou superior · ARM64 / x86_64

</div>

---

## Conheça o projeto

O Pestinha vive em uma janela transparente sobre os seus aplicativos. Você pode arrastá-lo, fazer carinho, brincar, acompanhar suas necessidades e escolher um personagem com memória própria. Ele também reúne ferramentas para a rotina: tarefas com prazo, lembretes de água, foco, notícias RSS e pausas conscientes.

É um aplicativo Android independente: **não precisa de Termux, Ubuntu ou VNC**.

**Nesta página:** [Instalar](#instalar-e-atualizar) · [Telas](#telas-e-funções) · [Personagens](#personagens-e-personalização) · [Gestos](#como-interagir) · [Versões](#arquivos-e-versões) · [Guias](#documentação) · [Próximos passos](#próximos-passos)

## Instalar e atualizar

### [↓ Baixar Pestinha-Android.apk](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk)

| Requisito | Disponibilidade |
| --- | --- |
| Android | 8.0 / API 26 ou superior |
| Arquiteturas | ARM64 e x86_64; 32 bits ainda em avaliação |
| Distribuição | APK experimental de desenvolvimento, com assinatura fixa desde 0.2 |
| Download | Arquivo `.apk` direto, sem ZIP ou login |

1. Baixe o APK e abra no celular. Se solicitado, permita a instalação para o navegador ou gerenciador usado.
2. Abra **Pestinha** e autorize **Aparecer sobre outros apps**.
3. Escolha um personagem no cartão superior e toque em **Mostrar pet**.
4. Autorize notificações para receber os avisos e acessar os controles.

**Já tem o app?** Instale sobre a versão atual para manter os dados. Desinstalar ou limpar os dados apaga os registros locais. Antes de qualquer remoção, consulte [memória e backup](docs/android/GUIA-DO-APK.md#salvar-exportar-e-restaurar). A versão 0.1 pode exigir [migração de assinatura](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/README.md#instalações-antigas-01).

[Release atual, hash e código correspondente](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest) · [Problemas comuns](docs/android/GUIA-DO-APK.md#10-problemas-comuns)

## Telas e funções

<table>
<tr>
<td align="center" width="33%"><img src="docs/android/versions/0.19/images/inicio.png" width="230" alt="Tela inicial da versão 0.19, com seletor do personagem junto da prévia"><br><b>Início · 0.19</b><br>Escolha o pet, acompanhe seu estado e acesse as funções.</td>
<td align="center" width="33%"><img src="docs/android/versions/0.19/images/menu-rapido.png" width="230" alt="Menu rápido de ícones aberto ao segurar o pet"><br><b>Menu rápido · 0.19</b><br>Interações, posição fixa e acesso ao Quebra-blocos.</td>
<td align="center" width="33%"><img src="docs/android/versions/0.13/images/radar.png" width="230" alt="Seleção de interesses do dia em cartões"><br><b>Meu radar · prévia 0.13</b><br>Assuntos do dia e fontes de notícias.</td>
</tr>
<tr>
<td align="center"><img src="docs/android/versions/0.13/images/memoria.png" width="230" alt="Painel de memória e necessidades, com valores de exemplo"><br><b>Memória · prévia 0.13</b><br>Gráficos, estado do pet, aprendizado e backup.</td>
<td align="center"><img src="docs/android/versions/0.19/images/quebra-blocos.png" width="230" alt="Quebra-blocos transparente com Mimir como plataforma"><br><b>Quebra-blocos · 0.19</b><br>O próprio pet vira a plataforma sobre o app aberto.</td>
<td align="center"><img src="docs/android/versions/0.17/images/presidente.png" width="230" alt="Presidente com arte detalhada, barba branca e faixa decorativa"><br><b>Arte dos pets · 0.17</b><br>Olhos animados e cores personalizáveis.</td>
</tr>
</table>

As imagens são **prévias de Views Android renderizadas em testes**, com dados de exemplo; não são capturas de um telefone. Cada imagem indica sua versão de origem. O Quebra-blocos tem fundo transparente: no aparelho, o aplicativo aberto fica visível por baixo. [Origem e catálogo das imagens](docs/android/images/README.md).

| Função | O que você pode fazer | Guia |
| --- | --- | --- |
| Pet flutuante | Arrastar, brincar, fazer carinho, usar inclinação e fixar a posição | [Gestos e movimento](docs/android/GUIA-DO-APK.md#3-pet-flutuante-e-gestos) |
| Cuidados e visual | Dar lanche, cuidar do mal-estar fictício, aliviar frio/calor simulados e escolher cores e itens | [Cuidados e guarda-roupa](docs/android/CUIDADOS.md) |
| Tarefas | Cadastrar entregas, definir prazo, informar andamento e receber lembretes | [Tarefas e prazos](docs/android/TAREFAS.md) |
| Água e foco | Configurar lembretes de água, timers, pausas e avisos locais | [Assistente](docs/android/GUIA-DO-APK.md#7-tela-assistente) |
| Notícias | Escolher interesses e feeds RSS; ler manchetes com comentários em texto | [Meu radar](docs/android/MEU-RADAR.md) |
| Uso de aplicativos | Acompanhar o tempo estimado nos apps escolhidos, com autorização opcional | [Pausas conscientes](docs/android/PAUSAS-CONSCIENTES.md) |
| Memória | Consultar gráficos, histórico e aprendizado; exportar ou restaurar a memória de cada pet | [Memória e cérebro](docs/android/GUIA-DO-APK.md#8-tela-memória-e-cérebro) |
| Quebra-blocos | Jogar com o pet como plataforma, três vidas, fases e recordes por personagem | [Como jogar](docs/android/QUEBRA-BLOCOS.md) |

## Personagens e personalização

<p align="center">
<img src="docs/android/versions/0.17/images/bryan.png" width="155" alt="Bryan">
<img src="docs/android/versions/0.17/images/mimir.png" width="155" alt="Mimir de elmo viking">
<img src="docs/android/versions/0.17/images/luna.png" width="155" alt="Luna com cabelo e laço">
<img src="docs/android/versions/0.17/images/presidente.png" width="155" alt="Presidente">
</p>

| Personagem | Personalidade e identidade |
| --- | --- |
| **Bryan** | Curioso e brincalhão; o pequeno caos original |
| **Jorge** | Coach fictício de sátira, com traje dourado e gravata |
| **Mimir** | Sábio, resmungão e inteligente, com referências nórdicas e elmo viking |
| **Luna** | Curiosa e afetuosa, com cabelo e laço |
| **Presidente** | Bem-humorado, de fala popular e cheio de histórias |

Cada personagem tem memória e temperamento próprios. **Recolha o pet antes de trocar** pelo seletor no topo. As cores da moldura e dos olhos podem ser ajustadas separadamente.

Na 0.17, o visual original usa arte detalhada adaptada das referências. Trocar um acessório usa o corpo básico detalhado com o item desenhado pelo app; todos os acessórios em camadas de igual acabamento ainda estão no roadmap. [Como funciona a arte e seus limites](docs/android/ARTE-HD.md).

## Navegação por categorias

A tela inicial usa uma lista vertical de categorias com **+ / −**: Personalização, Rotina, Notícias, Memória, Jogos e interações e Configurações. O estado aberto ou recolhido fica salvo no aparelho. Mostrar pet e Recolher pet continuam juntos no topo.

Cuidados, Assistente e Radar também dividem opções em seções recolhíveis. O atalho de personalização abre as cores e acessórios diretamente; gerenciar fontes abre a seção de RSS.

## Como interagir

| Gesto | Ação |
| --- | --- |
| Toque | Cutucar e provocar uma reação |
| Dois toques | Fazer carinho |
| Arrastar | Levar o pet para outra posição |
| Segurar | Abrir os pequenos ícones do menu rápido |
| Menu → Fixar no lugar | Desativar o movimento automático; arrastar continua permitido |

Notícias têm ações separadas: **Fechar** dispensa o balão; **Ler notícia** abre o cartão da matéria. Alguns aplicativos e telas protegidas do sistema podem ocultar o pet.

## Arquivos e versões

**[Catálogo de versões →](docs/android/versions/README.md)**

Cada versão publicada tem uma pasta própria com suas novidades, links fixos de APK e código, hash e metadados. As imagens históricas ficam na pasta da versão que as produziu. Os APKs ficam nas **Releases**, sem duplicar binários no repositório.

- **Instalar agora:** [APK mais recente](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk).
- **Consultar uma versão específica:** [pastas de versões](docs/android/versions/README.md).
- **Comparar mudanças:** [changelog](CHANGELOG.md).
- **Consultar etapas anteriores à 0.13:** [histórico de desenvolvimento](docs/android/versions/HISTORICO.md), sem APK público separado.

## Documentação

| Para quem | Onde começar |
| --- | --- |
| Quero instalar e usar | [Guia completo de telas, gestos e permissões](docs/android/GUIA-DO-APK.md) |
| Quero explorar uma função | [Índice dos guias](docs/android/README.md) |
| Quero divulgar o projeto | [Links para compartilhar](docs/android/DIVULGACAO.md) |
| Quero compilar ou contribuir | [Desenvolvimento Android](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/README.md) · [Referência técnica](docs/android/REFERENCIA-TECNICA.md) |
| Quero publicar uma versão | [Procedimento de Release](docs/android/PUBLICAR-RELEASE.md) |

O código do aplicativo fica na branch **[android](https://github.com/duOCELOT/pestinha-duOCELOT/tree/android)**. A **main** reúne a apresentação e os guias públicos. O projeto de desktop tem [documentação própria](README-DESKTOP.md).

## Privacidade e limites

Memórias e registros ficam no aparelho. RSS usa internet quando ativado; o Acesso ao uso é opcional. O app não lê o conteúdo da tela nem os compromissos da agenda. Os cuidados e o clima são simulados.

A personalidade combina regras, rede neural e textos escritos. **Não há LLM instalado, conversa livre ou voz.** O APK é experimental e usa assinatura pública de desenvolvimento; não é uma versão de produção para a Play Store. [Permissões e detalhes](docs/android/GUIA-DO-APK.md#9-permissões-e-telas-externas).

## Próximos passos

- [x] Integrar arte detalhada dos cinco personagens, com transparência e olhos animados.
- [ ] Refinar a fidelidade das artes às referências aprovadas.
- [ ] Separar todos os acessórios em camadas intercambiáveis de igual acabamento.
- [ ] Validar memória, bateria e fluidez das novas artes em aparelhos reais.
- [ ] Criar um jogo de estilingue transparente com o pet (planejado, ainda não disponível).
- [ ] Ampliar interações e personalidade dos pets.
- [ ] Avaliar modelos locais leves para funções específicas, com ativação opcional.
- [ ] Avaliar **Android de 32 bits (armeabi-v7a)**: suporte das dependências e testes em aparelho compatível; sem disponibilidade prometida.

## Créditos e licença

Adaptação Android por **duOCELOT**, a partir de [mille-okws/pestinha](https://github.com/mille-okws/pestinha). Código sob [GPL-3.0-or-later](LICENSE). [Origem das imagens e prévias](docs/android/images/README.md).
