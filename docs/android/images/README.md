# Telas e origem das imagens

[Projeto](../../../README.md) · [Guias](../README.md) · [Versões](../versions/README.md)

As prévias são renderizações de **Views Android reais em testes Robolectric**, com gráficos nativos. Não são capturas de telefone. Dados de memória e atividade são exemplos de teste. Densidade, fontes, permissões e estado do pet podem alterar o resultado no aparelho.

## Galerias separadas por versão

| Versão | Imagens arquivadas | Compilação de origem |
| --- | --- | --- |
| [0.18](../versions/0.18/README.md) | Início com Mostrar pet e Recolher pet no topo | [41651551275](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41651551275) |
| [0.17](../versions/0.17/README.md#prévias-desta-versão) | Início, Bryan, Mimir, Luna e Presidente com arte detalhada | [41647993545](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41647993545) |
| [0.16](../versions/0.16/README.md#prévias-desta-versão) | Início e quatro pets no acabamento Canvas anterior | [41645282506](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41645282506) |
| [0.15](../versions/0.15/README.md#prévias-desta-versão) | Prévia do antigo L.U.L.A | [41639512540](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41639512540) |
| [0.13](../versions/0.13/README.md#prévias-desta-versão) | Menu rápido, radar, memória e Arkanoid | [41541064297](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41541064297) |

A 0.14 tem Release e metadados, mas não uma galeria arquivada neste catálogo. Imagens posteriores não são apresentadas como capturas daquela versão.

## Arquivos usados pelos guias atuais

| Arquivos | Versão de origem | Teste |
| --- | --- | --- |
| `inicio.png`, `bryan.png`, `mimir.png`, `luna.png`, `presidente.png` | 0.17 | `DesignTest` |
| `menu-rapido.png` | 0.13 | `DesignTest` |
| `radar.png` | 0.13 | `InterestsDesignTest` |
| `memoria.png` | 0.13 | `DashboardTest` |
| `arkanoid.png` | 0.13 | `ArkanoidOverlayTest` |
| `lula.png` | 0.15, histórico | `DesignTest` |

A arte detalhada da 0.17 foi gerada a partir das referências fornecidas pelo mantenedor. O app desenha os olhos e aplica as cores configuradas. [Arquitetura da arte](../ARTE-HD.md).

O PNG do Arkanoid tem canal alfa: as áreas vazias são transparentes e o visualizador pode mostrar uma cor por baixo. O teste compõe o campo e o PetView em uma imagem para documentar as duas janelas. No telefone, o aplicativo aberto aparece sob o jogo.

Ao publicar novas prévias, copie os arquivos para a pasta da versão e registre a compilação de origem antes de atualizar as imagens dos guias. Não substitua imagens de uma versão arquivada por arte de outra.

Interface e documentação seguem a [licença do projeto](../../../LICENSE).

## Prévias da 0.19

A [galeria 0.19](../versions/0.19/README.md) traz início, menu rápido e Quebra-blocos, extraídos dos testes DesignTest e BrickGameOverlayTest na [compilação validada](https://github.com/duOCELOT/pestinha-duOCELOT/actions/runs/41654350495). As galerias antigas mantêm suas imagens originais.
