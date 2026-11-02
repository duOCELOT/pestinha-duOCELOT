package org.pestinha.android;

import android.app.*;
import android.content.*;
import android.os.Build;
import org.json.*;
import java.util.*;

/** On-device one-shot reminders and focus deadlines; no network or calendar reads. */
final class LocalAssistant {
    static final String PREFS="assistant",CHANNEL="assistant-reminders";
    static JSONArray alarms(Context c){try{return new JSONArray(c.getSharedPreferences(PREFS,0).getString("alarms","[]"));}catch(JSONException e){return new JSONArray();}}
    static boolean notificationsAllowed(Context c){return c.getSystemService(NotificationManager.class).areNotificationsEnabled();}
    static void channel(Context c){c.getSystemService(NotificationManager.class).createNotificationChannel(new NotificationChannel(CHANNEL,"Foco e lembretes",NotificationManager.IMPORTANCE_DEFAULT));}
    static boolean precise(Context c){AlarmManager m=c.getSystemService(AlarmManager.class);return Build.VERSION.SDK_INT<31||m.canScheduleExactAlarms();}
    static PendingIntent pending(Context c,JSONObject row){
        Intent i=new Intent(c,AssistantReceiver.class).setAction("org.pestinha.android.REMINDER").putExtra("id",row.optInt("id")).putExtra("token",row.optString("token"));
        return PendingIntent.getBroadcast(c,row.optInt("id"),i,PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
    }
    static void arm(Context c,JSONObject row){
        AlarmManager m=c.getSystemService(AlarmManager.class);long when=Math.max(System.currentTimeMillis()+500,row.optLong("when"));
        PendingIntent pi=pending(c,row);
        if(precise(c))try{m.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);return;}catch(SecurityException ignored){}
        m.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP,when,pi);
    }
    static synchronized void schedule(Context c,String title,long when,String kind) throws Exception {
        if(!notificationsAllowed(c))throw new IllegalStateException("Permita as notificações antes de criar avisos.");
        if(when<=System.currentTimeMillis())throw new IllegalArgumentException("Escolha um horário futuro.");
        JSONArray current=alarms(c),next=new JSONArray();boolean focus=kind.equals("focus")||kind.equals("break");
        if(!focus&&current.length()>=30)throw new IllegalStateException("Limite de 30 avisos. Cancele algum antes.");
        for(int i=0;i<current.length();i++){JSONObject r=current.getJSONObject(i);if(!focus||r.optInt("id")!=1)next.put(r);}
        android.content.SharedPreferences prefs=c.getSharedPreferences(PREFS,0);
        int id=focus?1:prefs.getInt("nextId",1000);
        JSONObject row=new JSONObject().put("id",id).put("title",title).put("kind",kind).put("when",when).put("token",UUID.randomUUID().toString());
        next.put(row);
        if(!prefs.edit().putString("alarms",next.toString()).putInt("nextId",focus?prefs.getInt("nextId",1000):id+1).commit())throw new IllegalStateException("Falha ao salvar aviso.");
        try{arm(c,row);}catch(RuntimeException e){cancel(c,id);throw e;}
        if(focus)PetService.assistantNotice(kind.equals("focus")?"Hora de focar. Estou trabalhando junto!":"Hora de descansar um pouco.",kind.equals("focus")?"focus_start":"focus_end");
    }
    static synchronized void cancel(Context c,int id){
        JSONArray next=new JSONArray(),current=alarms(c);
        for(int i=0;i<current.length();i++){JSONObject r=current.optJSONObject(i);if(r!=null){if(r.optInt("id")==id)c.getSystemService(AlarmManager.class).cancel(pending(c,r));else next.put(r);}}
        c.getSharedPreferences(PREFS,0).edit().putString("alarms",next.toString()).commit();
        if(id==1)PetService.assistantNotice("Timer encerrado.","focus_end");
    }
    static synchronized void fire(Context c,int id,String token){
        JSONArray current=alarms(c);JSONObject found=null;
        for(int i=0;i<current.length();i++){JSONObject row=current.optJSONObject(i);if(row!=null&&row.optInt("id")==id&&row.optString("token").equals(token))found=row;}
        if(found==null)return;
        if(found.optLong("when")>System.currentTimeMillis()+1000){arm(c,found);return;}
        // Remove the delivered row before posting, so duplicate broadcasts do not repeat it.
        JSONArray remaining=new JSONArray();for(int i=0;i<current.length();i++){JSONObject row=current.optJSONObject(i);if(row!=null&&row.optInt("id")!=id)remaining.put(row);}
        if(!c.getSharedPreferences(PREFS,0).edit().putString("alarms",remaining.toString()).commit())return;
        channel(c);String title=found.optString("title","Lembrete");
        PendingIntent open=PendingIntent.getActivity(c,0,new Intent(c,AssistantActivity.class),PendingIntent.FLAG_UPDATE_CURRENT|PendingIntent.FLAG_IMMUTABLE);
        Notification note=new Notification.Builder(c,CHANNEL).setSmallIcon(R.drawable.ic_pet).setContentTitle("Pestinha • "+title)
            .setContentText("Toque para abrir seus controles.").setContentIntent(open).setAutoCancel(true)
            .setStyle(new Notification.BigTextStyle().bigText(title)).build();
        if(Build.VERSION.SDK_INT>=33&&c.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)!=android.content.pm.PackageManager.PERMISSION_GRANTED)return;
        try{if(notificationsAllowed(c))c.getSystemService(NotificationManager.class).notify(id+100,note);}catch(SecurityException ignored){}
        PetService.assistantNotice(title,id==1?"focus_end":null);
    }
    static void rearm(Context c){JSONArray a=alarms(c);for(int i=0;i<a.length();i++){JSONObject row=a.optJSONObject(i);if(row!=null)try{arm(c,row);}catch(RuntimeException ignored){}}}
    static JSONObject focus(Context c){JSONArray a=alarms(c);for(int i=0;i<a.length();i++){JSONObject r=a.optJSONObject(i);if(r!=null&&r.optInt("id")==1)return r;}return null;}
}
