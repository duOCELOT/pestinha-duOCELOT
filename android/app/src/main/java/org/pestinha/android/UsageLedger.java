package org.pestinha.android;

import java.util.*;

/** In-memory reducer of Android events; only selected packages have totals. */
public final class UsageLedger {
    public static final int FOREGROUND=1,BACKGROUND=2,SCREEN_OFF=16,LOCKED=17,SHUTDOWN=26,STARTUP=27;
    final long dayStart;
    final Set<String> selected;
    final Map<String,Long> totals=new HashMap<>();
    private String active=null,lastClosed=null;
    private long segmentStart=0,sessionStart=0,lastClosedAt=0,lastSessionStart=0,lastEvent=0;
    public UsageLedger(long day,Set<String> packages){dayStart=day;selected=new HashSet<>(packages);}
    private void close(long when){
        if(active!=null){if(selected.contains(active))totals.put(active,totals.getOrDefault(active,0L)+Math.max(0,when-Math.max(segmentStart,dayStart)));
            lastClosed=active;lastClosedAt=when;lastSessionStart=sessionStart;}
        active=null;
    }
    public void accept(long when,String packageName,int type){
        if(when<lastEvent)return;lastEvent=when;
        if(type==SCREEN_OFF||type==LOCKED||type==SHUTDOWN||type==STARTUP){close(when);lastClosed=null;return;}
        if(type==FOREGROUND&&packageName!=null){
            if(packageName.equals(active))return;
            if(active!=null)close(when);
            long start=packageName.equals(lastClosed)&&when-lastClosedAt<=2000?lastSessionStart:when;
            active=packageName;segmentStart=when;sessionStart=start;
        }else if(type==BACKGROUND&&packageName!=null&&packageName.equals(active))close(when);
    }
    public Result finish(long now,boolean interactive){
        Map<String,Long> out=new HashMap<>(totals);String current=interactive&&active!=null&&selected.contains(active)?active:null;
        if(interactive&&active!=null&&selected.contains(active))out.put(active,out.getOrDefault(active,0L)+Math.max(0,now-Math.max(dayStart,segmentStart)));
        return new Result(current,current==null?0:Math.max(0,now-Math.max(dayStart,sessionStart)),out);
    }
    public static final class Result {
        public final String current;public final long sessionMillis;public final Map<String,Long> totals;
        Result(String pkg,long session,Map<String,Long> values){current=pkg;sessionMillis=session;totals=Collections.unmodifiableMap(values);}
    }
}
