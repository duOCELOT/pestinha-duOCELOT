# Pestinha Android 0.3 (experimental)

Adaptação independente de mille-okws/pestinha. Interface Android em Java/Canvas;
rede neural e memória originais em Python, integradas por Chaquopy. Sem Termux,
Ubuntu, Qt ou VNC no aparelho. Código de desktop preservado.

## Funções

- Bryan e Jorge, com sementes, temperamentos e memórias separados.
- Pet transparente sobre apps, gravidade, quique, arraste e arremesso por toque.
- Toque para cutucar, toque duplo para carinho, segurar para abrir controles.
- Bolinha arrastável, chute por proximidade, notebook para trabalhar junto.
- Bilhetes e memes de uma imagem escolhida pelo usuário; dispensar ensina uma
  reação negativa, deixar o bilhete na tela ensina uma reação positiva.
- Rede LIF, emoções, energia, estimulação e carência originais.
- Condicionamento original de PRESS, aprendizado por carinho e habituação.
- Memória episódica/preferências originais e consolidação durante o sono.
- Protestos e tristeza com cartazes; cantinho adaptado com saída automática ou
  por atividade do motor FUGIR; carinho encerra o cantinho.
- Fogos nativos na transição para feliz; ajustes de tamanho e velocidade.
- Contextos manuais livre, trabalho, jogo, música e social. Modo de teste tem
  tempos mais curtos, é temporário e deve ser usado só para experimentar.
- Monitor ao vivo e última fotografia salva do cérebro; exportação/restauração
  de backup JSON por personagem. O backup não inclui a imagem personalizada.

As travessuras automáticas são opcionais. A seleção aprende entre bilhete,
atravessar, nada e meme (se houver imagem). O contexto é escolhido pelo usuário,
não inferido de outros apps. Não há leitura de tela, histórico de apps, captura
ou acessibilidade. A rede decide as emoções; algumas ações solicitadas pelo
painel e os controles de movimento são adaptações explícitas para o Android.

## Limites em relação ao desktop

Não é paridade completa: não lê janelas/cursor/teclado globais nem pousa em
barras de outros apps. Não reproduz todo o controlador Qt do cantinho, as
revoluções com múltiplos objetos, as brincadeiras do notebook ou o monitor
espacial de cada neurônio. O cantinho Android usa os canais neurais originais,
mas não porta o controlador Qt. Os memes originais não vêm no APK: escolha uma
imagem sua. Não há pet sobre telas protegidas que bloqueiam overlays.

A animação e rede pausam com tela apagada; necessidades recuperam energia sem
wake-lock. O Android ainda pode encerrar o serviço. Não reinicia sozinho após
reboot; use Soltar o pet. Notificação tem botão Guardar.

## Física do celular (0.3)

Ative **Física por inclinação do celular** no painel. A translação usa o sensor
GRAVITY ou acelerômetro filtrado, não integração do giroscópio (que acumularia
erro). Giroscópio é opcional e acrescenta rotação visual ao corpo. Sensor ausente
não impede o uso do app. Amostragem normal SENSOR_DELAY_GAME, sem permissão
especial de alta frequência. Nenhum dado bruto é gravado ou transmitido.

Incline devagar: pet e bolinha aceleram na direção da gravidade na tela, com
atrito, teto de velocidade e colisões. Calibrar define a posição atual como
neutra para usar inclinação relativa; Restaurar gravidade natural retira o
zero. A calibração é temporária, zerada ao girar a orientação da tela. Arraste
tem prioridade; notebook/cantinho mantêm o pet parado. Em modo inclinação o pet
pode deslizar mesmo dormindo, sem acordar só por inclinar. Movimentos bruscos
podem alimentar o canal MOTION da rede, com intervalo mínimo entre eventos.

Os listeners param quando o modo é desativado, a tela apaga ou o serviço acaba.
A tela volta a 30 fps com física ativa para manter resposta uniforme. O giroscópio
não é usado para coletar deslocamentos ou atividades fora da interação com o pet.
Aumente a sensibilidade aos poucos; não é necessário sacudir forte o aparelho.

## Memória

Abra **Memória e cérebro** no painel. Mostra energia, estimulação, carência,
humor, motores em Hz, pesos aprendidos, habituação, condicionamento, preferências
e últimos episódios. Pesos numéricos não são pensamentos, frases ou conversa.
Os arquivos ficam em `files/memory/bryan/` ou `files/memory/jorge/`, na área
privada do aplicativo. Salva a cada 15 s, ao apagar a tela e ao guardar.

Exportar grava um JSON no destino escolhido pelo seletor do Android; quando o
pet está ativo, a exportação primeiro salva a memória no mesmo worker do cérebro.
Restaurar exige pet guardado, formato e personagem compatíveis; prepara a cópia
antes de substituir e mantém rollback se a troca de pasta falhar. Apagar pede
confirmação e afeta somente os arquivos de memória do personagem selecionado.
A imagem personalizada não está incluída no backup de memória.

Desinstalar/limpar dados apaga a memória. As versões 0.1 usavam uma assinatura
de debug diferente a cada runner do GitHub. A partir de 0.2 há uma chave pública
**somente de desenvolvimento** no repositório para manter a assinatura estável.
Não use essa chave para distribuição comercial/Play Store. Uma atualização de
0.1 para 0.2 pode ser recusada pelo Android por assinatura incompatível.

Para preservar a memória da versão 0.1, que não tem exportação na interface,
é possível usar um computador com ADB e depuração USB autorizada, antes de
qualquer desinstalação (APK debug permite run-as):

```sh
adb exec-out run-as org.pestinha.android tar -C files -cf - memory > pestinha-memoria.tar
```

Depois de instalar a nova versão, abra o app, mantenha o pet guardado e restaure:

```sh
adb shell run-as org.pestinha.android mkdir -p files
adb shell -T run-as org.pestinha.android tar -C files -xf - < pestinha-memoria.tar
```

O Android continuará usando a área privada do app; o Termux não a acessa sem
ADB/root. Não é necessário root para o procedimento ADB no APK debug.

## Compilar e verificar

Java 17+, Gradle 8.13, SDK 35, Build Tools 35.0.0 e Python 3.11 no computador.
Defina `ANDROID_HOME` ou `sdk.dir` em `local.properties`. Na pasta `android/`:

```sh
gradle assembleDebug lintDebug verifyDebugPayload
PYTHONPATH=..:app/src/main/python python -m unittest discover -s tests -v
```

`syncBrain` empacota só módulos headless originais; `syncPetAssets` copia os
JSONs de personagens e licença. `verifyDebugPayload` abre o APK e exige esses
arquivos e a aplicação Python antes do upload. Saída:
`app/build/outputs/apk/debug/app-debug.apk`. Android 8+; ARM64 e x86_64.

O workflow `.github/workflows/android.yml` faz testes do adaptador, compilação,
lint e verificação do conteúdo. A compilação não substitui testes no aparelho:
permissão, rotação, arraste/arremesso, imagens, exportar/restaurar, guardar/soltar,
tela apagada, revogação da permissão e manutenção de memória após atualização.

## Licença

GNU GPL-3.0-or-later, como o código original. Veja `../LICENSE`, incluída no APK.
A adaptação desenha as próprias formas e não empacota as imagens de `assets/`.
