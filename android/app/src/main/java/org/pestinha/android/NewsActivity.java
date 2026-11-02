package org.pestinha.android;

import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.widget.*;
import org.json.*;
import java.text.DateFormat;
import java.util.*;
import java.util.concurrent.*;

public class NewsActivity extends Activity {
    private LinearLayout articles;private TextView status;private Dialog interests;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();private final Handler ui=new Handler(Looper.getMainLooper());private boolean loading;
    private final Runnable poll=new Runnable(){public void run(){render();ui.postDelayed(this,5000);}};
    private void text(LinearLayout box,String title,int size){TextView t=Design.text(this,title,size,size>=18?Design.TEXT:Design.MUTED);t.setPadding(0,Design.dp(this,8),0,Design.dp(this,10));box.addView(t);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
    @Override public void onCreate(Bundle saved){super.onCreate(saved);ScrollView scroll=new ScrollView(this);LinearLayout layout=Design.column(this);layout.setPadding(Design.dp(this,22),Design.dp(this,28),Design.dp(this,22),Design.dp(this,40));scroll.addView(layout);Design.label(layout,"SEU RADAR");text(layout,"Assuntos para hoje",30);status=Design.text(this,"",14,Design.MUTED);layout.addView(status);
        Design.button(layout,"Escolher os assuntos de hoje","primary",()->interests=InterestsDialog.show(this,()->{render();refresh();}));
        CheckBox ask=new CheckBox(this);ask.setText("Perguntar meus interesses na primeira abertura do dia");ask.setChecked(NewsRepository.prefs(this).getBoolean("radarAsk",true));ask.setOnCheckedChangeListener((v,value)->NewsRepository.prefs(this).edit().putBoolean("radarAsk",value).apply());layout.addView(ask);
        Design.button(layout,"Ativar / pausar comentários do pet","secondary",()->{boolean enabled=!NewsRepository.prefs(this).getBoolean("radarAuto",false);if(enabled&&NewsRepository.array(this,"feeds").length()==0){toast("Escolha os assuntos primeiro.");return;}NewsRepository.automatic(this,enabled);render();});
        Design.button(layout,"Intervalo entre manchetes","secondary",()->new AlertDialog.Builder(this).setTitle("O pet traz uma notícia a cada…").setItems(new String[]{"10 minutos","20 minutos","30 minutos","60 minutos"},(d,n)->{NewsRepository.prefs(this).edit().putInt("newsMinutes",new int[]{10,20,30,60}[n]).apply();render();}).show());
        Design.button(layout,"Atualizar notícias agora","secondary",this::refresh);
        text(layout,"Comentários em balões de texto, com o tom do personagem. Sem leitura em voz alta, tradução ou resumo por IA. Toque na notícia do pet para abrir o cartão e decidir se quer visitar o site.",14);
        articles=Design.column(this);layout.addView(articles);setContentView(scroll);Design.install(this,scroll);render();
    }
    private void refresh(){if(loading)return;if(NewsRepository.array(this,"feeds").length()==0){toast("Escolha pelo menos um assunto ou adicione um feed no Assistente.");return;}loading=true;render();worker.execute(()->{try{boolean completed=NewsRepository.refresh(this);ui.post(()->{if(!isDestroyed()){loading=false;render();if(!completed)toast("A atualização não terminou ou outra consulta já está em andamento. Você pode tentar novamente.");}});}catch(Exception error){ui.post(()->{if(!isDestroyed()){loading=false;toast("Não foi possível atualizar: "+error.getMessage());render();}});}});}
    private void article(JSONObject row,boolean focused){LinearLayout card=Design.card(this,articles);Design.label(card,row.optString("category",NewsRepository.category(this,row.optString("source"))));String title=NewsRepository.plain(row.optString("title"));text(card,title,21);String publisher=row.optString("publisher",FeedCatalog.name(this,row.optString("source")));text(card,publisher+(row.optString("date").isEmpty()?"":"\nData informada pelo feed: "+row.optString("date")),13);String excerpt=NewsRepository.plain(row.optString("snippet"));if(excerpt.length()>700)excerpt=excerpt.substring(0,697)+"…";if(!excerpt.isEmpty())text(card,excerpt,15);
        Design.button(card,"Ler matéria no site","secondary",()->{try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(FeedParser.https(row.optString("link")).toString())));}catch(Exception error){toast("Não foi possível abrir o link HTTPS da matéria.");}});
        Design.button(card,"Pet comenta esta manchete","secondary",()->{if(!PetService.running){toast("Solte o pet primeiro.");return;}PetService.newsNotice(row,NewsRepository.generation(this),true);});
    }
    private void render(){if(articles==null)return;JSONArray topics=NewsRepository.array(this,"radarTopics");StringBuilder summary=new StringBuilder();for(int i=0;i<topics.length();i++){if(i>0)summary.append(" · ");summary.append(topics.optString(i));}if(topics.length()==0)summary.append("Escolha seus interesses");long at=NewsRepository.prefs(this).getLong("newsAt",0);status.setText(summary+"\n"+(NewsRepository.prefs(this).getBoolean("radarAuto",false)?"Pet comenta a cada "+NewsRepository.prefs(this).getInt("newsMinutes",20)+" min, quando está solto e a tela desbloqueada.":"Comentários automáticos pausados.")+(loading?"\nConsultando fontes…":at>0?"\nÚltima consulta: "+DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date(at)):"\nAinda sem notícias salvas.")+"\n"+NewsRepository.prefs(this).getString("newsError",""));articles.removeAllViews();
        String focused=getIntent().getStringExtra("article");if(focused!=null)try{article(new JSONObject(focused),true);Design.label(articles,"MAIS NO SEU RADAR");}catch(Exception ignored){}
        JSONArray rows=NewsRepository.array(this,"news");if(rows.length()==0)text(articles,"Salve seus assuntos e atualize para encontrar manchetes.",18);for(int i=0;i<Math.min(rows.length(),30);i++){JSONObject row=rows.optJSONObject(i);if(row!=null&&!(focused!=null&&row.toString().equals(focused)))article(row,false);}
    }
    @Override protected void onResume(){super.onResume();ui.post(poll);}
    @Override protected void onPause(){ui.removeCallbacks(poll);super.onPause();}
    @Override protected void onDestroy(){worker.shutdownNow();ui.removeCallbacksAndMessages(null);if(interests!=null)interests.dismiss();super.onDestroy();}
}
