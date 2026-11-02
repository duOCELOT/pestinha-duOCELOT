package org.pestinha.android;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.graphics.Color;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

/** Inspect and export actual persisted state, rather than inferred personality. */
public class MemoryActivity extends Activity {
    static final String[] FILES = {"nervous.json","drives.json","conditioning.json","protest.json","sad_protest.json","preferences.json","episodes.jsonl","android.json","snapshot.json"};
    private TextView body;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private final Runnable refresh = new Runnable() { public void run() { render(); ui.postDelayed(this, 1500); } };
    public static String petId(Context c) {
        return Characters.normalize(c.getSharedPreferences("pet",0).getString("character","bryan"));
    }
    static File directory(Context c) { return new File(c.getFilesDir(), "memory/"+petId(c)); }
    static String read(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream(); byte[] b=new byte[8192]; int n;
        while ((n=in.read(b))!=-1) { out.write(b,0,n); if(out.size()>4_000_000) throw new IOException("Arquivo maior que 4 MB"); }
        return out.toString("UTF-8");
    }
    static String read(File file) throws IOException { try(InputStream in=new FileInputStream(file)) { return read(in); } }
    static void write(File f,String s) throws IOException {
        try(OutputStream out=new FileOutputStream(f)) { out.write(s.getBytes(java.nio.charset.StandardCharsets.UTF_8)); }
    }
    static void backup(Context c, Uri uri) throws Exception {
        JSONObject root=new JSONObject(), entries=new JSONObject();
        root.put("format","pestinha-android"); root.put("version",1); root.put("pet",petId(c));
        root.put("exported_at",System.currentTimeMillis());
        for(String name:FILES) { File f=new File(directory(c),name); if(f.isFile()) entries.put(name,read(f)); }
        root.put("files",entries);
        try(OutputStream out=c.getContentResolver().openOutputStream(uri,"wt")) {
            if(out==null) throw new IOException("Não foi possível abrir o destino");
            out.write(root.toString(2).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }
    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        ScrollView scroll=new ScrollView(this); scroll.setBackgroundColor(Design.BG);
        LinearLayout layout=new LinearLayout(this); layout.setOrientation(LinearLayout.VERTICAL); layout.setPadding(Design.dp(this,22),Design.dp(this,30),Design.dp(this,22),Design.dp(this,40)); scroll.addView(layout);
        Design.label(layout,"MEMÓRIA E CÉREBRO");TextView heading=Design.text(this,"Como ele aprende",28,Design.TEXT);heading.setPadding(0,0,0,Design.dp(this,20));layout.addView(heading);
        Button export=new Button(this); export.setText("Exportar memória / backup JSON"); layout.addView(export);
        export.setOnClickListener(v -> startActivityForResult(new Intent(Intent.ACTION_CREATE_DOCUMENT).setType("application/json")
                .addCategory(Intent.CATEGORY_OPENABLE).putExtra(Intent.EXTRA_TITLE,"pestinha-"+petId(this)+"-"+System.currentTimeMillis()+".json"),20));
        Button restore=new Button(this); restore.setText("Restaurar backup (pet guardado)"); layout.addView(restore);
        restore.setOnClickListener(v -> { if(PetService.busy) { toast("Guarde o pet e aguarde alguns segundos."); return; }
            startActivityForResult(new Intent(Intent.ACTION_OPEN_DOCUMENT).setType("application/json").addCategory(Intent.CATEGORY_OPENABLE),21); });
        Button consolidate=new Button(this); consolidate.setText("Consolidar lembranças agora"); layout.addView(consolidate);
        consolidate.setOnClickListener(v -> { if(PetService.running) startService(new Intent(this,PetService.class).setAction("consolidate")); else toast("Solte o pet primeiro."); });
        Button clear=new Button(this);clear.setTag("danger"); clear.setText("Apagar memória deste personagem…"); layout.addView(clear);
        clear.setOnClickListener(v -> {
            if(PetService.busy) { toast("Guarde o pet e aguarde alguns segundos."); return; }
            new AlertDialog.Builder(this).setTitle("Apagar memória de "+petId(this)+"?")
                .setMessage("Isso apaga pesos aprendidos, necessidades e histórico deste personagem. Exporte um backup para guardar uma cópia.")
                .setNegativeButton("Cancelar",null).setPositiveButton("Apagar",(d,w)-> {
                    if(PetService.busy) { toast("Guarde o pet primeiro."); return; }
                    for(String name:FILES) new File(directory(this),name).delete();
                    toast("Memória apagada."); render();
                }).show();
        });
        body=new TextView(this); body.setTextColor(Color.WHITE); body.setTextSize(15); body.setTextIsSelectable(true); body.setPadding(Design.dp(this,16),Design.dp(this,18),Design.dp(this,16),Design.dp(this,18));body.setBackground(Design.shape(this,Design.CARD,20,Design.BORDER));LinearLayout.LayoutParams bodyParams=new LinearLayout.LayoutParams(-1,-2);bodyParams.topMargin=Design.dp(this,18);layout.addView(body,bodyParams);
        setContentView(scroll);Design.install(this,scroll);
    }
    private void toast(String s) { Toast.makeText(this,s,Toast.LENGTH_LONG).show(); }
    @Override protected void onActivityResult(int request,int result,Intent data) {
        super.onActivityResult(request,result,data); if(result!=RESULT_OK||data==null||data.getData()==null)return;
        Uri uri=data.getData();
        if(request==20) {
            if(PetService.running) startService(new Intent(this,PetService.class).setAction("export").putExtra("uri",uri.toString()));
            else if(PetService.busy) toast("Aguarde o salvamento e tente novamente.");
            else new Thread(()->{ try { backup(this,uri); ui.post(()->toast("Backup salvo.")); } catch(Exception e){ui.post(()->toast("Erro: "+e.getMessage()));} }).start();
        } else if(request==21) {
            if(PetService.busy){toast("Guarde o pet antes de restaurar.");return;}
            new AlertDialog.Builder(this).setTitle("Substituir a memória de "+petId(this)+"?")
                .setMessage("O backup deve ser do mesmo personagem. A memória atual será substituída.")
                .setNegativeButton("Cancelar",null).setPositiveButton("Restaurar",(d,w)->restore(uri)).show();
        }
    }
    private void restore(Uri uri) {
        if(PetService.busy){toast("Guarde o pet primeiro.");return;}
        try(InputStream in=getContentResolver().openInputStream(uri)) {
            if(in==null) throw new IOException("Arquivo indisponível");
            JSONObject data=new JSONObject(read(in));
            if(!"pestinha-android".equals(data.optString("format"))||data.optInt("version")!=1||!petId(this).equals(data.optString("pet"))) throw new IOException("Formato ou personagem incompatível");
            JSONObject files=data.getJSONObject("files");
            if(!files.has("nervous.json")||!files.has("drives.json"))throw new IOException("Backup incompleto");
            Set<String> allowed=new HashSet<>(Arrays.asList(FILES));
            Iterator<String> keys=files.keys();
            while(keys.hasNext()) {
                String key=keys.next(); if(!allowed.contains(key)) throw new IOException("Entrada desconhecida: "+key);
                String value=files.getString(key);
                if(key.endsWith(".json")) new JSONObject(value);
                else for(String line:value.split("\n")) if(!line.trim().isEmpty()) new JSONObject(line);
            }
            JSONObject neural=new JSONObject(files.getString("nervous.json"));
            if(neural.optInt("seed")!=Characters.definition(this,petId(this)).getInt("seed")||neural.optInt("version")!=1)throw new IOException("Rede incompatível");
            File dest=directory(this), parent=dest.getParentFile(); if(!parent.exists()&&!parent.mkdirs())throw new IOException("Falha ao preparar pasta");
            File stage=new File(parent,"restore-"+System.nanoTime()); if(!stage.mkdir())throw new IOException("Falha ao preparar backup");
            for(String key:allowed) if(files.has(key))write(new File(stage,key),files.getString(key));
            File previous=new File(parent,"previous-"+System.nanoTime());
            boolean existed=dest.exists(); if(existed&&!dest.renameTo(previous))throw new IOException("Falha ao guardar memória atual");
            if(!stage.renameTo(dest)) { if(existed)previous.renameTo(dest); throw new IOException("Falha ao restaurar"); }
            if(existed) { for(File f:Objects.requireNonNull(previous.listFiles()))f.delete(); previous.delete(); }
            toast("Memória restaurada. Pode soltar o pet."); render();
        } catch(Exception e){toast("Não restaurado: "+e.getMessage());}
    }
    private String pct(JSONObject j,String key){return Math.round(j.optDouble(key)*100)+"%";}
    private void render() {
        try {
            String raw=PetService.running?PetService.liveSnapshot:"";
            if(raw.isEmpty()) { File f=new File(directory(this),"snapshot.json"); if(!f.exists()){body.setText("MEMÓRIA • "+petId(this)+"\n\nSolte o pet para começar a acompanhar o cérebro. A memória antiga será carregada automaticamente.");return;} raw=read(f); }
            JSONObject s=new JSONObject(raw);
            StringBuilder b=new StringBuilder("MEMÓRIA E CÉREBRO • ").append(s.optString("name",petId(this))).append("\n")
                .append(PetService.running?"Ao vivo\n":"Último estado salvo • pet guardado\n")
                .append("\nEmoção: ").append(s.optString("emotion")).append("\nEnergia: ").append(pct(s,"energy"))
                .append("\nEstimulação: ").append(pct(s,"stimulation")).append("\nCarência: ").append(pct(s,"contact"))
                .append("\nHumor: ").append(String.format(java.util.Locale.ROOT,"%+.2f",s.optDouble("valence")))
                .append("\nContexto: ").append(s.optString("context")).append("\nProtesto: ").append(s.optString("protest"))
                .append("\nTristeza (estágio): ").append(s.optInt("sad_stage"))
                .append("\n\nRede original: ").append(s.optInt("neurons")).append(" neurônios / ").append(s.optInt("synapses")).append(" sinapses\n");
            b.append("\nINTERAÇÕES\n").append(s.optJSONObject("counts"));
            b.append("\n\nASSOCIAÇÕES APRENDIDAS\nCanal → alvo: peso (não é um pensamento em texto)\n");
            JSONArray learned=s.optJSONArray("learned");
            if(learned==null||learned.length()==0)b.append("Ainda sem associações fortes. Faça carinho durante as atividades.\n");
            else for(int i=0;i<learned.length();i++){JSONArray row=learned.getJSONArray(i);b.append(row.getString(0)).append(" → ").append(row.getString(1)).append(": ").append(String.format(java.util.Locale.ROOT,"%+.5f",row.getDouble(2))).append('\n');}
            b.append("\nCONDICIONAMENTO\n").append(s.optJSONObject("conditioning"));
            b.append("\n\nPREFERÊNCIAS (contexto, ação, resultado médio, experiências)\n").append(s.optJSONArray("preferences"));
            b.append("\n\nMOTORES • atividade em Hz\n").append(s.optJSONObject("motors").toString(2));
            b.append("\n\nHABITUAÇÃO • 1 = sensibilidade cheia\n").append(s.optJSONObject("habituation").toString(2));
            b.append("\n\nÚLTIMOS EPISÓDIOS\n");
            JSONArray recent=s.optJSONArray("recent");
            if(recent!=null) for(int i=recent.length()-1;i>=0;i--){JSONObject ep=recent.getJSONObject(i);b.append(ep.optString("quando")).append(" • ").append(ep.optString("contexto")).append("\n").append(ep.optString("acao")).append(" → ").append(ep.optString("resultado")).append("\n\n");}
            b.append("\nOs arquivos ficam na área privada do app, separados por personagem. Guardar o pet mantém a memória; desinstalar ou limpar os dados apaga. Exporte um backup para levar a memória com você.");
            body.setText(b);
        }catch(Exception e){body.setText("Ainda sem estado completo salvo. Solte o pet por alguns segundos.\n"+e.getMessage());}
    }
    @Override protected void onResume(){super.onResume();ui.post(refresh);}
    @Override protected void onPause(){ui.removeCallbacks(refresh);super.onPause();}
}
