package org.pestinha.android;

import android.app.*;
import android.app.usage.*;
import android.content.*;
import android.os.*;
import org.json.*;
import java.util.*;

final class UsageRepository {
    static final String PREFS="usage";
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences(PREFS,0);}
    static JSONObject selected(Context c){try{return new JSONObject(prefs(c).getString("apps","{}"));}catch(Exception e){return new JSONObject();}}
    static void change(Context c,android.content.SharedPreferences.Editor edit){edit.putString("generation",UUID.randomUUID().toString()).commit();PetService.usageSettingsChanged();}
    static boolean hasAccess(Context c){try{return c.getSystemService(AppOpsManager.class).checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),c.getPackageName())==AppOpsManager.MODE_ALLOWED;}catch(RuntimeException e){return false;}}
    static boolean interactive(Context c){return c.getSystemService(PowerManager.class).isInteractive()&&!c.getSystemService(KeyguardManager.class).isKeyguardLocked();}
    static long dayStart(long now){Calendar day=Calendar.getInstance();day.setTimeInMillis(now);day.set(Calendar.HOUR_OF_DAY,0);day.set(Calendar.MINUTE,0);day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);return day.getTimeInMillis();}
    static JSONObject sample(Context c) throws Exception {
        if(!hasAccess(c))throw new SecurityException("Autorize Acesso ao uso nas configurações do Android.");
        long now=System.currentTimeMillis(),day=dayStart(now);JSONObject apps=selected(c);
        String generation=prefs(c).getString("generation","");
        Set<String> allowed=new HashSet<>();Iterator<String> keys=apps.keys();while(keys.hasNext())allowed.add(keys.next());
        if(allowed.isEmpty())throw new IllegalStateException("Escolha pelo menos um aplicativo.");
        UsageEvents events=c.getSystemService(UsageStatsManager.class).queryEvents(Math.max(0,day-3600000L),now);
        if(events==null)throw new IllegalStateException("O Android não disponibilizou os eventos de uso.");
        UsageLedger ledger=new UsageLedger(day,allowed);UsageEvents.Event event=new UsageEvents.Event();int count=0;
        while(events.hasNextEvent()){
            if(Thread.currentThread().isInterrupted())throw new InterruptedException();
            if(++count>100000)throw new IllegalStateException("Histórico de hoje grande demais para uma leitura confiável.");
            events.getNextEvent(event);ledger.accept(event.getTimeStamp(),event.getPackageName(),event.getEventType());
        }
        UsageLedger.Result result=ledger.finish(now,interactive(c));JSONObject totals=new JSONObject();for(String pkg:allowed)totals.put(pkg,result.totals.getOrDefault(pkg,0L));
        JSONObject snapshot=new JSONObject().put("day",day).put("at",now).put("current",result.current==null?JSONObject.NULL:result.current).put("session_ms",result.sessionMillis).put("totals",totals).put("generation",generation);
        if(!Thread.currentThread().isInterrupted()&&hasAccess(c)&&generation.equals(prefs(c).getString("generation","")))prefs(c).edit().putString("snapshot",snapshot.toString()).remove("error").apply();
        return snapshot;
    }
    static String commentary(String character,String app,long millis,boolean daily){
        long minutes=Math.max(1,millis/60000);String time=daily?"Hoje você já somou cerca de "+minutes+" min no "+app:"Você já está há cerca de "+minutes+" min no "+app+" nesta sessão";
        if(character.equals("lula"))return "Companheiro, veja bem: "+time+". Uma pausa agora cai bem, não é pouca coisa.";
        if(character.equals("mimir"))return "Hmpf. "+time+". Pelos galhos de Yggdrasil, já está bom, não? Uma pausa lhe faria bem.";
        if(character.equals("jorge"))return time+". Essa rolagem está rendendo? Bora investir numa pausa.";
        return time+". Que tal dar uma pausa e brincar um pouco comigo?";
    }
    static synchronized void record(Context c,String text){
        JSONArray old;try{old=new JSONArray(prefs(c).getString("messages","[]"));}catch(Exception e){old=new JSONArray();}
        JSONArray next=new JSONArray();for(int i=Math.max(0,old.length()-39);i<old.length();i++)next.put(old.opt(i));
        try{next.put(new JSONObject().put("at",System.currentTimeMillis()).put("character",Characters.name(c,MemoryActivity.petId(c))).put("text",text));}catch(JSONException ignored){}
        prefs(c).edit().putString("messages",next.toString()).apply();
    }
    static synchronized String automaticComment(Context c,JSONObject snapshot) throws Exception {
        android.content.SharedPreferences p=prefs(c);
        if(!p.getBoolean("enabled",false)||!hasAccess(c)||!interactive(c)||!snapshot.optString("generation").equals(p.getString("generation","")))return null;
        long now=System.currentTimeMillis();if(now<p.getLong("snoozeUntil",0))return null;
        String pkg=snapshot.optString("current","");JSONObject apps=selected(c);if(pkg.isEmpty()||pkg.equals("null")||!apps.has(pkg))return null;
        boolean daily=p.getBoolean("daily",false);long duration=daily?snapshot.getJSONObject("totals").optLong(pkg):snapshot.optLong("session_ms");
        if(duration<p.getInt("limit",20)*60000L)return null;
        long last=p.getLong("lastAlert",0),cooldown=p.getInt("cooldown",15)*60000L;if(now>=last&&now-last<cooldown)return null;
        String message=commentary(MemoryActivity.petId(c),apps.optString(pkg,pkg),duration,daily);
        p.edit().putLong("lastAlert",now).apply();record(c,message);return message;
    }
    static String summary(Context c,JSONObject snapshot) throws Exception {
        JSONObject totals=snapshot.getJSONObject("totals"),apps=selected(c);ArrayList<String> ids=new ArrayList<>();Iterator<String> keys=totals.keys();while(keys.hasNext())ids.add(keys.next());
        ids.sort((a,b)->Long.compare(totals.optLong(b),totals.optLong(a)));StringBuilder b=new StringBuilder(MemoryActivity.petId(c).equals("mimir")?"O registro das suas jornadas de hoje: ":"Seu uso estimado de hoje: ");
        int n=0;for(String id:ids){long time=totals.optLong(id);if(time<=0)continue;if(n++>0)b.append("; ");b.append(apps.optString(id,id)).append(" — ").append(time/60000).append(" min");if(n>=5)break;}
        if(n==0)b.append("ainda sem tempo registrado para os apps escolhidos");return b.append('.').toString();
    }
}
