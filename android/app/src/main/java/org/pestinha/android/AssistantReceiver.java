package org.pestinha.android;
import android.content.*;
public class AssistantReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context c,Intent i){
        try{String action=i.getAction();
            if(Tasks.ACTION.equals(action))Tasks.fire(c,i.getStringExtra("task"),i.getStringExtra("token"));
            else if(Tasks.CONTROL.equals(action))Tasks.control(c,i.getStringExtra("task"),i.getStringExtra("token"),i.getStringExtra("control"));
            else if("org.pestinha.android.REMINDER".equals(action))LocalAssistant.fire(c,i.getIntExtra("id",-1),i.getStringExtra("token"));
            else {LocalAssistant.rearm(c);Tasks.rearm(c);}
        }catch(Exception error){Tasks.prefs(c).edit().putString("error","Não foi possível processar o aviso: "+error.getClass().getSimpleName()).apply();}
    }
}
