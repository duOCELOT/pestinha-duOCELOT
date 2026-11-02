package org.pestinha.android;

import android.content.Context;
import java.util.concurrent.*;

/** Monitoring belongs to the visible pet's service; never starts a hidden service. */
final class UsageMonitor {
    private final Context context;private ScheduledExecutorService executor;
    UsageMonitor(Context c){context=c;}
    synchronized void update(boolean screenOn){
        stop();
        if(!screenOn||!UsageRepository.prefs(context).getBoolean("enabled",false)||!UsageRepository.hasAccess(context))return;
        executor=Executors.newSingleThreadScheduledExecutor();executor.scheduleWithFixedDelay(()->{
            if(PetService.playing||!UsageRepository.prefs(context).getBoolean("enabled",false)||!UsageRepository.hasAccess(context)||!UsageRepository.interactive(context))return;
            try{String message=UsageRepository.automaticComment(context,UsageRepository.sample(context));if(message!=null&&!Thread.currentThread().isInterrupted())PetService.assistantNotice(message,null);}
            catch(InterruptedException interrupted){Thread.currentThread().interrupt();}
            catch(Exception error){if(!Thread.currentThread().isInterrupted()&&UsageRepository.prefs(context).getBoolean("enabled",false))UsageRepository.prefs(context).edit().putString("error",String.valueOf(error.getMessage())).apply();}
        },2,30,TimeUnit.SECONDS);
    }
    synchronized void stop(){if(executor!=null){executor.shutdownNow();executor=null;}}
}
