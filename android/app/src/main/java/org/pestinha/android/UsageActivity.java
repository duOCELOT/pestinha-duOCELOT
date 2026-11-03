package org.pestinha.android;

import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.widget.*;
import org.json.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;

/** Explicit opt-in controls and a local conversation-like log of the pet's remarks. */
public class UsageActivity extends Activity {
    private AlertDialog appDialog;private TextView status,report,messages,selection;
    private MetricChart usageChart;
    private CheckBox enabled;
    private boolean adjusting=false,loading=false;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService jobs=Executors.newSingleThreadExecutor();
    private final Runnable refresh=new Runnable(){public void run(){render();ui.postDelayed(this,2000);}};
    private android.content.SharedPreferences prefs(){return UsageRepository.prefs(this);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private TextView text(LinearLayout box,String s,int size){TextView t=Design.text(this,s,size,size>=18?Design.TEXT:Design.MUTED);t.setPadding(0,Design.dp(this,8),0,Design.dp(this,10));box.addView(t);return t;}
    private void button(LinearLayout box,String label,Runnable action){Design.button(box,label,"secondary",action);}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);ScrollView scroll=new ScrollView(this);LinearLayout layout=Design.column(this);layout.setPadding(Design.dp(this,22),Design.dp(this,28),Design.dp(this,22),Design.dp(this,36));scroll.addView(layout);
        text(layout,"Pausas conscientes",28);
        text(layout,"O Android fornece eventos de uso dos aplicativos. Aqui você escolhe quais acompanhar. Nenhuma tela, mensagem, senha ou conteúdo de outro app é lido. Os dados ficam neste aparelho e não são enviados aos feeds.",15);
        LinearLayout controls=Design.card(this,layout);Design.label(controls,"ACOMPANHAMENTO OPCIONAL");status=text(controls,"",16);
        button(controls,"Autorizar Acesso ao uso no Android",()->{try{startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).setData(Uri.parse("package:"+getPackageName())));}catch(ActivityNotFoundException e){startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));}});
        selection=text(controls,"",14);button(controls,"Escolher aplicativos",this::pickApps);
        enabled=new CheckBox(this);enabled.setText("Receber recados automáticos do pet");enabled.setChecked(prefs().getBoolean("enabled",false));controls.addView(enabled);
        enabled.setOnCheckedChangeListener((b,value)->{if(adjusting)return;if(value&&(!UsageRepository.hasAccess(this)||UsageRepository.selected(this).length()==0)){adjusting=true;enabled.setChecked(false);adjusting=false;toast("Autorize Acesso ao uso e escolha os apps primeiro.");return;}UsageRepository.change(this,prefs().edit().putBoolean("enabled",value));render();});
        button(controls,"Definir limite em minutos",()->number("Após quantos minutos avisar?",prefs().getInt("limit",20),5,180,"limit"));
        button(controls,"Intervalo mínimo entre recados",()->number("Minutos entre recados",prefs().getInt("cooldown",15),5,120,"cooldown"));
        button(controls,"Escolher sessão atual ou total de hoje",()->new AlertDialog.Builder(this).setTitle("Quando o pet deve comentar?").setSingleChoiceItems(new String[]{"Tempo nesta sessão","Tempo acumulado hoje"},prefs().getBoolean("daily",false)?1:0,(d,which)->{UsageRepository.change(this,prefs().edit().putBoolean("daily",which==1));d.dismiss();render();}).setNegativeButton("Cancelar",null).show());
        button(controls,"Silenciar os recados por 30 minutos",()->{UsageRepository.change(this,prefs().edit().putLong("snoozeUntil",System.currentTimeMillis()+1800000));render();});
        button(controls,"Retomar recados",()->{UsageRepository.change(this,prefs().edit().remove("snoozeUntil"));render();});
        text(controls,"Os avisos funcionam com o pet solto e a tela desbloqueada. A leitura é periódica (cerca de 30 s), não um cronômetro exato. Tela dividida, troca de atividades e restrições do fabricante podem mudar a estimativa.",14);
        LinearLayout stats=Design.card(this,layout);Design.label(stats,"TEMPO DOS APPS ESCOLHIDOS");report=text(stats,"",15);usageChart=new MetricChart(this);stats.addView(usageChart,new LinearLayout.LayoutParams(-1,-2));
        button(stats,"Atualizar medições agora",()->sample(false));button(stats,"Pedir resumo de hoje ao pet",()->sample(true));
        LinearLayout conversation=Design.card(this,layout);Design.label(conversation,"RECADOS DO PET");messages=text(conversation,"",16);messages.setTextIsSelectable(true);
        text(conversation,"Comentários escritos no tom do personagem, com os tempos medidos. Ainda não é um chat com LLM. Guardar o pet pausa os avisos automáticos; a permissão pode ser revogada no Android.",14);
        Design.button(layout,"Desativar e apagar dados locais…","danger",()->new AlertDialog.Builder(this).setTitle("Apagar o acompanhamento local?").setMessage("Desativa os avisos e apaga seleção, medições e recados do Pestinha. Não apaga o histórico mantido pelo Android nem a memória neural dos personagens.").setNegativeButton("Cancelar",null).setPositiveButton("Apagar",(d,w)->{UsageRepository.change(this,prefs().edit().clear().putBoolean("enabled",false));adjusting=true;enabled.setChecked(false);adjusting=false;render();}).show());
        setContentView(scroll);Design.install(this,scroll);render();
    }
    private void number(String title,int current,int min,int max,String key){EditText input=new EditText(this);input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);input.setText(String.valueOf(current));new AlertDialog.Builder(this).setTitle(title+" ("+min+"–"+max+")").setView(input).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",(d,w)->{try{int n=Integer.parseInt(input.getText().toString());if(n<min||n>max)throw new IllegalArgumentException();UsageRepository.change(this,prefs().edit().putInt(key,n));render();}catch(Exception e){toast("Escolha um valor entre "+min+" e "+max+".");}}).show();}
    private void pickApps(){
        toast("Carregando aplicativos deste perfil…");jobs.execute(()->{
            LinkedHashMap<String,String> found=new LinkedHashMap<>();PackageManager pm=getPackageManager();
            for(ResolveInfo r:pm.queryIntentActivities(new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER),0)){String pkg=r.activityInfo.packageName;if(!pkg.equals(getPackageName()))found.put(pkg,r.loadLabel(pm).toString().replace('\n',' ').trim());}
            JSONObject old=UsageRepository.selected(this);Iterator<String> previous=old.keys();while(previous.hasNext()){String id=previous.next();if(!found.containsKey(id))found.put(id,old.optString(id,id));}
            ArrayList<String> ids=new ArrayList<>(found.keySet());ids.sort((a,b)->found.get(a).compareToIgnoreCase(found.get(b)));
            ArrayList<AppPickerDialog.Entry> entries=new ArrayList<>();
            for(String id:ids){android.graphics.drawable.Drawable icon;boolean available=true;try{icon=pm.getApplicationIcon(id);}catch(PackageManager.NameNotFoundException error){icon=pm.getDefaultActivityIcon();available=false;}entries.add(new AppPickerDialog.Entry(id,found.get(id),AppPickerDialog.thumbnail(this,icon),available));}
            ui.post(()->{if(isDestroyed())return;if(entries.isEmpty()){toast("Nenhum app com tela de abertura foi encontrado neste perfil.");return;}appDialog=AppPickerDialog.show(this,entries,old,next->{android.content.SharedPreferences.Editor edit=prefs().edit().putString("apps",next.toString()).remove("snapshot");if(next.length()==0)edit.putBoolean("enabled",false);UsageRepository.change(this,edit);render();});});
        });
    }
    private void sample(boolean summarize){if(loading){toast("Leitura em andamento.");return;}if(!UsageRepository.hasAccess(this)){toast("Autorize Acesso ao uso primeiro.");return;}loading=true;jobs.execute(()->{try{JSONObject s=UsageRepository.sample(this);if(summarize){String message=UsageRepository.summary(this,s);if(!Thread.currentThread().isInterrupted()){UsageRepository.record(this,message);PetService.assistantNotice(message,null);}}ui.post(()->{if(!isDestroyed()){loading=false;render();}});}catch(Exception e){ui.post(()->{if(!isDestroyed()){loading=false;toast(e.getMessage());}});}});}
    private void render(){if(status==null)return;if(enabled.isChecked()!=prefs().getBoolean("enabled",false)){adjusting=true;enabled.setChecked(prefs().getBoolean("enabled",false));adjusting=false;}JSONObject apps=UsageRepository.selected(this);StringBuilder names=new StringBuilder("Apps escolhidos: ");Iterator<String> keys=apps.keys();while(keys.hasNext()){String id=keys.next();names.append(apps.optString(id,id));if(keys.hasNext())names.append(", ");}if(apps.length()==0)names.append("nenhum");selection.setText(names);
        boolean access=UsageRepository.hasAccess(this),on=prefs().getBoolean("enabled",false);String mode=prefs().getBoolean("daily",false)?"total de hoje":"sessão atual";
        status.setText((!access?"Acesso ao uso não autorizado.":on?"Recados habilitados.":"Recados automáticos desligados.")+"\nLimite: "+prefs().getInt("limit",20)+" min • "+mode+"\nIntervalo: "+prefs().getInt("cooldown",15)+" min"+(prefs().getLong("snoozeUntil",0)>System.currentTimeMillis()?"\nRecados temporariamente silenciados.":"")+(on&&!PetService.running?"\nSolte o pet para receber avisos automáticos.":""));
        String error=prefs().getString("error","");if(!error.isEmpty())status.append("\nÚltima leitura: "+error);
        try{JSONObject s=new JSONObject(prefs().getString("snapshot","{}"));if(!s.has("at")){usageChart.data(new String[0],new double[0],new String[0],1,Design.ACCENT);report.setText("Autorize o acesso, escolha apps e atualize. Os totais usam o histórico de hoje fornecido pelo Android.");}else{
            StringBuilder b=new StringBuilder("Última leitura: ").append(DateFormat.getTimeInstance().format(new Date(s.getLong("at")))).append('\n');
            if(s.optLong("day")!=UsageRepository.dayStart(System.currentTimeMillis()))b.append("Leitura de outro dia; atualize para ver hoje.\n");
            String active=s.optString("current","");if(!active.isEmpty()&&!active.equals("null"))b.append("Na leitura: ").append(apps.optString(active,active)).append(" • sessão ≈ ").append(s.optLong("session_ms")/60000).append(" min\n");
            JSONObject totals=s.optJSONObject("totals");if(totals!=null){Iterator<String> it=totals.keys();while(it.hasNext()){String id=it.next();if(apps.has(id))b.append(apps.optString(id,id)).append(": ≈ ").append(totals.optLong(id)/60000).append(" min hoje\n");}}
            report.setText(b.toString());
            ArrayList<String> ids=new ArrayList<>();Iterator<String> chartKeys=apps.keys();while(chartKeys.hasNext())ids.add(chartKeys.next());ids.sort((a,z)->Long.compare(totals==null?0:totals.optLong(z),totals==null?0:totals.optLong(a)));String[] labels=new String[ids.size()],values=new String[ids.size()];double[] durations=new double[ids.size()];double max=60000;for(int i=0;i<ids.size();i++){labels[i]=apps.optString(ids.get(i));durations[i]=totals==null?0:totals.optLong(ids.get(i));max=Math.max(max,durations[i]);values[i]=MemoryActivity.duration(durations[i]/1000);}usageChart.data(labels,durations,values,max,Design.ACCENT);
        }}catch(Exception e){report.setText("Atualize para obter uma nova leitura.");}
        try{JSONArray log=new JSONArray(prefs().getString("messages","[]"));StringBuilder b=new StringBuilder();for(int i=log.length()-1;i>=0;i--){JSONObject row=log.getJSONObject(i);b.append(row.optString("character")).append(" • ").append(DateFormat.getTimeInstance().format(new Date(row.optLong("at")))).append("\n").append(row.optString("text")).append("\n\n");}messages.setText(b.length()==0?"Ainda sem recados. Eles aparecem aqui e podem surgir junto do pet.":b.toString());}catch(Exception e){messages.setText("Ainda sem recados.");}
    }
    @Override protected void onResume(){super.onResume();PetService.usageSettingsChanged();ui.post(refresh);}
    @Override protected void onPause(){ui.removeCallbacks(refresh);super.onPause();}
    @Override protected void onDestroy(){jobs.shutdownNow();ui.removeCallbacksAndMessages(null);if(appDialog!=null)appDialog.dismiss();super.onDestroy();}
}
