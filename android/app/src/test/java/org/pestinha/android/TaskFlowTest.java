package org.pestinha.android;
import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;
import org.junit.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
public class TaskFlowTest {
 @Test public void tappingNewTaskOpensFormAndSavesADeadline() throws Exception {
  Context c=RuntimeEnvironment.getApplication();Tasks.prefs(c).edit().clear().commit();shadowOf(c.getSystemService(NotificationManager.class)).setNotificationsEnabled(true);
  Activity activity=Robolectric.buildActivity(TaskActivity.class).create().get();View button=findText(activity.findViewById(android.R.id.content),"Contar uma nova tarefa");assertNotNull(button);button.performClick();AlertDialog form=org.robolectric.shadows.ShadowAlertDialog.getLatestAlertDialog();assertTrue(form.isShowing());EditText title=findEdit(form.getWindow().getDecorView());assertNotNull(title);title.setText("Preparar apresentação");form.getButton(AlertDialog.BUTTON_POSITIVE).performClick();assertFalse(form.isShowing());assertEquals(1,Tasks.list(c).length());assertEquals("Preparar apresentação",Tasks.list(c).getJSONObject(0).getString("title"));assertTrue(Tasks.list(c).getJSONObject(0).getLong("due")>System.currentTimeMillis());activity.finish();
 }
 View findText(View v,String text){if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=findText(((ViewGroup)v).getChildAt(i),text);if(found!=null)return found;}return null;}
 EditText findEdit(View v){if(v instanceof EditText)return (EditText)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){EditText found=findEdit(((ViewGroup)v).getChildAt(i));if(found!=null)return found;}return null;}
}
