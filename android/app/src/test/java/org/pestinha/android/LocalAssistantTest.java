package org.pestinha.android;

import android.app.*;
import android.content.*;
import org.json.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,manifest=Config.NONE)
public class LocalAssistantTest {
    Context c;
    @Before public void setup(){c=RuntimeEnvironment.getApplication();c.getSharedPreferences(LocalAssistant.PREFS,0).edit().clear().commit();shadowOf(c.getSystemService(NotificationManager.class)).setNotificationsEnabled(true);}
    @Test public void reminderPersistsRearmsAndCancels() throws Exception {
        LocalAssistant.schedule(c,"Test",System.currentTimeMillis()+60000,"reminder");
        assertEquals(1,LocalAssistant.alarms(c).length());assertNotNull(shadowOf(c.getSystemService(AlarmManager.class)).getNextScheduledAlarm());
        LocalAssistant.rearm(c);assertNotNull(shadowOf(c.getSystemService(AlarmManager.class)).getNextScheduledAlarm());
        int id=LocalAssistant.alarms(c).getJSONObject(0).getInt("id");LocalAssistant.cancel(c,id);assertEquals(0,LocalAssistant.alarms(c).length());
    }
    @Test public void focusReplacementLeavesOtherRemindersUntouched() throws Exception {
        long deadline=System.currentTimeMillis()+60000;
        LocalAssistant.schedule(c,"Remember",deadline,"reminder");LocalAssistant.schedule(c,"Focus",deadline,"focus");LocalAssistant.schedule(c,"Break",deadline,"break");
        assertEquals(2,LocalAssistant.alarms(c).length());assertEquals("break",LocalAssistant.focus(c).getString("kind"));
    }
    @Test public void staleBroadcastCannotConsumeNewReminder() throws Exception {
        LocalAssistant.schedule(c,"Focus",System.currentTimeMillis()+60000,"focus");
        String old=LocalAssistant.focus(c).getString("token");LocalAssistant.schedule(c,"New",System.currentTimeMillis()+120000,"focus");
        LocalAssistant.fire(c,1,old);assertNotNull(LocalAssistant.focus(c));assertEquals("New",LocalAssistant.focus(c).getString("title"));
    }
    @Test public void completedReminderIsDeliveredOnlyOnce() throws Exception {
        LocalAssistant.schedule(c,"Done",System.currentTimeMillis()+60000,"reminder");JSONArray a=LocalAssistant.alarms(c);JSONObject row=a.getJSONObject(0);row.put("when",System.currentTimeMillis()-1000);
        c.getSharedPreferences(LocalAssistant.PREFS,0).edit().putString("alarms",a.toString()).commit();
        int id=row.getInt("id");String token=row.getString("token");LocalAssistant.fire(c,id,token);LocalAssistant.fire(c,id,token);
        assertEquals(0,LocalAssistant.alarms(c).length());assertEquals(1,shadowOf(c.getSystemService(NotificationManager.class)).getAllNotifications().size());
    }
    @Test public void notificationDenialDoesNotSaveAnUndeliverableReminder() throws Exception {
        shadowOf(c.getSystemService(NotificationManager.class)).setNotificationsEnabled(false);
        try{LocalAssistant.schedule(c,"Denied",System.currentTimeMillis()+60000,"reminder");fail();}catch(IllegalStateException expected){}
        assertEquals(0,LocalAssistant.alarms(c).length());
    }
}
