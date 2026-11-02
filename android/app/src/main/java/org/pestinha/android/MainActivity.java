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
    private TextView status;
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

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(16, 29, 37));
        getWindow().setNavigationBarColor(Color.rgb(16, 29, 37));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(16, 29, 37));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(26), dp(30), dp(26), dp(36));
        scroll.addView(layout);
        text(layout, "PESTINHA / ANDROID", 13, Color.rgb(115, 230, 202));
        text(layout, "Um pequeno caos.\nAgora no seu bolso.", 32, Color.WHITE);
        text(layout, "Pestinha • versão experimental 0.3", 16, Color.LTGRAY);
        PetView preview = new PetView(this);
        layout.addView(preview, new LinearLayout.LayoutParams(dp(180), dp(140)));
        status = text(layout, "Pronto para conhecer você.", 17, Color.WHITE);
        text(layout, "Toque para cutucar. Dois toques fazem carinho. Arraste e solte rápido para arremessar. Segure para abrir este painel.", 16, Color.LTGRAY);
        button(layout, "Memória e cérebro • ver / exportar / restaurar", v -> startActivity(new Intent(this, MemoryActivity.class)));
        text(layout,"Personagem (guarde o pet antes de trocar)",15,Color.LTGRAY);
        Spinner character=new Spinner(this);
        character.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Bryan","Jorge"}));
        character.setSelection(MemoryActivity.petId(this).equals("jorge")?1:0);
        layout.addView(character);
        character.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onNothingSelected(android.widget.AdapterView<?> parent){}
            public void onItemSelected(android.widget.AdapterView<?> parent,View view,int position,long id){
                String selected=position==1?"jorge":"bryan";
                if(selected.equals(MemoryActivity.petId(MainActivity.this)))return;
                if(PetService.busy){Toast.makeText(MainActivity.this,"Guarde o pet antes de trocar.",android.widget.Toast.LENGTH_LONG).show();character.setSelection(MemoryActivity.petId(MainActivity.this).equals("jorge")?1:0);return;}
                getSharedPreferences("pet",0).edit().putString("character",selected).remove("error").apply();
            }
        });
        button(layout, "1. Permitir aparecer sobre os apps", v -> {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        });
        button(layout, "2. Soltar o pet", v -> {
            if (PetService.busy && !PetService.running) {Toast.makeText(this,"Aguarde o início ou salvamento do pet.",android.widget.Toast.LENGTH_LONG).show();return;}
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Primeiro permita aparecer sobre outros apps.", Toast.LENGTH_LONG).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
            }
            try {
                startForegroundService(new Intent(this, PetService.class));
                Toast.makeText(this, "Preparando o cérebro…", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException error) {
                new AlertDialog.Builder(this).setMessage("Não foi possível iniciar: " + error.getMessage())
                        .setPositiveButton("OK", null).show();
            }
        });
        button(layout, "Fazer carinho", v -> send("caress"));
        button(layout, "Dormir / acordar", v -> send("sleep"));
        button(layout, "Bolinha • mostrar / guardar", v -> send("ball"));
        button(layout, "Notebook • trabalhar / parar", v -> send("work"));
        button(layout, "Deixar um bilhete", v -> send("note"));
        button(layout, "Cantinho • entrar / sair", v -> send("corner"));
        button(layout, "Escolher imagem para memes", v -> startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("image/*").addCategory(Intent.CATEGORY_OPENABLE),30));
        button(layout, "Mostrar meu meme", v -> {if(new java.io.File(MemoryActivity.directory(this),"custom-meme.png").exists())send("meme");else Toast.makeText(this,"Escolha uma imagem primeiro.",android.widget.Toast.LENGTH_LONG).show();});
        CheckBox mischief=new CheckBox(this); mischief.setText("Travessuras automáticas (bilhetes, memes e cartazes)");mischief.setTextColor(Color.WHITE);
        mischief.setChecked(getSharedPreferences("pet",0).getBoolean("mischief",false));layout.addView(mischief);
        mischief.setOnCheckedChangeListener((b,checked)->{getSharedPreferences("pet",0).edit().putBoolean("mischief",checked).apply();if(PetService.running)send("mischief");});
        CheckBox fireworks=new CheckBox(this);fireworks.setText("Fogos quando ficar feliz");fireworks.setTextColor(Color.WHITE);
        fireworks.setChecked(getSharedPreferences("pet",0).getBoolean("fireworks",true));layout.addView(fireworks);
        fireworks.setOnCheckedChangeListener((b,checked)->getSharedPreferences("pet",0).edit().putBoolean("fireworks",checked).apply());
        button(layout,"Contexto • livre / trabalho / jogo / música / social",v->new AlertDialog.Builder(this).setTitle("O que estamos fazendo?")
            .setItems(new String[]{"Livre","Trabalho","Jogo","Música","Social"},(d,which)->{
                String value=new String[]{"livre","trabalho","jogo","musica","social"}[which];getSharedPreferences("pet",0).edit().putString("context",value).apply();if(PetService.running)send("context:"+value);
            }).show());
        button(layout,"Modo de teste • tempos curtos (sessão atual)",v->send("fast"));
        android.hardware.SensorManager sensors=getSystemService(android.hardware.SensorManager.class);
        boolean hasTilt=sensors!=null&&(sensors.getDefaultSensor(android.hardware.Sensor.TYPE_GRAVITY)!=null||sensors.getDefaultSensor(android.hardware.Sensor.TYPE_ACCELEROMETER)!=null);
        CheckBox tilt=new CheckBox(this);tilt.setText("Física por inclinação do celular");tilt.setTextColor(Color.WHITE);tilt.setEnabled(hasTilt);
        tilt.setChecked(getSharedPreferences("pet",0).getBoolean("tilt",false));layout.addView(tilt);
        tilt.setOnCheckedChangeListener((b,checked)->{getSharedPreferences("pet",0).edit().putBoolean("tilt",checked).apply();if(PetService.running)send("settings");});
        text(layout,hasTilt?"Incline devagar para mover o pet e a bolinha. O giroscópio acrescenta balanço, se disponível. A física pausa no notebook e cantinho. Sensores desligam com a tela apagada.":"Este aparelho não oferece sensor de inclinação. O arraste continua disponível.",14,Color.LTGRAY);
        slider(layout,"Sensibilidade da inclinação", "sensitivity",25,200);
        button(layout,"Calibrar • esta posição fica neutra",v->send("calibrate"));
        button(layout,"Restaurar gravidade natural",v->send("reset_tilt"));
        slider(layout,"Tamanho", "size",60,160);
        slider(layout,"Velocidade", "speed",25,200);
        button(layout, "Guardar o pet", v -> stopService(new Intent(this, PetService.class)));
        text(layout, "Tudo fica neste aparelho. Sem internet, leitura de tela ou acesso aos seus aplicativos. A animação pausa com a tela apagada. Alguns apps do sistema podem ocultar o pet.", 14, Color.LTGRAY);
        button(layout, "Sobre o projeto e licença", v -> new AlertDialog.Builder(this)
                .setTitle("Pestinha Android • GPL-3.0-or-later")
                .setMessage("Adaptação experimental de mille-okws/pestinha. Rede neural, emoções e homeostase originais; interface Android nova. Código-fonte: github.com/mille-okws/pestinha (original). Esta adaptação inclui seus fontes na entrega.\n\nInclui bolinha, notebook, bilhetes, imagem personalizada, protestos, cantinho e monitor de memória. Janelas de outros apps e posição do cursor de desktop não são observadas. O contexto é escolhido por você. A versão Android não é uma versão oficial do autor.\n\n" + license())
                .setPositiveButton("Fechar", null).show());
        setContentView(scroll);
    }
    private void slider(LinearLayout layout,String label,String key,int min,int max){
        TextView title=text(layout,label+": "+getSharedPreferences("pet",0).getInt(key,100)+"%",15,Color.WHITE);
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
            Toast.makeText(this, "Solte o Bryan primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }
        startService(new Intent(this, PetService.class).setAction(action));
    }
    private TextView text(LinearLayout parent, String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setPadding(0, dp(10), 0, dp(14)); parent.addView(view); return view;
    }
    private void button(LinearLayout parent, String title, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(title); b.setAllCaps(false);
        b.setOnClickListener(listener);
        parent.addView(b, new LinearLayout.LayoutParams(-1, dp(58)));
    }
    private void updateStatus() {
        String error = getSharedPreferences("pet", 0).getString("error", "");
        if (!error.isEmpty()) status.setText("Não foi possível iniciar: " + error);
        else if (PetService.running) status.setText((MemoryActivity.petId(this).equals("jorge")?"Jorge":"Bryan")+" está " + PetService.emotion + ".\nEnergia: " + Math.round(PetService.energy * 100) + "%");
        else status.setText("Bryan está guardado. Sua memória continua salva.");
    }
    @Override protected void onResume() { super.onResume(); handler.post(refresh); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }
}
