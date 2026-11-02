package org.pestinha.android;
import android.content.Context;
import java.util.concurrent.*;
/** Online polling only with a visible, unlocked, user-started pet and enabled radar. */
final class NewsMonitor {
    private final Context context;private ScheduledExecutorService worker;
    NewsMonitor(Context c){context=c;}
    synchronized void update(boolean screenOn){stop();if(!screenOn||!NewsRepository.prefs(context).getBoolean("radarAuto",false))return;worker=Executors.newSingleThreadScheduledExecutor();worker.scheduleWithFixedDelay(()->{
        if(!PetService.running||!UsageRepository.interactive(context)||InterestsDialog.visible||!NewsRepository.prefs(context).getBoolean("radarAuto",false))return;
        try{if(NewsRepository.needsRefresh(context,System.currentTimeMillis()))NewsRepository.refresh(context);if(Thread.currentThread().isInterrupted()||!UsageRepository.interactive(context))return;org.json.JSONObject row=NewsRepository.candidate(context,System.currentTimeMillis());if(row!=null)PetService.newsNotice(row,NewsRepository.generation(context),false);}catch(Exception ignored){}
    },4,30,TimeUnit.SECONDS);}
    synchronized void stop(){if(worker!=null){worker.shutdownNow();worker=null;}}
}
