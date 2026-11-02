package org.pestinha.android;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.*;
import android.text.Html;
import android.widget.*;
import org.json.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;

public class AssistantActivity extends Activity {
    private LinearLayout layout,reminders,news,feeds;
    private TextView countdown,feedStatus,waterStatus;
    private EditText address;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final ExecutorService network=Executors.newSingleThreadExecutor();
    private boolean loading=false;
    private final Runnable refresh=new Runnable(){public void run(){refreshTimer();refreshWater();ui.postDelayed(this,1000);}};
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private android.content.SharedPreferences prefs(){return getSharedPreferences(LocalAssistant.PREFS,0);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    private TextView text(LinearLayout box,String title,int size){TextView t=new TextView(this);t.setText(title);t.setTextColor(size>=18?Design.TEXT:Design.MUTED);t.setTextSize(size);t.setLineSpacing(dp(2),1);t.setPadding(0,dp(8),0,dp(10));box.addView(t);return t;}
    private void button(LinearLayout box,String label,Runnable action){Design.button(box,label,"secondary",action);}
    private LinearLayout section(String title){LinearLayout box=Design.card(this,layout);Design.label(box,title.toUpperCase(java.util.Locale.ROOT));return box;}
    @Override public void onCreate(Bundle saved){
        super.onCreate(saved);LocalAssistant.channel(this);LocalAssistant.rearm(this);
        ScrollView scroll=new ScrollView(this);scroll.setBackgroundColor(Design.BG);
        layout=new LinearLayout(this);layout.setOrientation(LinearLayout.VERTICAL);layout.setPadding(dp(22),dp(36),dp(22),dp(50));scroll.addView(layout);
        text(layout,"Seu assistente",28);
        text(layout,"Timers e lembretes ficam neste aparelho. Notícias usam apenas os feeds HTTPS que você adicionar. Agenda e relógio abrem para você revisar e confirmar.",15);
        LinearLayout mindful=section("Pausas conscientes");
        text(mindful,"Escolha apps e receba comentários do pet sobre o tempo de uso. Requer Acesso ao uso autorizado por você; desligado por padrão.",14);
        button(mindful,"Uso de aplicativos e recados do pet",()->startActivity(new Intent(this,UsageActivity.class)));
        LinearLayout water=section("Água ao longo do dia");waterStatus=text(water,"",18);
        button(water,"Ativar / pausar lembretes de água",()->{boolean on=!Hydration.prefs(this).getBoolean("enabled",false);if(on&&!requireNotifications())return;Hydration.prefs(this).edit().putBoolean("enabled",on).apply();Hydration.ensure(this,true);refreshWater();});
        button(water,"Intervalo entre lembretes",()->new AlertDialog.Builder(this).setTitle("Lembrar a cada…").setItems(new String[]{"30 minutos","60 minutos","90 minutos","120 minutos"},(d,w)->{Hydration.prefs(this).edit().putInt("interval",new int[]{30,60,90,120}[w]).apply();Hydration.ensure(this,true);refreshWater();}).show());
        button(water,"Escolher início e fim do dia",this::waterWindow);
        button(water,"Bebi água • registrar um copo",()->{Hydration.drank(this);refreshWater();});
        text(water,"Registro manual de copos, sem meta médica de consumo. Padrão: 08h às 22h, a cada 60 min. Os avisos podem atrasar com economia de bateria e não se acumulam ao voltar. Funcionam por notificação mesmo com o pet guardado.",14);
        LinearLayout focus=section("Foco e pausas");countdown=text(focus,"",19);
        button(focus,"Focar por 25 minutos",()->startTimer(25,"focus"));
        button(focus,"Pausa de 5 minutos",()->startTimer(5,"break"));
        button(focus,"Escolher duração do foco",()->{
            EditText minutes=new EditText(this);minutes.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);minutes.setText("25");
            new AlertDialog.Builder(this).setTitle("Foco em minutos (1 a 180)").setView(minutes).setNegativeButton("Cancelar",null).setPositiveButton("Iniciar",(d,w)->{
                try{int n=Integer.parseInt(minutes.getText().toString());if(n<1||n>180)throw new IllegalArgumentException();startTimer(n,"focus");}catch(Exception e){toast("Use uma duração entre 1 e 180 minutos.");}
            }).show();
        });
        button(focus,"Cancelar timer",()->{LocalAssistant.cancel(this,1);refreshTimer();renderReminders();});
        text(focus,"O aviso usa notificações. Sem autorização de horário preciso, o Android pode atrasá-lo durante a economia de bateria. Para um alarme do sistema, use o atalho Relógio abaixo.",14);
        button(focus,"Permitir notificações",this::requestNotifications);
        button(focus,"Autorizar horário preciso (opcional)",()->{
            if(Build.VERSION.SDK_INT>=31)open(new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM,Uri.parse("package:"+getPackageName())));
            else toast("Este Android já permite agendamento preciso.");
        });
        LinearLayout remindersBox=section("Lembretes locais");
        button(remindersBox,"Novo lembrete com data e hora",this::newReminder);
        reminders=new LinearLayout(this);reminders.setOrientation(LinearLayout.VERTICAL);remindersBox.addView(reminders);
        LinearLayout external=section("Agenda e relógio");
        button(external,"Criar evento na agenda",this::calendarEvent);
        button(external,"Criar alarme no relógio",this::clockAlarm);
        button(external,"Abrir timer do relógio",()->open(new Intent(AlarmClock.ACTION_SET_TIMER).putExtra(AlarmClock.EXTRA_LENGTH,25*60).putExtra(AlarmClock.EXTRA_MESSAGE,"Foco com o Pestinha").putExtra(AlarmClock.EXTRA_SKIP_UI,false)));
        text(external,"O app não lê seus compromissos. Confira os campos e salve no aplicativo que abrir.",14);
        LinearLayout rss=section("Notícias • RSS / Atom");
        button(rss,"Escolher fontes oficiais por categoria",this::chooseOfficialFeeds);
        text(rss,"Biblioteca de feeds dos próprios veículos, verificada em 02/11/2026. Você escolhe quais adicionar; até 10 ativos. São notícias, não cotações em tempo real.",14);
        address=new EditText(this);address.setTextColor(Color.WHITE);address.setHintTextColor(Color.LTGRAY);address.setHint("https://site.exemplo/feed.xml");address.setSingleLine();address.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_VARIATION_URI);rss.addView(address);
        button(rss,"Adicionar feed",this::addFeed);
        feeds=new LinearLayout(this);feeds.setOrientation(LinearLayout.VERTICAL);rss.addView(feeds);
        button(rss,"Atualizar notícias agora",this::fetchNews);
        button(rss,"Apagar notícias salvas",()->{prefs().edit().remove("news").remove("newsAt").apply();showCache();});
        feedStatus=text(rss,"",14);news=new LinearLayout(this);news.setOrientation(LinearLayout.VERTICAL);rss.addView(news);
        text(rss,"Os trechos vêm dos próprios feeds; ainda não há resumo por LLM nem busca geral na web. Notícias não podem acionar funções do app. Abrir uma matéria usa seu navegador.",14);
        setContentView(scroll);Design.install(this,scroll);renderFeeds();renderReminders();showCache();
    }
    private String clock(int minute){return String.format(Locale.ROOT,"%02d:%02d",minute/60,minute%60);}
    private void refreshWater(){if(waterStatus==null)return;android.content.SharedPreferences p=Hydration.prefs(this);waterStatus.setText((p.getBoolean("enabled",false)?"Lembretes ativos":"Lembretes pausados")+" • "+p.getInt("interval",60)+" min\n"+clock(p.getInt("start",480))+"–"+clock(p.getInt("end",1320))+" • "+Hydration.cups(this)+" copos registrados hoje");}
    private void waterWindow(){android.content.SharedPreferences p=Hydration.prefs(this);int start=p.getInt("start",480),end=p.getInt("end",1320);new TimePickerDialog(this,(v,h,m)->{int from=h*60+m;new TimePickerDialog(this,(v2,h2,m2)->{int to=h2*60+m2;if(to<=from){toast("O fim deve ser depois do início, no mesmo dia.");return;}p.edit().putInt("start",from).putInt("end",to).apply();Hydration.ensure(this,true);refreshWater();},end/60,end%60,true).show();},start/60,start%60,true).show();}
    private boolean requireNotifications(){if(LocalAssistant.notificationsAllowed(this))return true;requestNotifications();toast("Permita as notificações e tente criar o aviso novamente.");return false;}
    private void requestNotifications(){
        if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},12);
        else open(new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(Settings.EXTRA_APP_PACKAGE,getPackageName()));
    }
    private void startTimer(int minutes,String kind){
        if(!requireNotifications())return;
        try{LocalAssistant.schedule(this,kind.equals("focus")?"Foco concluído. Hora de uma pausa!":"Pausa concluída. Pronto para voltar?",System.currentTimeMillis()+minutes*60000L,kind);refreshTimer();renderReminders();toast("Timer iniciado.");}catch(Exception e){toast(e.getMessage());}
    }
    private void refreshTimer(){
        JSONObject f=LocalAssistant.focus(this);
        if(f==null){countdown.setText("Nenhum timer ativo.");return;}
        long seconds=Math.max(0,(f.optLong("when")-System.currentTimeMillis()+999)/1000);
        countdown.setText((f.optString("kind").equals("focus")?"Foco • ":"Pausa • ")+String.format(java.util.Locale.ROOT,"%02d:%02d",seconds/60,seconds%60)+(seconds==0?"\nAguardando entrega do aviso.":""));
    }
    private void newReminder(){
        if(!requireNotifications())return;
        EditText title=new EditText(this);title.setHint("Ex.: beber água");title.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(120)});
        new AlertDialog.Builder(this).setTitle("Qual é o lembrete?").setView(title).setNegativeButton("Cancelar",null).setPositiveButton("Escolher horário",(d,w)->{
            String value=title.getText().toString().trim();if(value.isEmpty()){toast("Escreva o lembrete.");return;}
            pickDateTime(when->{try{LocalAssistant.schedule(this,value,when,"reminder");renderReminders();toast("Lembrete salvo.");}catch(Exception e){toast(e.getMessage());}});
        }).show();
    }
    interface TimeChoice{void selected(long time);}
    private void pickDateTime(TimeChoice choice){
        Calendar now=Calendar.getInstance();
        new DatePickerDialog(this,(v,y,m,d)->{
            Calendar chosen=Calendar.getInstance();chosen.set(y,m,d);
            new TimePickerDialog(this,(clock,h,min)->{chosen.set(Calendar.HOUR_OF_DAY,h);chosen.set(Calendar.MINUTE,min);chosen.set(Calendar.SECOND,0);chosen.set(Calendar.MILLISECOND,0);choice.selected(chosen.getTimeInMillis());},now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),true).show();
        },now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void renderReminders(){
        reminders.removeAllViews();JSONArray list=LocalAssistant.alarms(this);int count=0;
        for(int i=0;i<list.length();i++){JSONObject row=list.optJSONObject(i);if(row==null||row.optInt("id")==1||row.optInt("id")==2)continue;count++;
            text(reminders,row.optString("title")+"\n"+DateFormat.getDateTimeInstance().format(new Date(row.optLong("when"))),16);
            int id=row.optInt("id");button(reminders,"Cancelar este lembrete",()->{LocalAssistant.cancel(this,id);renderReminders();});
        }
        if(count==0)text(reminders,"Nenhum lembrete pendente.",15);
    }
    private void calendarEvent(){
        EditText title=new EditText(this);title.setHint("Título do compromisso");
        new AlertDialog.Builder(this).setTitle("Criar evento").setView(title).setNegativeButton("Cancelar",null).setPositiveButton("Escolher horário",(d,w)->pickDateTime(when->open(new Intent(Intent.ACTION_INSERT).setData(CalendarContract.Events.CONTENT_URI)
            .putExtra(CalendarContract.Events.TITLE,title.getText().toString()).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,when).putExtra(CalendarContract.EXTRA_EVENT_END_TIME,when+3600000L)))).show();
    }
    private void clockAlarm(){Calendar now=Calendar.getInstance();new TimePickerDialog(this,(v,h,m)->open(new Intent(AlarmClock.ACTION_SET_ALARM).putExtra(AlarmClock.EXTRA_HOUR,h).putExtra(AlarmClock.EXTRA_MINUTES,m).putExtra(AlarmClock.EXTRA_MESSAGE,"Pestinha").putExtra(AlarmClock.EXTRA_SKIP_UI,false)),now.get(Calendar.HOUR_OF_DAY),now.get(Calendar.MINUTE),true).show();}
    private void open(Intent intent){try{startActivity(intent);}catch(ActivityNotFoundException e){toast("Nenhum aplicativo compatível instalado.");}catch(SecurityException e){toast("O aplicativo de destino não permitiu abrir esta ação.");}}
    private JSONArray feedUrls(){try{return new JSONArray(prefs().getString("feeds","[]"));}catch(JSONException e){return new JSONArray();}}
    private void chooseOfficialFeeds(){
        try{JSONArray catalog=FeedCatalog.entries(this),current=feedUrls();String[] labels=new String[catalog.length()];boolean[] chosen=new boolean[catalog.length()];
            java.util.HashSet<String> library=new java.util.HashSet<>();for(int i=0;i<catalog.length();i++){JSONObject row=catalog.getJSONObject(i);String url=row.getString("url");library.add(url);labels[i]=row.getString("category")+" • "+row.getString("name")+" ("+row.getString("language")+")";for(int j=0;j<current.length();j++)if(url.equals(current.optString(j)))chosen[i]=true;}
            new AlertDialog.Builder(this).setTitle("Fontes dos próprios veículos").setMultiChoiceItems(labels,chosen,(d,which,value)->chosen[which]=value).setNegativeButton("Cancelar",null).setPositiveButton("Salvar seleção",(d,w)->{
                try{JSONArray next=new JSONArray();java.util.HashSet<String> seen=new java.util.HashSet<>();JSONArray latest=feedUrls();for(int i=0;i<latest.length();i++){String url=latest.optString(i);if(!library.contains(url)&&seen.add(url))next.put(url);}for(int i=0;i<catalog.length();i++)if(chosen[i]){String url=catalog.getJSONObject(i).getString("url");if(seen.add(url))next.put(url);}if(next.length()>10){toast("Escolha até 10 feeds no total, incluindo os adicionados manualmente.");return;}prefs().edit().putString("feeds",next.toString()).remove("news").remove("newsAt").apply();renderFeeds();showCache();toast("Fontes salvas. Toque em Atualizar notícias agora para consultar.");}catch(Exception e){toast("Não foi possível salvar: "+e.getMessage());}
            }).show();
        }catch(Exception e){toast("Não foi possível abrir a biblioteca de fontes.");}
    }
    private void addFeed(){
        try{String url=FeedParser.https(address.getText().toString()).toString();JSONArray list=feedUrls();if(list.length()>=10){toast("Limite de 10 feeds.");return;}
            for(int i=0;i<list.length();i++)if(url.equals(list.optString(i))){toast("Esse feed já está na lista.");return;}
            list.put(url);prefs().edit().putString("feeds",list.toString()).apply();address.setText("");renderFeeds();
        }catch(Exception e){toast("Informe o endereço HTTPS do feed RSS ou Atom.");}
    }
    private void renderFeeds(){feeds.removeAllViews();JSONArray list=feedUrls();for(int i=0;i<list.length();i++){String url=list.optString(i);text(feeds,FeedCatalog.name(this,url)+"\n"+url,14);button(feeds,"Remover este feed",()->{JSONArray next=new JSONArray();JSONArray current=feedUrls();for(int j=0;j<current.length();j++)if(!url.equals(current.optString(j)))next.put(current.optString(j));prefs().edit().putString("feeds",next.toString()).remove("news").remove("newsAt").apply();renderFeeds();showCache();});}}
    private void fetchNews(){
        if(loading){toast("Atualização em andamento.");return;}JSONArray list=feedUrls();if(list.length()==0){toast("Adicione pelo menos um feed.");return;}
        loading=true;feedStatus.setText("Consultando os feeds escolhidos…");
        network.execute(()->{
            JSONArray rows=new JSONArray();StringBuilder errors=new StringBuilder();int succeeded=0;
            for(int i=0;i<list.length()&&!Thread.currentThread().isInterrupted();i++){
                if(!prefs().getString("feeds","[]").equals(list.toString()))break;
                String source=list.optString(i);try{for(FeedParser.Item item:FeedClient.fetch(source))rows.put(new JSONObject().put("title",item.title).put("link",item.link).put("snippet",item.snippet).put("date",item.date).put("source",source));succeeded++;}
                catch(Exception e){errors.append(source).append(": ").append(e.getMessage()).append('\n');}
            }
            // If the user removed a source during the request, discard that result.
            boolean hasResults=succeeded>0;
            ui.post(()->{if(isDestroyed())return;
                if(hasResults&&prefs().getString("feeds","[]").equals(list.toString()))prefs().edit().putString("news",rows.toString()).putLong("newsAt",System.currentTimeMillis()).apply();
                loading=false;showCache();if(errors.length()>0)feedStatus.append("\nNão foi possível atualizar:\n"+errors);});
        });
    }
    private String plain(String value){return Html.fromHtml(value,Html.FROM_HTML_MODE_LEGACY).toString().trim();}
    private void showCache(){
        news.removeAllViews();long at=prefs().getLong("newsAt",0);feedStatus.setText(at==0?"Nenhuma notícia salva.":"Última consulta: "+DateFormat.getDateTimeInstance().format(new Date(at))+" • conteúdo dos feeds");
        try{JSONArray list=new JSONArray(prefs().getString("news","[]"));for(int i=0;i<list.length();i++){
            JSONObject row=list.getJSONObject(i);text(news,plain(row.optString("title")),18);String snippet=plain(row.optString("snippet"));if(snippet.length()>700)snippet=snippet.substring(0,700)+"…";
            text(news,row.optString("date")+"\n"+row.optString("source")+"\n"+snippet,14);
            String headline=plain(row.optString("title"));
            button(news,"Trazer manchete com o pet",()->{if(PetService.running)PetService.assistantNotice(headline,null);else toast("Solte o pet primeiro.");});
            String link=row.optString("link");button(news,"Abrir matéria no navegador",()->{try{open(new Intent(Intent.ACTION_VIEW,Uri.parse(FeedParser.https(link).toString())));}catch(Exception e){toast("Link indisponível.");}});
        }}catch(JSONException e){feedStatus.setText("Não foi possível ler as notícias salvas.");}
    }
    @Override protected void onResume(){super.onResume();ui.post(refresh);if(reminders!=null)renderReminders();}
    @Override protected void onPause(){ui.removeCallbacks(refresh);super.onPause();}
    @Override protected void onDestroy(){network.shutdownNow();ui.removeCallbacksAndMessages(null);super.onDestroy();}
}
