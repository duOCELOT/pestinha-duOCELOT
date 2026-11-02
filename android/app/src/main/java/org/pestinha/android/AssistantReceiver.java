package org.pestinha.android;

import android.content.*;

public class AssistantReceiver extends BroadcastReceiver {
    @Override public void onReceive(Context context,Intent intent){
        if("org.pestinha.android.REMINDER".equals(intent.getAction()))LocalAssistant.fire(context,intent.getIntExtra("id",-1),intent.getStringExtra("token"));
        else LocalAssistant.rearm(context);
    }
}
