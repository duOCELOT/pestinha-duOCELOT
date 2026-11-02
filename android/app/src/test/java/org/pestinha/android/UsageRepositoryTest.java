package org.pestinha.android;

import android.app.*;
import android.content.Context;
import android.os.PowerManager;
import org.json.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,manifest=Config.NONE)
public class UsageRepositoryTest {
    Context c;
    @Before public void setup() throws Exception {
        c=RuntimeEnvironment.getApplication();UsageRepository.prefs(c).edit().clear().putString("generation","g1").putString("apps","{\"com.instagram.android\":\"Instagram\"}").putInt("limit",5).putInt("cooldown",15).commit();
        c.getSharedPreferences("pet",0).edit().clear().putString("character","mimir").commit();
        shadowOf(c.getSystemService(AppOpsManager.class)).setMode(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),c.getPackageName(),AppOpsManager.MODE_ALLOWED);
        shadowOf(c.getSystemService(PowerManager.class)).setIsInteractive(true);shadowOf(c.getSystemService(KeyguardManager.class)).setKeyguardLocked(false);
    }
    JSONObject snapshot() throws Exception {return new JSONObject().put("generation","g1").put("current","com.instagram.android").put("session_ms",360000).put("totals",new JSONObject().put("com.instagram.android",1800000));}
    @Test public void disabledMeansNoAutomaticMessages() throws Exception {assertNull(UsageRepository.automaticComment(c,snapshot()));assertFalse(UsageRepository.prefs(c).contains("messages"));}
    @Test public void requiresPermissionEvenWhenEnabled() throws Exception {UsageRepository.prefs(c).edit().putBoolean("enabled",true).commit();shadowOf(c.getSystemService(AppOpsManager.class)).setMode(AppOpsManager.OPSTR_GET_USAGE_STATS,android.os.Process.myUid(),c.getPackageName(),AppOpsManager.MODE_IGNORED);assertNull(UsageRepository.automaticComment(c,snapshot()));try{UsageRepository.sample(c);fail();}catch(SecurityException expected){}}
    @Test public void mimirCommentsWithMeasuredTimeAndCooldown() throws Exception {UsageRepository.prefs(c).edit().putBoolean("enabled",true).commit();String first=UsageRepository.automaticComment(c,snapshot());assertNotNull(first);assertTrue(first.contains("Hmpf"));assertTrue(first.contains("6 min"));assertTrue(first.contains("Instagram"));assertNull(UsageRepository.automaticComment(c,snapshot()));assertEquals(1,new JSONArray(UsageRepository.prefs(c).getString("messages","[]")).length());}
    @Test public void dailyModeDoesNotPretendTimeIsOneSession() throws Exception {UsageRepository.prefs(c).edit().putBoolean("enabled",true).putInt("limit",20).commit();assertNull(UsageRepository.automaticComment(c,snapshot()));UsageRepository.prefs(c).edit().putBoolean("daily",true).commit();String message=UsageRepository.automaticComment(c,snapshot());assertTrue(message.contains("Hoje"));assertTrue(message.contains("30 min"));assertFalse(message.contains("nesta sessão"));}
    @Test public void snoozeAndChangedSelectionRejectAutomaticComments() throws Exception {UsageRepository.prefs(c).edit().putBoolean("enabled",true).putLong("snoozeUntil",System.currentTimeMillis()+60000).commit();assertNull(UsageRepository.automaticComment(c,snapshot()));UsageRepository.prefs(c).edit().remove("snoozeUntil").putString("generation","g2").commit();assertNull(UsageRepository.automaticComment(c,snapshot()));}
    @Test public void lockScreenDoesNotProduceRemarks() throws Exception {UsageRepository.prefs(c).edit().putBoolean("enabled",true).commit();shadowOf(c.getSystemService(KeyguardManager.class)).setKeyguardLocked(true);assertNull(UsageRepository.automaticComment(c,snapshot()));}
    @Test public void messagesAreBoundedAndSelectedAppsAreNotImplicitlyExpanded() throws Exception {for(int i=0;i<60;i++)UsageRepository.record(c,"Recado "+i);assertEquals(40,new JSONArray(UsageRepository.prefs(c).getString("messages","[]")).length());assertEquals(1,UsageRepository.selected(c).length());}
}
