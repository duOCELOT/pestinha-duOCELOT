# Prévias da interface Android

As imagens são renderizações de **Views Android reais**, executadas por Robolectric
com gráficos nativos. **Não são capturas de um telefone nem imagens de uma sessão pessoal.**
Dados exibidos nos testes de memória são exemplos de teste, não medições de um usuário.

As imagens originais foram extraídas dos logs da
[compilação validada 0.13](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41541064297).

| Arquivo | Conteúdo | Teste de origem |
| --- | --- | --- |
| [inicio.png](inicio.png) | Tela principal 0.16 com escolha do pet junto da prévia | `DesignTest` |
| [menu-rapido.png](menu-rapido.png) | Menu de segurar com Fixar no lugar e Arkanoid | `DesignTest` |
| [mimir.png](mimir.png) | Mimir 0.16 com elmo, gola e medalhão | `DesignTest` |
| [memoria.png](memoria.png) | Painel de Mimir com valores de exemplo | `DashboardTest` |
| [radar.png](radar.png) | Seleção diária por cartões de assuntos | `InterestsDesignTest` |
| [arkanoid.png](arkanoid.png) | Campo, controles e o PetView original como plataforma | `ArkanoidOverlayTest` |

O PNG do Arkanoid tem canal alfa: suas áreas vazias são transparentes. O teste
compõe o campo e o PetView real em um único arquivo para documentar as duas janelas.
No telefone, o app que estiver aberto aparece por baixo. A cor de fundo do visualizador
do GitHub não faz parte do jogo.

O aspecto varia com densidade, tamanho da tela, fontes, permissões, estado do pet e Android.
Interface e documentação seguem a [licença do projeto](../../../LICENSE).

## Atualização 0.16

`inicio.png`, `mimir.png`, `bryan.png`, `luna.png` e `presidente.png` vêm da [compilação validada 0.16](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41645282506), via `DesignTest`. Mostram o desenho Canvas real, com transparência, gradientes e acessórios em camadas. `lula.png` permanece como registro da versão 0.15.

## Atualização 0.17

`inicio.png`, `bryan.png`, `mimir.png`, `luna.png` e `presidente.png` foram atualizadas a partir da [compilação validada 0.17](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41647993545). A arte detalhada é derivada das referências fornecidas pelo mantenedor via geração de imagens; olhos e cores são renderizados pelo código Android. São testes de Views reais, não capturas de telefone.
