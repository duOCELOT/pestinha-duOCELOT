package org.pestinha.android;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import android.widget.LinearLayout;
import org.json.JSONObject;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
@Config(sdk=28,manifest=Config.NONE,qualifiers="w360dp-h780dp-mdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
public class DesignTest {
    private Context context;
    @Before public void setup(){context=RuntimeEnvironment.getApplication();context.getSharedPreferences("pet",0).edit().clear().commit();PetService.running=false;PetService.busy=false;org.robolectric.shadows.ShadowSystemClock.advanceBy(java.time.Duration.ofMillis(600));}
    private Bitmap draw(View view,int width,int height){view.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(height,View.MeasureSpec.EXACTLY));view.layout(0,0,width,height);Bitmap b=Bitmap.createBitmap(width,height,Bitmap.Config.ARGB_8888);view.draw(new Canvas(b));return b;}
    private void preview(String name,Bitmap bitmap){ByteArrayOutputStream out=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.PNG,100,out);System.out.println("DESIGN_PREVIEW_"+name+"="+Base64.getEncoder().encodeToString(out.toByteArray()));}
    @Test public void originalBryanFrameScreenAndTransparencyArePreserved(){PetView pet=new PetView(context);Bitmap b=draw(pet,320,260);assertEquals(Color.parseColor("#63e56f"),b.getPixel(40,130));assertEquals(Color.parseColor("#171e19"),b.getPixel(160,130));assertEquals(0,Color.alpha(b.getPixel(0,0)));preview("PET",b);}
    @Test public void jorgeUsesOriginalGoldAppearance() throws Exception {PetView pet=new PetView(context);pet.appearance(new JSONObject("{\"moldura\":\"#e2b13c\",\"casca\":\"#14120c\",\"tela\":\"#1c1a12\",\"olho\":\"#ffd166\"}"));Bitmap b=draw(pet,320,260);assertEquals(Color.parseColor("#e2b13c"),b.getPixel(40,130));assertEquals(Color.parseColor("#1c1a12"),b.getPixel(160,130));}
    @Test public void mimirHelmetAndBluePaletteRenderWithoutClipping() throws Exception {
        PetView pet=new PetView(context);pet.appearance(Characters.definition(context,"mimir").getJSONObject("aparencia"));
        Bitmap b=draw(pet,320,260);int blue=0,metal=0;
        for(int y=0;y<b.getHeight();y++)for(int x=0;x<b.getWidth();x++){if(b.getPixel(x,y)==Color.parseColor("#8bbcc7"))blue++;if(b.getPixel(x,y)==Color.parseColor("#77929c"))metal++;}
        assertTrue(blue>50);assertTrue(metal>50);preview("MIMIR",b);
        pet.bodyAngle=18;Bitmap tilted=draw(pet,320,260);for(int x=0;x<320;x++)assertEquals(0,Color.alpha(tilted.getPixel(x,0)));
    }
    @Test public void allThreeCharactersCanBeSelectedAndHaveIndependentMemoryPaths() throws Exception {
        assertArrayEquals(new String[]{"Bryan","Jorge","Mimir","Luna","L.U.L.A"},Characters.names(context));
        assertEquals(104729,Characters.definition(context,"mimir").getInt("seed"));
        context.getSharedPreferences("pet",0).edit().putString("character","mimir").commit();assertEquals("mimir",MemoryActivity.petId(context));
        java.io.File mimir=MemoryActivity.directory(context);
        context.getSharedPreferences("pet",0).edit().putString("character","bryan").commit();assertNotEquals(mimir,MemoryActivity.directory(context));
        assertEquals("bryan",Characters.normalize("../outside"));
    }
    @Test public void newPetsHaveDistinctAppearanceAndRender() throws Exception {for(String id:new String[]{"luna","lula"}){PetView v=new PetView(context);v.appearance(Characters.appearance(context,id));Bitmap b=draw(v,320,260);assertEquals(0,Color.alpha(b.getPixel(0,0)));preview(id.toUpperCase(java.util.Locale.ROOT),b);}assertNotEquals(Characters.definition(context,"luna").getInt("seed"),Characters.definition(context,"lula").getInt("seed"));}
    @Test public void menuRoutesActionsWithoutOpeningThePanel(){AtomicReference<String> selected=new AtomicReference<>();QuickMenu menu=new QuickMenu(context,context.getSystemService(WindowManager.class),selected::set);LinearLayout content=menu.content(false,false,false,4);View heart=content.findViewWithTag("caress");assertNotNull(heart);heart.performClick();assertEquals("caress",selected.get());assertFalse(menu.isShowing());preview("MENU",draw(content,280,390));}
    @Test public void menuPlacementFitsAllPhoneCorners(){for(int x:new int[]{0,200,330})for(int y:new int[]{0,390,700}){int[] p=QuickMenu.place(360,780,280,390,x,y,100,90,12);assertTrue(p[0]>=0);assertTrue(p[1]>=0);assertTrue(p[0]+280<=360);assertTrue(p[1]+390<=780);}}
    @Test public void mainScreenHasRealNavigationAndRenders(){Activity activity=Robolectric.buildActivity(MainActivity.class).create().get();View content=((android.view.ViewGroup)activity.findViewById(android.R.id.content)).getChildAt(0);preview("HOME",draw(content,360,780));assertNotNull(content.findViewWithTag("primary"));activity.finish();}
}
