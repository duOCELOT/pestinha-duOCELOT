# Pestinha para Android (experimental)

Adaptação independente do projeto de mille-okws. Não é uma versão oficial do autor.
O código de desktop permanece intacto. A pasta `android/` contém uma interface
Android nativa em Java e um adaptador Python via Chaquopy.

## O que esta versão implementa

- Bryan desenhado em Canvas transparente sobre outros apps.
- Rede LIF original, emoções, homeostase e aprendizado por carinho.
- Toque: cutucar; toque duplo: carinho; arraste: mover; segurar: controles.
- Serviço em primeiro plano com notificação e ação Guardar.
- Memória privada local, gravação periódica e ao parar.
- Pausa da animação e da simulação com a tela apagada; sem wake-lock.
- Sem internet, acessibilidade, captura de tela ou leitura de outros aplicativos.

Ainda sem memes, bolinha, protestos, personagem Jorge ou leitura de janelas.
Não há reinício automático depois de reboot ou encerramento pelo Android.
Algumas telas protegidas do sistema e aplicativos podem bloquear overlays.

## Usar

Instale o APK, abra Pestinha, conceda “aparecer sobre outros apps” e toque
em “Soltar o Bryan”. Permita notificações para facilitar o acesso ao botão
Guardar. Android 8+; APK inclui ARM64 e x86_64. O pet não exige Termux ou VNC.

## Compilar

Requer Java 17+, Gradle 8.13, Android SDK 35, Build Tools 35.0.0 e Python 3.11
no computador de compilação. Crie `local.properties` com `sdk.dir=/caminho/do/sdk`
ou defina `ANDROID_HOME`. Na pasta `android/`:

```sh
gradle assembleDebug
gradle lintDebug
```

Saída: `app/build/outputs/apk/debug/app-debug.apk`. O APK de debug é assinado
com uma chave de desenvolvimento; não é uma release para a Play Store.
Chaquopy inclui Python 3.11 e NumPy 1.26.2, suficiente para os módulos do
cérebro; não instala as dependências Qt do desktop.

O task `syncBrain` copia somente os módulos Python independentes de Qt do
repositório original para o build. `android_brain.py` faz a ponte dos toques
para os sentidos da rede. A interface e o movimento são implementados pelo
Android; essa versão não tenta reproduzir todas as ações do desktop.

## Validação e limites

O adaptador Python pode ser verificado num computador com NumPy:

```sh
PYTHONPATH=..:app/src/main/python python -m unittest discover -s tests -v
```

Antes de distribuir, compilar o APK e testar em aparelho real: permissão de
overlay, iniciar/parar repetidamente, toque duplo, arraste, rotação, tela
apagada, revogação da permissão e restauração de memória. Uma compilação
bem-sucedida não substitui esses testes. A autonomia de bateria e as regras
de segundo plano variam por fabricante.

## Licença

GNU GPL-3.0-or-later, como o código original; veja `../LICENSE`, também
incluída nos assets do APK. Esta adaptação não inclui as imagens de assets/
ou memes, que não estão cobertos pela licença do código original.
