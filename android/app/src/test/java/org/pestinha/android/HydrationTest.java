package org.pestinha.android;
import android.app.*;
import android.content.*;
import org.json.*;
import java.util.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
public class HydrationTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();Hydration.prefs(c).edit().clear().commit();c.getSharedPreferences(LocalAssistant.PREFS,0).edit().clear().commit();shadowOf(c.getSystemService(NotificationManager.class)).setNotificationsEnabled(true);}
 long at(int hour,int minute){Calendar d=Calendar.getInstance();d.set(2026,10,2,hour,minute,0);d.set(Calendar.MILLISECOND,0);return d.getTimeInMillis();}
 @Test public void windowHasNoOvernightOrCatchupBurst(){assertEquals(at(8,0),Hydration.next(at(7,0),480,1320,60));assertEquals(at(10,0),Hydration.next(at(9,0),480,1320,60));Calendar next=Calendar.getInstance();next.setTimeInMillis(at(8,0));next.add(Calendar.DAY_OF_YEAR,1);assertEquals(next.getTimeInMillis(),Hydration.next(at(21,45),480,1320,60));}
 @Test public void replacingWaterKeepsFocusAndOnlyOneWaterAlarm() throws Exception {Hydration.prefs(c).edit().putBoolean("enabled",true).commit();LocalAssistant.schedule(c,"Focus",System.currentTimeMillis()+60000,"focus");Hydration.ensure(c,true);Hydration.ensure(c,true);assertEquals(2,LocalAssistant.alarms(c).length());Hydration.prefs(c).edit().putBoolean("enabled",false).commit();Hydration.ensure(c,true);assertEquals(1,LocalAssistant.alarms(c).length());assertNotNull(LocalAssistant.focus(c));}
 @Test public void deliveredWaterRearmsAndRejectsDuplicate() throws Exception {Hydration.prefs(c).edit().putBoolean("enabled",true).commit();Hydration.ensure(c,true);JSONArray a=LocalAssistant.alarms(c);JSONObject row=a.getJSONObject(0);String token=row.getString("token");row.put("when",System.currentTimeMillis()-2000);c.getSharedPreferences(LocalAssistant.PREFS,0).edit().putString("alarms",a.toString()).commit();LocalAssistant.fire(c,2,token);LocalAssistant.fire(c,2,token);assertEquals(1,LocalAssistant.alarms(c).length());assertNotEquals(token,LocalAssistant.alarms(c).getJSONObject(0).getString("token"));assertEquals(1,shadowOf(c.getSystemService(NotificationManager.class)).getAllNotifications().size());}
 @Test public void cupsAreManualAndResetOnNewDay(){assertEquals(0,Hydration.cups(c));Hydration.drank(c);assertEquals(1,Hydration.cups(c));Hydration.prefs(c).edit().putString("day","yesterday").commit();assertEquals(0,Hydration.cups(c));}
}
