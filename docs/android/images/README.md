# Prévias da interface Android 0.13

As imagens são renderizações de **Views Android reais**, executadas por Robolectric
com gráficos nativos. **Não são capturas de um telefone nem imagens de uma sessão pessoal.**
Dados exibidos nos testes de memória são exemplos de teste, não medições de um usuário.

Todas as imagens abaixo foram extraídas dos logs da
[compilação validada 0.13](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41541064297).

| Arquivo | Conteúdo | Teste de origem |
| --- | --- | --- |
| [inicio.png](inicio.png) | Tela principal com Bryan e navegação atual | `DesignTest` |
| [menu-rapido.png](menu-rapido.png) | Menu de segurar com Fixar no lugar e Arkanoid | `DesignTest` |
| [mimir.png](mimir.png) | Personagem azul-gelo e elmo viking | `DesignTest` |
| [memoria.png](memoria.png) | Painel de Mimir com valores de exemplo | `DashboardTest` |
| [radar.png](radar.png) | Seleção diária por cartões de assuntos | `InterestsDesignTest` |
| [arkanoid.png](arkanoid.png) | Campo, controles e o PetView original como plataforma | `ArkanoidOverlayTest` |

O PNG do Arkanoid tem canal alfa: suas áreas vazias são transparentes. O teste
compõe o campo e o PetView real em um único arquivo para documentar as duas janelas.
No telefone, o app que estiver aberto aparece por baixo. A cor de fundo do visualizador
do GitHub não faz parte do jogo.

O aspecto varia com densidade, tamanho da tela, fontes, permissões, estado do pet e Android.
Interface e documentação seguem a [licença do projeto](../../../LICENSE).
