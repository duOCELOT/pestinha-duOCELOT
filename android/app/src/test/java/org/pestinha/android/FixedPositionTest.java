package org.pestinha.android;
import android.content.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.Config;
import java.lang.reflect.*;
import java.util.concurrent.atomic.AtomicReference;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE)
public class FixedPositionTest {
 PetService service;Context c;WindowManager.LayoutParams position;PetView pet;
 void set(String name,Object value) throws Exception {Field f=PetService.class.getDeclaredField(name);f.setAccessible(true);f.set(service,value);}
 Object get(String name) throws Exception {Field f=PetService.class.getDeclaredField(name);f.setAccessible(true);return f.get(service);}
 void call(String name,Class<?>[] types,Object... args) throws Exception {Method m=PetService.class.getDeclaredMethod(name,types);m.setAccessible(true);m.invoke(service,args);}
 @Before public void setup() throws Exception {c=RuntimeEnvironment.getApplication();c.getSharedPreferences("pet",0).edit().clear().commit();service=Robolectric.buildService(PetService.class).get();WindowManager wm=c.getSystemService(WindowManager.class);pet=new PetView(c);position=new WindowManager.LayoutParams();position.x=120;position.y=180;set("pos",position);set("pet",pet);set("wm",wm);set("world",new PetWorld(c,wm,value->{}));set("quickMenu",new QuickMenu(c,wm,value->{}));set("xFloat",120f);set("yFloat",180f);PetService.running=true;PetService.emotion="observando";}
 @After public void cleanup() throws Exception {((Handler)get("ui")).removeCallbacksAndMessages(null);PetService.running=false;}
 void frames() throws Exception {for(int i=0;i<10;i++){set("lastFrame",SystemClock.uptimeMillis()-33);((Runnable)get("frame")).run();}}
 @Test public void fixedPetStaysWhereDroppedAndLosesThrowMomentum() throws Exception {call("setFixed",new Class[]{boolean.class},true);position.x=235;position.y=315;set("vx",500f);set("vy",600f);frames();assertEquals(235,position.x);assertEquals(315,position.y);assertEquals(0f,(float)get("vx"),0);assertEquals(0f,(float)get("vy"),0);assertTrue(c.getSharedPreferences("pet",0).getBoolean("fixed",false));}
 @Test public void unpinningRestoresGravityAndClearsOldMomentum() throws Exception {call("setFixed",new Class[]{boolean.class},true);set("vx",900f);set("vy",800f);call("setFixed",new Class[]{boolean.class},false);assertEquals(0f,(float)get("vx"),0);assertEquals(0f,(float)get("vy"),0);frames();assertTrue(position.y>180);assertFalse(c.getSharedPreferences("pet",0).getBoolean("fixed",true));}
 @Test public void fixedPreferenceIsLoadedWhenServiceSettingsAreRestored() throws Exception {c.getSharedPreferences("pet",0).edit().putBoolean("fixed",true).commit();call("applySettings",new Class[0]);assertTrue((boolean)get("fixed"));frames();assertEquals(180,position.y);}
 @Test public void longPressMenuHasDirectToggleAndReflectsSavedState(){AtomicReference<String> chosen=new AtomicReference<>();QuickMenu menu=new QuickMenu(c,c.getSystemService(WindowManager.class),chosen::set);View pin=menu.content(false,false,false,4).findViewWithTag("pin");assertNotNull(pin);assertFalse(pin.isSelected());pin.performClick();assertEquals("pin",chosen.get());c.getSharedPreferences("pet",0).edit().putBoolean("fixed",true).commit();View unpin=menu.content(false,false,false,4).findViewWithTag("pin");assertTrue(unpin.isSelected());assertTrue(unpin.getContentDescription().toString().contains("Soltar posição"));}
}
