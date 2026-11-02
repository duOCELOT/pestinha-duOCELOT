package org.pestinha.android;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private TextView status,nameTitle,energyLabel,characterDescription;
    private PetView preview;
    private ProgressBar energyBar;
    private Button launch,permission;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresh = new Runnable() {
        public void run() { updateStatus(); handler.postDelayed(this, 1500); }
    };
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private static String readText(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int count;
        while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
        return out.toString("UTF-8");
    }

    private android.app.Dialog radarDialog;private boolean radarRestore;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);radarRestore=state!=null&&state.getBoolean("radarOpen",false);LocalAssistant.rearm(this);Tasks.rearm(this);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setClipToPadding(false);
        LinearLayout layout=Design.column(this);layout.setPadding(dp(20),dp(20),dp(20),dp(28));scroll.addView(layout);
        TextView brand=Design.text(this,"PESTINHA",13,Design.ACCENT);brand.setLetterSpacing(.18f);brand.setPadding(dp(4),dp(4),0,dp(16));layout.addView(brand);
        LinearLayout hero=Design.card(this,layout);
        Design.label(hero,"SEU COMPANHEIRO DE TELA");
        nameTitle=Design.text(this,"Bryan",32,Design.TEXT);nameTitle.setTypeface(android.graphics.Typeface.create("sans-serif-medium",android.graphics.Typeface.NORMAL));hero.addView(nameTitle);
        preview=new PetView(this);preview.autoAnimate=true;
        LinearLayout.LayoutParams portrait=new LinearLayout.LayoutParams(-1,dp(150));portrait.gravity=android.view.Gravity.CENTER;hero.addView(preview,portrait);
        status=Design.text(this,"Pronto para ficar por perto.",17,Design.TEXT);hero.addView(status);
        energyLabel=Design.text(this,"Sua memória fica guardada neste aparelho.",13,Design.MUTED);energyLabel.setPadding(0,dp(6),0,dp(8));hero.addView(energyLabel);
        energyBar=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);energyBar.setMax(100);energyBar.setProgress(100);energyBar.setProgressTintList(android.content.res.ColorStateList.valueOf(Design.ACCENT));hero.addView(energyBar,new LinearLayout.LayoutParams(-1,dp(4)));
        launch=Design.button(hero,"Soltar o pet","primary",this::startPet);
        permission=Design.button(hero,"Permitir aparecer sobre outros apps","secondary",()->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))));
        TextView hint=Design.text(this,"Segure para abrir os atalhos. Dois toques fazem carinho.",14,Design.MUTED);hint.setPadding(0,dp(12),0,0);hero.addView(hint);
        navigation(layout,"note","Meu radar","Assuntos do dia e notícias com o pet",()->startActivity(new Intent(this,NewsActivity.class)));
        navigation(layout,"assistant","Assistente","Foco, lembretes e notícias",()->startActivity(new Intent(this,AssistantActivity.class)));
        navigation(layout,"note","Tarefas e prazos","Conte o que precisa fazer e acompanhe",()->startActivity(new Intent(this,TaskActivity.class)));
        navigation(layout,"memory","Memória e cérebro","Personalidade, gráficos e tempo de cada pet",()->startActivity(new Intent(this,MemoryActivity.class)));
        navigation(layout,"assistant","Pausas conscientes","Tempo nos apps e recados do pet",()->startActivity(new Intent(this,UsageActivity.class)));
        LinearLayout play=expandable(layout,"Brincar e interagir");
        button(play,"Jogar Arkanoid • o pet é a plataforma",()->send("arcade"));
        button(play,"Fazer carinho",()->send("caress"));
        button(play,"Mostrar / guardar bolinha",()->send("ball"));
        button(play,"Dormir / acordar",()->send("sleep"));
        button(play,"Trabalhar no notebook",()->send("work"));
        button(play,"Deixar um bilhete",()->send("note"));
        button(play,"Entrar / sair do cantinho",()->send("corner"));
        button(play,"Escolher imagem para memes",()->startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE),30));
        button(play,"Mostrar meu meme",()->{if(new java.io.File(MemoryActivity.directory(this),"custom-meme.png").exists())send("meme");else Toast.makeText(this,"Escolha uma imagem primeiro.",Toast.LENGTH_LONG).show();});
        button(play,"Escolher atividade",()->new AlertDialog.Builder(this).setTitle("O que estamos fazendo?").setItems(new String[]{"Livre","Trabalho","Jogo","Música","Social"},(d,which)->{
            String value=new String[]{"livre","trabalho","jogo","musica","social"}[which];getSharedPreferences("pet",0).edit().putString("context",value).apply();if(PetService.running)send("context:"+value);
        }).show());
        LinearLayout appearance=expandable(layout,"Personagem e aparência");
        text(appearance,"Guarde o pet antes de trocar de personagem. Cada um tem sua própria memória.",14,Design.MUTED);
        Spinner character=new Spinner(this);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,Characters.names(this)){
            @Override public View getView(int position,View convert,android.view.ViewGroup parent){View v=super.getView(position,convert,parent);if(v instanceof TextView){((TextView)v).setTextColor(Design.TEXT);((TextView)v).setTextSize(18);}return v;}
            @Override public View getDropDownView(int position,View convert,android.view.ViewGroup parent){View v=super.getDropDownView(position,convert,parent);v.setBackgroundColor(Design.RAISED);if(v instanceof TextView)((TextView)v).setTextColor(Design.TEXT);return v;}
        };
        character.setAdapter(adapter);character.setSelection(Characters.index(MemoryActivity.petId(this)));appearance.addView(character);
        characterDescription=text(appearance,"",14,Design.MUTED);
        character.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(AdapterView<?> p){}
            public void onItemSelected(AdapterView<?> p,View v,int index,long id){String selected=Characters.id(index);if(selected.equals(MemoryActivity.petId(MainActivity.this)))return;
                if(PetService.busy){Toast.makeText(MainActivity.this,"Guarde o pet antes de trocar.",Toast.LENGTH_LONG).show();character.setSelection(Characters.index(MemoryActivity.petId(MainActivity.this)));return;}
                getSharedPreferences("pet",0).edit().putString("character",selected).remove("error").apply();loadAppearance();updateStatus();
            }
        });
        slider(appearance,"Tamanho","size",60,160);slider(appearance,"Velocidade","speed",25,200);
        CheckBox mischief=check(appearance,"Travessuras automáticas","mischief",false);mischief.setOnCheckedChangeListener((b,value)->{getSharedPreferences("pet",0).edit().putBoolean("mischief",value).apply();if(PetService.running)send("mischief");});
        CheckBox fireworks=check(appearance,"Fogos quando ficar feliz","fireworks",true);fireworks.setOnCheckedChangeListener((b,value)->getSharedPreferences("pet",0).edit().putBoolean("fireworks",value).apply());
        LinearLayout physics=expandable(layout,"Movimento e inclinação");
        text(physics,"Segure o pet → Fixar no lugar. Ele fica onde você soltar ao arrastar. Use Soltar posição para voltar ao movimento normal.",14,Design.MUTED);
        android.hardware.SensorManager sensors=getSystemService(android.hardware.SensorManager.class);
        boolean hasTilt=sensors!=null&&(sensors.getDefaultSensor(android.hardware.Sensor.TYPE_GRAVITY)!=null||sensors.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)!=null);
        CheckBox tilt=check(physics,"Mover ao inclinar o celular","tilt",false);tilt.setEnabled(hasTilt);
        tilt.setOnCheckedChangeListener((b,value)->{getSharedPreferences("pet",0).edit().putBoolean("tilt",value).apply();if(PetService.running)send("settings");});
        text(physics,hasTilt?"Incline devagar para mover o pet e a bolinha. Calibre para escolher uma posição neutra.":"Sensor de inclinação indisponível. Você pode continuar arrastando o pet.",14,Design.MUTED);
        slider(physics,"Sensibilidade","sensitivity",25,200);
        button(physics,"Calibrar nesta posição",()->send("calibrate"));button(physics,"Restaurar gravidade natural",()->send("reset_tilt"));
        LinearLayout about=expandable(layout,"Privacidade e informações");
        text(about,"A memória fica no aparelho. A internet consulta somente os feeds escolhidos, manualmente ou pelo radar opcional. Sem leitura de tela ou da agenda. O acompanhamento de apps usa estatísticas do Android somente se você autorizar e ativar. Sensores desligam com a tela apagada.",14,Design.MUTED);
        text(about,"Pestinha Android • 0.12 experimental\nVisual baseado no desenho original de mille-okws/pestinha.",14,Design.MUTED);
        button(about,"Modo de teste • tempos curtos",()->send("fast"));
        button(about,"Licença e projeto original",()->new AlertDialog.Builder(this).setTitle("Pestinha • GPL-3.0-or-later").setMessage("Adaptação independente de mille-okws/pestinha. Código-fonte: github.com/duOCELOT/pestinha-duOCELOT, branch android.\n\n"+license()).setPositiveButton("Fechar",null).show());
        Design.button(layout,"Guardar o pet","danger",()->stopService(new Intent(this,PetService.class)));
        setContentView(scroll);Design.install(this,scroll);loadAppearance();updateStatus();
    }
    private void startPet(){
        if(PetService.busy&&!PetService.running){Toast.makeText(this,"Aguarde o início ou salvamento do pet.",Toast.LENGTH_LONG).show();return;}
        if(!Settings.canDrawOverlays(this)){Toast.makeText(this,"Permita aparecer sobre outros apps primeiro.",Toast.LENGTH_LONG).show();return;}
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},7);
        try{startForegroundService(new Intent(this,PetService.class));}catch(RuntimeException e){new AlertDialog.Builder(this).setMessage("Não foi possível iniciar: "+e.getMessage()).setPositiveButton("OK",null).show();}
    }
    private void loadAppearance(){try(java.io.InputStream in=getAssets().open("pets/"+MemoryActivity.petId(this)+".json")){org.json.JSONObject pet=new org.json.JSONObject(readText(in));preview.jorge=MemoryActivity.petId(this).equals("jorge");preview.appearance(pet.optJSONObject("aparencia"));nameTitle.setText(pet.optString("nome","Bryan"));if(characterDescription!=null)characterDescription.setText(pet.optString("descricao",pet.optString("nome")+" tem temperamento e memória próprios."));}catch(Exception ignored){preview.appearance(null);}}
    private CheckBox check(LinearLayout box,String title,String key,boolean fallback){CheckBox b=new CheckBox(this);b.setText(title);b.setChecked(getSharedPreferences("pet",0).getBoolean(key,fallback));box.addView(b);return b;}
    private LinearLayout expandable(LinearLayout parent,String title){LinearLayout card=Design.card(this,parent);TextView heading=Design.text(this,title+"   +",17,Design.TEXT);heading.setMinHeight(dp(46));heading.setGravity(android.view.Gravity.CENTER_VERTICAL);heading.setFocusable(true);card.addView(heading);LinearLayout body=Design.column(this);body.setVisibility(View.GONE);card.addView(body);heading.setOnClickListener(v->{boolean open=body.getVisibility()!=View.VISIBLE;body.setVisibility(open?View.VISIBLE:View.GONE);heading.setText(title+(open?"   −":"   +"));});return body;}
    private void navigation(LinearLayout parent,String icon,String title,String subtitle,Runnable action){LinearLayout row=new LinearLayout(this);row.setGravity(android.view.Gravity.CENTER_VERTICAL);row.setPadding(dp(18),dp(18),dp(16),dp(18));row.setBackground(Design.touch(this,Design.CARD,Design.BORDER));row.setFocusable(true);row.setContentDescription(title+". "+subtitle);
        ActionIcon graphic=new ActionIcon(this,icon);row.addView(graphic,new LinearLayout.LayoutParams(dp(28),dp(28)));LinearLayout labels=Design.column(this);LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-2,1);lp.leftMargin=dp(16);row.addView(labels,lp);labels.addView(Design.text(this,title,18,Design.TEXT));TextView sub=Design.text(this,subtitle,13,Design.MUTED);sub.setPadding(0,dp(3),0,0);labels.addView(sub);row.addView(Design.text(this,"›",24,Design.MUTED));LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=dp(12);parent.addView(row,rp);row.setOnClickListener(v->action.run());}
    private void slider(LinearLayout layout,String label,String key,int min,int max){
        TextView title=text(layout,label+": "+getSharedPreferences("pet",0).getInt(key,100)+"%",15,Design.TEXT);
        SeekBar bar=new SeekBar(this);bar.setMax(max-min);bar.setProgress(getSharedPreferences("pet",0).getInt(key,100)-min);layout.addView(bar);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onStartTrackingTouch(SeekBar b){}
            public void onProgressChanged(SeekBar b,int value,boolean fromUser){if(fromUser)title.setText(label+": "+(value+min)+"%");}
            public void onStopTrackingTouch(SeekBar b){getSharedPreferences("pet",0).edit().putInt(key,b.getProgress()+min).apply();if(PetService.running)send("settings");}
        });
    }
    @Override protected void onActivityResult(int request,int result,Intent data){
        super.onActivityResult(request,result,data);if(request!=30||result!=RESULT_OK||data==null||data.getData()==null)return;
        android.net.Uri uri=data.getData();java.io.File targetDir=MemoryActivity.directory(this);
        new Thread(()->{try{
            android.graphics.BitmapFactory.Options opt=new android.graphics.BitmapFactory.Options();opt.inJustDecodeBounds=true;
            try(java.io.InputStream in=getContentResolver().openInputStream(uri)){android.graphics.BitmapFactory.decodeStream(in,null,opt);}
            if(opt.outWidth<1||opt.outHeight<1)throw new java.io.IOException("Imagem inválida");
            opt.inSampleSize=1;while(Math.max(opt.outWidth,opt.outHeight)/opt.inSampleSize>1024)opt.inSampleSize*=2;opt.inJustDecodeBounds=false;
            android.graphics.Bitmap bitmap;try(java.io.InputStream in=getContentResolver().openInputStream(uri)){bitmap=android.graphics.BitmapFactory.decodeStream(in,null,opt);}
            if(bitmap==null)throw new java.io.IOException("Imagem não suportada");
            if(!targetDir.exists()&&!targetDir.mkdirs())throw new java.io.IOException("Falha ao criar pasta");
            java.io.File temp=new java.io.File(targetDir,"custom-meme.tmp");
            try(java.io.OutputStream out=new java.io.FileOutputStream(temp)){if(!bitmap.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out))throw new java.io.IOException("Falha ao salvar imagem");}finally{bitmap.recycle();}
            if(!temp.renameTo(new java.io.File(targetDir,"custom-meme.png")))throw new java.io.IOException("Falha ao guardar imagem");
            handler.post(()->Toast.makeText(this,"Imagem guardada para este personagem.",android.widget.Toast.LENGTH_LONG).show());
        }catch(Exception e){handler.post(()->Toast.makeText(this,"Erro na imagem: "+e.getMessage(),android.widget.Toast.LENGTH_LONG).show());}}).start();
    }
    private String license() {
        try (java.io.InputStream in = getAssets().open("LICENSE")) {
            return readText(in);
        } catch (Exception e) { return "GNU GPL versão 3 ou posterior."; }
    }
    private void send(String action) {
        if (!PetService.running) {
            Toast.makeText(this, "Solte o pet primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }
        startService(new Intent(this, PetService.class).setAction(action));
    }
    private TextView text(LinearLayout parent, String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setPadding(0, dp(10), 0, dp(14)); parent.addView(view); return view;
    }
    private void button(LinearLayout parent,String title,Runnable action){Design.button(parent,title,"secondary",action);}
    private void updateStatus(){
        if(status==null)return;
        String error=getSharedPreferences("pet",0).getString("error","");
        boolean running=PetService.running;
        if(!error.isEmpty())status.setText("Não foi possível iniciar: "+error);
        else status.setText(running?nameTitle.getText()+" está "+PetService.emotion+".":"Pronto para ficar por perto.");
        energyLabel.setText(running?"Energia • "+Math.round(PetService.energy*100)+"%":"Sua memória continua salva.");energyBar.setProgress(running?(int)Math.round(PetService.energy*100):100);
        permission.setVisibility(Settings.canDrawOverlays(this)?View.GONE:View.VISIBLE);
        launch.setText(running?"Companheiro está solto":PetService.busy?"Preparando…":"Soltar "+nameTitle.getText());launch.setEnabled(!running&&!PetService.busy);launch.setAlpha(running?.6f:1);
        if(running){try{org.json.JSONObject state=new org.json.JSONObject(PetService.liveSnapshot);preview.emotion=PetService.emotion;preview.face(state.optJSONObject("face"),state.optBoolean("caressing"));preview.notebook=state.optBoolean("notebook");}catch(Exception ignored){}}
        else {preview.emotion="observando";preview.caressing=false;preview.notebook=false;preview.face(null,false);}
        preview.invalidate();
    }
    @Override protected void onResume() { super.onResume(); handler.post(refresh);handler.post(()->{if(!isFinishing()&&!isDestroyed()&&(radarDialog==null||!radarDialog.isShowing())&&(radarRestore||NewsRepository.askToday(this))){radarRestore=false;radarDialog=InterestsDialog.show(this,()->android.widget.Toast.makeText(this,"Radar salvo. Solte o pet para receber as manchetes.",android.widget.Toast.LENGTH_LONG).show());}}); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }
    @Override protected void onSaveInstanceState(Bundle state){super.onSaveInstanceState(state);state.putBoolean("radarOpen",radarDialog!=null&&radarDialog.isShowing());}
    @Override protected void onDestroy(){handler.removeCallbacksAndMessages(null);if(radarDialog!=null)radarDialog.dismiss();super.onDestroy();}

}
