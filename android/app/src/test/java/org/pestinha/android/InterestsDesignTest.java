package org.pestinha.android;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.*;
import static org.junit.Assert.*;
import static org.robolectric.Shadows.shadowOf;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE,qualifiers="w360dp-h780dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class InterestsDesignTest {
 @Test public void selectingCardsIsExplicitAccessibleAndSavesOnlyOnConfirmation() throws Exception {
  Context c=RuntimeEnvironment.getApplication();NewsRepository.prefs(c).edit().clear().commit();InterestsDialog.visible=false;Activity activity=Robolectric.buildActivity(Activity.class).create().get();AtomicBoolean saved=new AtomicBoolean();Dialog dialog=InterestsDialog.show(activity,()->saved.set(true));shadowOf(android.os.Looper.getMainLooper()).idle();View root=dialog.getWindow().getDecorView();View crypto=root.findViewWithTag("topic:Criptomoedas"),science=root.findViewWithTag("topic:Ciência");assertNotNull(crypto);crypto.performClick();science.performClick();assertTrue(crypto.isSelected());assertTrue(crypto.getContentDescription().toString().contains("selecionado"));assertEquals(0,NewsRepository.array(c,"feeds").length());
  View panel=((ViewGroup)dialog.findViewById(android.R.id.content)).getChildAt(0);panel.measure(View.MeasureSpec.makeMeasureSpec(336,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(700,View.MeasureSpec.EXACTLY));panel.layout(0,0,336,700);Bitmap b=Bitmap.createBitmap(336,700,Bitmap.Config.ARGB_8888);panel.draw(new Canvas(b));ByteArrayOutputStream out=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,out);System.out.println("DESIGN_PREVIEW_RADAR="+Base64.getEncoder().encodeToString(out.toByteArray()));
  find(root,"Salvar meu radar").performClick();shadowOf(android.os.Looper.getMainLooper()).idle();assertTrue("save callback",saved.get());assertFalse("dismiss callback completed",InterestsDialog.visible);assertEquals(2,NewsRepository.array(c,"feeds").length());activity.finish();
 }
 View find(View v,String text){if(v instanceof TextView&&text.contentEquals(((TextView)v).getText()))return v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){View found=find(((ViewGroup)v).getChildAt(i),text);if(found!=null)return found;}return null;}
}
