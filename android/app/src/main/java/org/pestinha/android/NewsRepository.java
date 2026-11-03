package org.pestinha.android;

import android.content.*;
import android.text.Html;
import org.json.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/** Explicit topic subscriptions and publisher text. No LLM, profiling or unrelated data. */
final class NewsRepository {
    static final String[] TOPICS={"Notícias","Mundo","Economia","Bolsa e mercados","Criptomoedas","Tecnologia","Ciência"};
    static final String[] TITLES={"Notícias","Mundo","Economia","Bolsa","Cripto","Tecnologia","Ciência"};
    static final String[] SOURCES={"Agência Brasil","BBC • inglês","Agência Brasil","InfoMoney","Livecoins","BBC • inglês","NASA • inglês"};
    static final String[] URLS={"https://agenciabrasil.ebc.com.br/rss/ultimasnoticias/feed.xml","https://feeds.bbci.co.uk/news/world/rss.xml","https://agenciabrasil.ebc.com.br/rss/economia/feed.xml","https://www.infomoney.com.br/feed/","https://livecoins.com.br/feed/","https://feeds.bbci.co.uk/news/technology/rss.xml","https://www.nasa.gov/feed/"};
    private static final AtomicBoolean fetching=new AtomicBoolean();
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences(LocalAssistant.PREFS,0);}
    static JSONArray array(Context c,String key){try{return new JSONArray(prefs(c).getString(key,"[]"));}catch(Exception e){return new JSONArray();}}
    static String generation(Context c){return prefs(c).getString("radarGeneration","");}
    static String today(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());}
    static boolean askToday(Context c){return prefs(c).getBoolean("radarAsk",true)&&!today().equals(prefs(c).getString("radarAsked",""));}
    static void skipToday(Context c){prefs(c).edit().putString("radarAsked",today()).apply();}
    static synchronized void select(Context c,Set<String> chosen,boolean automatic) throws Exception {
        JSONArray topics=new JSONArray(),feeds=new JSONArray(),catalog=FeedCatalog.entries(c);Set<String> known=new HashSet<>(),seen=new HashSet<>();for(int i=0;i<catalog.length();i++)known.add(catalog.getJSONObject(i).getString("url"));
        JSONArray previous=array(c,"feeds");for(int i=0;i<previous.length();i++){String url=previous.optString(i);if(!known.contains(url)&&seen.add(url))feeds.put(url);}
        for(int i=0;i<TOPICS.length;i++)if(chosen.contains(TOPICS[i])){topics.put(TOPICS[i]);if(seen.add(URLS[i]))feeds.put(URLS[i]);}
        if(feeds.length()>10)throw new IllegalArgumentException("Há URLs personalizadas demais para estes temas. Remova algumas no Assistente (limite total: 10 feeds).");
        boolean saved=prefs(c).edit().putString("radarTopics",topics.toString()).putBoolean("radarAuto",automatic&&topics.length()>0).putString("feeds",feeds.toString()).putString("radarAsked",today()).putString("radarGeneration",UUID.randomUUID().toString()).remove("news").remove("newsAt").remove("newsAttempt").remove("newsError").commit();if(!saved)throw new IllegalStateException("Não foi possível salvar os assuntos.");PetService.newsSettingsChanged();
    }
    static synchronized void sourcesChanged(Context c){prefs(c).edit().putString("radarGeneration",UUID.randomUUID().toString()).remove("news").remove("newsAt").remove("newsAttempt").remove("newsError").commit();PetService.newsSettingsChanged();}
    static synchronized void automatic(Context c,boolean enabled){prefs(c).edit().putBoolean("radarAuto",enabled).putString("radarGeneration",UUID.randomUUID().toString()).commit();PetService.newsSettingsChanged();}
    static boolean needsRefresh(Context c,long now){return now-prefs(c).getLong("newsAt",0)>=3600000&&now-prefs(c).getLong("newsAttempt",0)>=300000;}
    static String plain(String text){return Html.fromHtml(text,Html.FROM_HTML_MODE_LEGACY).toString().replaceAll("\\s+"," ").trim();}
    static String key(JSONObject row){return row.optString("link")+"|"+plain(row.optString("title"));}
    static String category(Context c,String url){try{JSONArray all=FeedCatalog.entries(c);for(int i=0;i<all.length();i++){JSONObject f=all.getJSONObject(i);if(url.equals(f.getString("url")))return f.getString("category");}}catch(Exception ignored){}return "Seu feed";}
    static boolean refresh(Context c) throws Exception {
        if(!fetching.compareAndSet(false,true))return false;
        String generation=generation(c),urls=prefs(c).getString("feeds","[]");
        try{JSONArray sources=new JSONArray(urls);ArrayList<List<JSONObject>> groups=new ArrayList<>();StringBuilder errors=new StringBuilder();int success=0;
            for(int i=0;i<sources.length();i++){
                if(Thread.currentThread().isInterrupted()||!generation.equals(generation(c))||!urls.equals(prefs(c).getString("feeds","[]")))return false;
                String url=sources.getString(i),name=FeedCatalog.name(c,url),category=category(c,url);
                try{ArrayList<JSONObject> group=new ArrayList<>();for(FeedParser.Item item:FeedClient.fetch(url))group.add(new JSONObject().put("title",plain(item.title)).put("link",item.link).put("snippet",plain(item.snippet)).put("date",item.date).put("source",url).put("publisher",name).put("category",category));groups.add(group);success++;}catch(Exception error){if(Thread.currentThread().isInterrupted())return false;errors.append(name).append(": ").append(error.getMessage()).append('\n');}
            }
            JSONArray rows=new JSONArray();Set<String> unique=new HashSet<>();for(int item=0;item<30&&rows.length()<100;item++)for(List<JSONObject> group:groups)if(item<group.size()&&rows.length()<100){JSONObject row=group.get(item);if(unique.add(key(row)))rows.put(row);}
            synchronized(NewsRepository.class){if(Thread.currentThread().isInterrupted()||!generation.equals(generation(c))||!urls.equals(prefs(c).getString("feeds","[]")))return false;android.content.SharedPreferences.Editor edit=prefs(c).edit().putLong("newsAttempt",System.currentTimeMillis()).putString("newsError",errors.toString());if(success>0)edit.putString("news",rows.toString()).putLong("newsAt",System.currentTimeMillis());edit.apply();}return success>0;
        }finally{fetching.set(false);}
    }
    static JSONObject candidate(Context c,long now){
        if(!prefs(c).getBoolean("radarAuto",false)||array(c,"feeds").length()==0||InterestsDialog.visible)return null;
        long at=prefs(c).getLong("newsAt",0);if(at==0||now-at>86400000||now-prefs(c).getLong("newsSpokenAt",0)<prefs(c).getInt("newsMinutes",20)*60000L)return null;
        Set<String> read=new HashSet<>();JSONArray seen=array(c,"newsSeen");for(int i=0;i<seen.length();i++)read.add(seen.optString(i));JSONArray rows=array(c,"news");for(int i=0;i<rows.length();i++){JSONObject row=rows.optJSONObject(i);if(row!=null&&!read.contains(key(row)))return row;}return null;
    }
    static synchronized void shown(Context c,JSONObject row){JSONArray old=array(c,"newsSeen"),next=new JSONArray();String key=key(row);for(int i=Math.max(0,old.length()-149);i<old.length();i++)if(!key.equals(old.optString(i)))next.put(old.optString(i));next.put(key);prefs(c).edit().putString("newsSeen",next.toString()).putLong("newsSpokenAt",System.currentTimeMillis()).putString("newsLast",row.toString()).apply();}
    static String message(Context c,JSONObject row){String source=row.optString("publisher",FeedCatalog.name(c,row.optString("source"))),headline=plain(row.optString("title"));if(headline.length()>220)headline=headline.substring(0,217)+"…";String pet=MemoryActivity.petId(c);return (pet.equals("lula")?"Companheiro, uma manchete de ":pet.equals("mimir")?"Hmpf. Uma manchete de ":pet.equals("jorge")?"No radar de hoje, ":"Olha esta manchete de ")+source+":\n"+headline+"\nToque para ler a notícia.";}
}
