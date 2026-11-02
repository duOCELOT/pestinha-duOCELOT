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
public class TasksTest {
 Context c;
 @Before public void setup(){c=RuntimeEnvironment.getApplication();Tasks.prefs(c).edit().clear().commit();c.getSharedPreferences("pet",0).edit().putString("character","mimir").commit();shadowOf(c.getSystemService(NotificationManager.class)).setNotificationsEnabled(true);}
 void fixture(JSONObject t){Tasks.prefs(c).edit().putString("items",new JSONArray().put(t).toString()).commit();}
 JSONObject task(int hours) throws Exception{return Tasks.save(c,null,"Entregar relatório",System.currentTimeMillis()+6*3600000L,hours);}
 @Test public void taskPersistsOriginAndOnlyRealUserChangesState() throws Exception {JSONObject t=task(2);assertEquals("mimir",Tasks.find(c,t.getString("id")).getString("character"));assertEquals("todo",t.getString("status"));Tasks.update(c,t.getString("id"),"doing");assertEquals("doing",Tasks.find(c,t.getString("id")).getString("status"));Tasks.update(c,t.getString("id"),"done");assertEquals("done",Tasks.find(c,t.getString("id")).getString("status"));assertEquals(Long.MAX_VALUE,Tasks.when(Tasks.find(c,t.getString("id")),System.currentTimeMillis()));}
 @Test public void overdueDeliveryDoesNotReplayMissedChecksAndDuplicateIsIgnored() throws Exception {JSONObject t=task(2);String id=t.getString("id"),token=t.getString("token");t.put("due",System.currentTimeMillis()-1000);fixture(t);Tasks.fire(c,id,token);Tasks.fire(c,id,token);JSONObject after=Tasks.find(c,id);assertTrue(after.getBoolean("dueSent"));assertTrue(after.getLong("overdueAt")>System.currentTimeMillis());assertEquals(1,shadowOf(c.getSystemService(NotificationManager.class)).getAllNotifications().size());assertNotEquals(token,after.getString("token"));assertTrue(Tasks.message(c,after,"due").contains("relatório"));}
 @Test public void editedDeadlineRejectsOldAlarmAndNotificationActions() throws Exception {JSONObject old=task(2);JSONObject changed=Tasks.save(c,old.getString("id"),"Novo título",System.currentTimeMillis()+8*3600000L,0);Tasks.fire(c,old.getString("id"),old.getString("token"));Tasks.control(c,old.getString("id"),old.getString("token"),"done");assertEquals("todo",Tasks.find(c,old.getString("id")).getString("status"));assertEquals("Novo título",changed.getString("title"));assertEquals(0,shadowOf(c.getSystemService(NotificationManager.class)).getAllNotifications().size());}
 @Test public void snoozeChangesNoticeTimeWithoutChangingDeadline() throws Exception {JSONObject t=task(0);long due=System.currentTimeMillis()+60000;t.put("due",due);fixture(t);Tasks.update(c,t.getString("id"),"snooze");JSONObject after=Tasks.find(c,t.getString("id"));assertEquals(due,after.getLong("due"));assertTrue(Tasks.when(after,System.currentTimeMillis())>=System.currentTimeMillis()+1700000);}
 @Test public void deleteOrCompleteMakesOldBroadcastHarmless() throws Exception {JSONObject t=task(2);Tasks.delete(c,t.getString("id"));Tasks.fire(c,t.getString("id"),t.getString("token"));assertEquals(0,Tasks.list(c).length());assertEquals(0,shadowOf(c.getSystemService(NotificationManager.class)).getAllNotifications().size());}
 @Test public void quietHoursApplyToCheckinsAndInvitationIsOncePerDay(){Calendar at=Calendar.getInstance();at.set(2026,10,2,23,0,0);Calendar next=Calendar.getInstance();next.setTimeInMillis(Tasks.daytime(at.getTimeInMillis()));assertEquals(3,next.get(Calendar.DAY_OF_MONTH));assertEquals(8,next.get(Calendar.HOUR_OF_DAY));long now=System.currentTimeMillis();assertTrue(Tasks.invitationDue(c,now));Tasks.prefs(c).edit().putLong("invitedAt",now).commit();assertFalse(Tasks.invitationDue(c,now));Tasks.prefs(c).edit().putBoolean("invite",false).commit();assertFalse(Tasks.invitationDue(c,now+2*86400000));}
 @Test public void delayedPeriodicQuestionDoesNotInterruptAtNight() throws Exception {TimeZone previous=TimeZone.getDefault();try{Calendar utc=Calendar.getInstance(TimeZone.getTimeZone("UTC"));TimeZone.setDefault(new SimpleTimeZone((23-utc.get(Calendar.HOUR_OF_DAY))*3600000,"test-night"));JSONObject t=Tasks.save(c,null,"Tarefa noturna",System.currentTimeMillis()+48*3600000L,2);t.put("checkAt",System.currentTimeMillis()-2000);fixture(t);Tasks.fire(c,t.getString("id"),t.getString("token"));assertEquals(0,shadowOf(c.getSystemService(NotificationManager.class)).getAllNotifications().size());assertTrue(Tasks.find(c,t.getString("id")).getLong("checkAt")>System.currentTimeMillis());}finally{TimeZone.setDefault(previous);}}
 @Test public void invalidTaskDoesNotSaveOrSchedule() throws Exception {try{Tasks.save(c,null,"  ",System.currentTimeMillis()+60000,2);fail();}catch(IllegalArgumentException expected){}try{Tasks.save(c,null,"x",System.currentTimeMillis()-1,2);fail();}catch(IllegalArgumentException expected){}assertEquals(0,Tasks.list(c).length());}
}
