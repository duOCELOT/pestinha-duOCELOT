# Central de versões — Pestinha Android

Este é o lugar para consultar **o que mudou e qual APK baixar**.

## Versão atual: 0.15 experimental

**[Baixar o APK atual](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk)** ·
[Release com hash e código-fonte](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest) ·
[Como instalar ou atualizar](docs/android/GUIA-DO-APK.md)

O APK atual inclui as funções das versões anteriores. Não é necessário instalar as versões em sequência.
Android 8.0+, ARM64 ou x86_64. O arquivo já é `.apk`, sem ZIP ou login.

## Resumo das versões

| Versão | Principal novidade | APK público |
| --- | --- | --- |
| **0.15 — atual** | Novo nome e visual do L.U.L.A | **[Baixar 0.15](https://github.com/duOCELOT/pestinha-duOCELOT/releases/download/android-v0.15.0/Pestinha-Android.apk)** |
| 0.14 | Notícias: fechar sem abrir; cuidados, visual e dois pets novos | **[Baixar 0.14](https://github.com/duOCELOT/pestinha-duOCELOT/releases/download/android-v0.14.0/Pestinha-Android.apk)** |
| 0.13 | Fundo transparente no Arkanoid | **[Baixar 0.13](https://github.com/duOCELOT/pestinha-duOCELOT/releases/download/android-v0.13.0/Pestinha-Android.apk)** |
| 0.12 | Arkanoid com o próprio pet como plataforma | Histórico de desenvolvimento |
| 0.11 | Fixar o pet onde ele for solto | Histórico de desenvolvimento |
| 0.10 | Meu radar: interesses do dia e manchetes | Histórico de desenvolvimento |
| 0.9 | Tarefas com prazo e acompanhamento | Histórico de desenvolvimento |
| 0.8 | Água, gráficos e contadores por pet | Histórico de desenvolvimento |
| 0.7 | Biblioteca RSS e pausas conscientes | Histórico de desenvolvimento |
| Anteriores | Pet nativo, personagens, memória e interações | Histórico de desenvolvimento |

**0.15, 0.14 e 0.13 têm Releases públicas.** As linhas marcadas como histórico
registram a evolução do app e não têm pacotes separados para instalar. Seus antigos
artefatos de teste não são o canal de download. Para novas instalações, use o APK atual.

## Detalhes das mudanças

### 0.15 — L.U.L.A

Nome sem sufixo, cabelo grisalho, sobrancelhas, bigode e barba em camadas. Moldura vermelha mais sóbria. ID, seed e memória mantidos; identificação de caricatura não oficial fica na descrição.

### 0.14 — Cuidados e personagens

- Notícias com ações separadas de fechar e ler.
- Fome, saúde e conforto térmico simulados; sem punição offline.
- Paletas de cores, cor personalizada e acessórios por personagem.
- Luna e Lula (caricatura não oficial), com memórias próprias.
- Interface tipada para futuros modelos leves; provedor atual é de regras locais, sem modelo instalado.
- [Guia de cuidados e personalização](docs/android/CUIDADOS.md).

### 0.13 — Arkanoid transparente

- As áreas vazias do jogo deixam o app aberto visível, sem cobrir a tela com um fundo escuro.
- Blocos, bola, pet e controles continuam visíveis, com fundos locais para textos/placar.
- Mantém a física, as fases e os recordes da 0.12.
- APK interno `0.13.0-alpha`, versionCode 14. [Release específica](https://github.com/duOCELOT/pestinha-duOCELOT/releases/tag/android-v0.13.0), com APK e SHA-256.

### 0.12 — Arkanoid com o próprio pet

- O boneco desce para a parte de baixo e se move apenas para os lados.
- Três vidas, 28 blocos por fase, velocidade progressiva, partículas e reações.
- Recorde por personagem, pausa com tela apagada/rotação e restauração do pet ao fechar.
- [Guia do Arkanoid](docs/android/ARKANOID.md).

### 0.11 — Posição fixa

- **Segurar → Fixar no lugar / Soltar posição**.
- Desativa gravidade, passeio, inclinação e impulso de arremesso enquanto fixo.
- A preferência fica salva; arrastar para outra posição continua permitido.

### 0.10 — Meu radar

- Cartões de assuntos na primeira abertura do dia, com fontes RSS configuradas por área.
- Manchetes em balões, com fonte e cartão da matéria.
- Atualização opcional com pet solto/tela desbloqueada, pausa e intervalo configuráveis.
- [Guia do radar](docs/android/MEU-RADAR.md).

### 0.9 — Tarefas e prazos

- Convite para cadastrar uma tarefa e sua entrega.
- Perguntas de andamento, avisos de prazo/atraso, começar, concluir, adiar aviso e mudar prazo.
- [Guia de tarefas](docs/android/TAREFAS.md).

### 0.8 — Água, gráficos e contadores

- Água com intervalo e janela diária, além do registro manual de copos.
- Painel por personagem com gráficos de necessidades, temperamento e atividade.
- Tempos acumulados das atividades e gráficos do uso estimado dos apps escolhidos.

### 0.7 — RSS e pausas conscientes

- Biblioteca com 14 feeds dos próprios veículos.
- Acompanhamento opcional de uso, com autorização, seleção de apps e recados por personagem.
- [Fontes RSS](docs/android/FONTES-RSS.md) · [Pausas conscientes](docs/android/PAUSAS-CONSCIENTES.md).

### Base anterior

Interface Android nativa, Bryan/Jorge/Mimir, memórias independentes, menu de segurar,
interações, brinquedos, sensores e assistente local. A assinatura estável de desenvolvimento
é usada desde 0.2. A 0.1 pode exigir migração antes de desinstalar; veja a [documentação técnica](https://github.com/duOCELOT/pestinha-duOCELOT/blob/android/android/README.md#memória).

## Como acompanhar as próximas versões

- **Novidades:** esta página.
- **Instalar:** botão de download da página principal, sempre apontando para a Release mais recente.
- **Conferir um APK:** notas, hash e código-fonte na sua Release.
- **Código e testes:** área de desenvolvimento; GitHub Actions não é necessário para instalar.

[Voltar ao projeto](README.md) · [Histórico original de desktop](CHANGELOG-DESKTOP.md).
