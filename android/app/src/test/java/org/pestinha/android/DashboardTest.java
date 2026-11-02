package org.pestinha.android;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.Base64;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE,qualifiers="w360dp-h780dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DashboardTest {
 @Test public void inspectingMimirDoesNotShowBryansLiveDataOrSwitchActivePet() throws Exception {
  Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("pet",0).edit().putString("character","bryan").commit();
  File dir=new File(c.getFilesDir(),"memory/mimir");dir.mkdirs();MemoryActivity.write(new File(dir,"snapshot.json"),"{\"emotion\":\"sereno\",\"energy\":0.6,\"neurons\":100,\"synapses\":300,\"motors\":{\"WATCH\":12},\"durations\":{\"awake_seconds\":3600}}");
  PetService.running=true;PetService.liveSnapshot="{\"emotion\":\"BRYAN_LIVE_ONLY\"}";
  Activity a=Robolectric.buildActivity(MemoryActivity.class).create().get();java.lang.reflect.Field f=MemoryActivity.class.getDeclaredField("inspected");f.setAccessible(true);f.set(a,"mimir");Spinner selector=findSpinner(a.findViewById(android.R.id.content));assertNotNull(selector);selector.setSelection(2);java.lang.reflect.Method render=MemoryActivity.class.getDeclaredMethod("render");render.setAccessible(true);render.invoke(a);
  View root=((ViewGroup)a.findViewById(android.R.id.content)).getChildAt(0);String text=allText(root);assertTrue(text.contains("sereno"));assertFalse(text.contains("BRYAN_LIVE_ONLY"));assertEquals("bryan",MemoryActivity.petId(c));
  root.measure(View.MeasureSpec.makeMeasureSpec(360,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(780,View.MeasureSpec.EXACTLY));root.layout(0,0,360,780);Bitmap b=Bitmap.createBitmap(360,780,Bitmap.Config.ARGB_8888);root.draw(new Canvas(b));ByteArrayOutputStream out=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,out);System.out.println("DESIGN_PREVIEW_DASHBOARD="+Base64.getEncoder().encodeToString(out.toByteArray()));
  PetService.running=false;PetService.liveSnapshot="";a.finish();
 }
 Spinner findSpinner(View v){if(v instanceof Spinner)return (Spinner)v;if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++){Spinner found=findSpinner(((ViewGroup)v).getChildAt(i));if(found!=null)return found;}return null;}
 String allText(View v){StringBuilder s=new StringBuilder(v instanceof TextView?((TextView)v).getText():"");if(v instanceof ViewGroup)for(int i=0;i<((ViewGroup)v).getChildCount();i++)s.append(allText(((ViewGroup)v).getChildAt(i)));return s.toString();}
}
