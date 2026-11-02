package org.pestinha.android;
import android.content.*;
import android.view.*;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE,qualifiers="mdpi")
public class ArcadeRestoreTest {
 @Test public void leavingArcadeRestoresOriginalPositionSizeAndPinnedSetting() throws Exception {
  Context c=RuntimeEnvironment.getApplication();c.getSharedPreferences("pet",0).edit().clear().putBoolean("fixed",true).commit();PetService service=Robolectric.buildService(PetService.class).get();AtomicInteger adds=new AtomicInteger();WindowManager wm=(WindowManager)Proxy.newProxyInstance(WindowManager.class.getClassLoader(),new Class[]{WindowManager.class},(object,method,args)->{if(method.getName().equals("addView"))adds.incrementAndGet();return null;});PetView pet=new PetView(c);WindowManager.LayoutParams pos=new WindowManager.LayoutParams();pos.x=250;pos.y=650;pos.width=120;pos.height=98;
  set(service,"pos",pos);set(service,"pet",pet);set(service,"wm",wm);set(service,"world",new PetWorld(c,wm,value->{}));set(service,"beforeGameX",33);set(service,"beforeGameY",221);PetService.playing=true;
  try{Method finish=PetService.class.getDeclaredMethod("finishArcade");finish.setAccessible(true);finish.invoke(service);assertFalse(PetService.playing);assertEquals(33,pos.x);assertEquals(221,pos.y);assertEquals(150,pos.width);assertEquals(122,pos.height);assertTrue(c.getSharedPreferences("pet",0).getBoolean("fixed",false));assertEquals(1,adds.get());}finally{PetService.playing=false;}
 }
 void set(Object target,String name,Object value) throws Exception {Field f=PetService.class.getDeclaredField(name);f.setAccessible(true);f.set(target,value);}
}
