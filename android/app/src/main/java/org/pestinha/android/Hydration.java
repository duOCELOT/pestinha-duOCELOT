package org.pestinha.android;

import android.content.*;
import org.json.*;
import java.util.*;

/** One future reminder, rearmed after delivery; no alarms outside the chosen window. */
final class Hydration {
    static android.content.SharedPreferences prefs(Context c){return c.getSharedPreferences("hydration",0);}
    static long next(long now,int start,int end,int interval){
        Calendar day=Calendar.getInstance();day.setTimeInMillis(now);
        int minute=day.get(Calendar.HOUR_OF_DAY)*60+day.get(Calendar.MINUTE);
        day.set(Calendar.SECOND,0);day.set(Calendar.MILLISECOND,0);
        if(minute<start){day.set(Calendar.HOUR_OF_DAY,start/60);day.set(Calendar.MINUTE,start%60);return day.getTimeInMillis();}
        long candidate=now+interval*60000L;
        Calendar finish=(Calendar)day.clone();finish.set(Calendar.HOUR_OF_DAY,end/60);finish.set(Calendar.MINUTE,end%60);
        if(candidate<finish.getTimeInMillis())return candidate;
        day.add(Calendar.DAY_OF_YEAR,1);day.set(Calendar.HOUR_OF_DAY,start/60);day.set(Calendar.MINUTE,start%60);return day.getTimeInMillis();
    }
    static void ensure(Context c,boolean replace){
        if(!prefs(c).getBoolean("enabled",false)){LocalAssistant.cancel(c,2);return;}
        if(!LocalAssistant.notificationsAllowed(c))return;
        JSONArray rows=LocalAssistant.alarms(c);for(int i=0;i<rows.length();i++){JSONObject r=rows.optJSONObject(i);if(r!=null&&r.optInt("id")==2&&!replace&&r.optLong("when")>System.currentTimeMillis())return;}
        try{LocalAssistant.schedule(c,"Hora de beber água. Que tal uma pausa para se hidratar?",next(System.currentTimeMillis(),prefs(c).getInt("start",480),prefs(c).getInt("end",1320),prefs(c).getInt("interval",60)),"water");}catch(Exception ignored){}
    }
    static String today(){return new java.text.SimpleDateFormat("yyyy-MM-dd",Locale.ROOT).format(new Date());}
    static int cups(Context c){return today().equals(prefs(c).getString("day",""))?prefs(c).getInt("cups",0):0;}
    static void drank(Context c){prefs(c).edit().putString("day",today()).putInt("cups",cups(c)+1).apply();}
}
