# Pestinha Android — desenvolvimento e distribuição

[**Versão atual e histórico do Android**](../CHANGELOG.md).
Android 8.0/API 26+, ARM64 e x86_64. Interface Java/Canvas com o cérebro Python original via Chaquopy.

**Quer instalar? [Baixe o APK direto da Release](https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk).**
Não precisa de GitHub Actions, ZIP, Termux, Ubuntu ou VNC no celular.
[Apresentação e funções](../README.md) · [Guia de uso](../docs/android/GUIA-DO-APK.md) · [Referência técnica](../docs/android/REFERENCIA-TECNICA.md).

## Código e requisitos para compilar

O desenvolvimento Android fica na [branch android](https://github.com/duOCELOT/pestinha-duOCELOT/tree/android).
A `main` apresenta o projeto e a documentação; confira a branch antes de compilar.

- JDK 17; Gradle 8.13 instalado no computador.
- Android SDK Platform 35 e Build Tools 35.0.0.
- Python 3.11 e NumPy 1.26.2 para os testes do adaptador.
- AGP 8.9.2 e Chaquopy 16.1 são resolvidos pelo Gradle.

```sh
git clone --branch android --single-branch https://github.com/duOCELOT/pestinha-duOCELOT.git
cd pestinha-duOCELOT
python3 -m pip install numpy==1.26.2
PYTHONPATH=.:android/app/src/main/python python3 -m unittest discover -s android/tests -v
cd android
# Configure ANDROID_HOME ou sdk.dir em local.properties antes de executar.
gradle --no-daemon assembleDebug lintDebug verifyDebugPayload testDebugUnitTest
```

Saída: `android/app/build/outputs/apk/debug/app-debug.apk`, relativa à raiz do repositório.
Não há Gradle Wrapper versionado: use a versão de Gradle indicada acima.

`syncBrain` copia os módulos headless do cérebro; `syncPetAssets` copia definições
JSON, licença e conteúdo necessário. O APK não usa Qt nem traz o desktop dentro dele.
`verifyDebugPayload` confere os componentes esperados, incluindo Python e catálogo RSS.
O [workflow Android](../.github/workflows/android.yml) também verifica a física de inclinação,
executa os testes Python/JVM/Robolectric e disponibiliza o artefato de CI.
Os testes simulados não substituem validar permissões, sobreposição, rotação,
arraste, sensores, notificações e atualização em aparelhos reais.

## Distribuição pública

[Releases](https://github.com/duOCELOT/pestinha-duOCELOT/releases) é o canal de instalação.
Cada Release tem `Pestinha-Android.apk`, `SHA256SUMS.txt`, notas, origem da compilação
e tag apontando para o código que produziu o APK. O link permanente de divulgação é:

```text
https://github.com/duOCELOT/pestinha-duOCELOT/releases/latest/download/Pestinha-Android.apk
```

O artefato de Actions continua útil para desenvolvimento; pode expirar e exigir login.
A publicação em Release não muda o APK, sua assinatura ou seu versionCode.
[Procedimento para publicar futuras versões](../docs/android/PUBLICAR-RELEASE.md).

A assinatura de debug é fixa desde 0.2. `debug-signing.keystore.b64` é uma chave pública
**somente de desenvolvimento**, intencionalmente versionada para manter atualizações compatíveis.
Não é uma chave privada de produção e não deve ser usada numa distribuição comercial/Play Store.
O APK publicado é uma versão experimental de desenvolvimento, com `debuggable=true`.

## Memória

Os estados de Bryan, Jorge e Mimir ficam na área privada do app:
`files/memory/bryan/`, `files/memory/jorge/` e `files/memory/mimir/`.
Há salvamentos periódicos, ao apagar a tela e ao guardar. O painel mostra dados reais
persistidos ou ao vivo do personagem ativo; o temperamento inicial vem da sua definição.

**Memória e cérebro → Exportar memória** gera um JSON por personagem. Com o pet
ativo, o serviço salva o cérebro antes de exportar. Restaurar exige pet guardado,
formato e personagem compatíveis; a troca prepara uma pasta temporária e prevê rollback.
Apagar pede confirmação e afeta o personagem consultado.

O backup neural inclui os arquivos de estado conhecidos, não uma cópia de todos os dados do app.
**Não inclui** imagem personalizada, tarefas/prazos, feeds/cache, registros de água,
configurações/timers do assistente, estatísticas/recados de uso ou recordes do jogo.
Atualizar sobre uma instalação compatível mantém a área privada; desinstalar ou limpar dados apaga tudo.

### Instalações antigas 0.1

A 0.1 pode ter assinatura incompatível e não oferece o exportador atual.
Antes de desinstalar, um computador com ADB e depuração USB autorizada pode guardar
uma cópia bruta da memória de um APK de debug:

```sh
adb exec-out run-as org.pestinha.android tar -C files -cf - memory > pestinha-memoria.tar
```

Esse TAR é uma cópia técnica, não o JSON importável pela interface. O layout de versões
antigas pode precisar de migração manual para as pastas por personagem. Não considere
um conflito de assinatura resolvido sem antes preservar e verificar os dados antigos.

## Funções e limites atuais

| Área | Referência |
| --- | --- |
| Personagens, gestos, sensores, memória e água/foco | [Guia das telas](../docs/android/GUIA-DO-APK.md) |
| Arkanoid com o próprio pet e fundo transparente | [Jogo e ciclo de vida](../docs/android/ARKANOID.md) |
| Fixar posição sem gravidade | Menu de segurar → Fixar no lugar / Soltar posição |
| Tarefas, prazos e acompanhamento explícito | [Tarefas](../docs/android/TAREFAS.md) |
| Temas do dia, fontes e comentários do pet | [Meu radar](../docs/android/MEU-RADAR.md) |
| 14 RSS dos próprios veículos | [Fontes e referências](../docs/android/FONTES-RSS.md) |
| Acesso ao uso opcional, apps escolhidos, sessão/dia | [Pausas conscientes](../docs/android/PAUSAS-CONSCIENTES.md) |

O Arkanoid recebe toques sobre o app de baixo até ser fechado no ×. O pet original
vira a plataforma; não há leitura/captura da tela. A posição e o modo de movimento
anteriores são restaurados ao sair. Recordes são separados por personagem.

O radar pode consultar os feeds automaticamente quando habilitado, com pet solto
e tela desbloqueada; o intervalo de consulta é aproximadamente uma hora. Os comentários
são em balões de texto, com manchetes/fontes, sem LLM, voz ou tradução automática.
A leitura manual continua disponível. Até dez feeds ativos, um catálogo de 14 fontes,
HTTPS, timeout, 1,5 MB por resposta e limites de itens. DTDs e credenciais na URL são rejeitados.
Dados de tarefas, memória e uso dos apps não são enviados aos feeds.

## Permissões e integração com o Android

- `SYSTEM_ALERT_WINDOW`: pet e jogo sobre os apps; telas protegidas podem ocultá-los.
- Serviço em primeiro plano/notificação: pet iniciado e encerrado pelo usuário; corpo pausa com tela apagada.
- Notificações e horário preciso opcional: lembretes podem atrasar por política de bateria.
- `PACKAGE_USAGE_STATS`: Acesso ao uso explícito, desligado por padrão; não é leitura do conteúdo dos apps nem acessibilidade.
- Sensores opcionais: inclinação/gravidade e giroscópio; indisponibilidade não impede arrastar.
- Agenda/relógio/navegador: intents para telas que o usuário revisa, sem leitura de compromissos.

No jogo, sensores e balões não controlam/cobrem a plataforma. A memória neural e os
avisos de sistema continuam com seu próprio ciclo; o recorde e os contadores de ações são locais.
Não há LLM, observação de janelas de outros apps, ranking online, publicidade ou leitura de mensagens.

## Licença e origem

[GPL-3.0-or-later](../LICENSE), preservando os créditos de
[mille-okws/pestinha](https://github.com/mille-okws/pestinha).
O fork porta a interface para Android e reaproveita os módulos originais do cérebro.
O APK desenha as formas em Canvas e não empacota os memes/imagens de `assets/` do desktop.
