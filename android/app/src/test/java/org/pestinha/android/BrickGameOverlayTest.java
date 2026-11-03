package org.pestinha.android;
import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.*;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;
@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE,qualifiers="w360dp-h780dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class BrickGameOverlayTest {
 Context c;BrickGameOverlay game;FrameLayout root;PetView pet;int[] placement=new int[4];AtomicInteger closed=new AtomicInteger();
 @Before public void setup() throws Exception {c=RuntimeEnvironment.getApplication();c.getSharedPreferences("pet",0).edit().putString("character","mimir").commit();c.getSharedPreferences("arcade",0).edit().clear().commit();pet=new PetView(c);pet.appearance(Characters.definition(c,"mimir").getJSONObject("aparencia"));game=new BrickGameOverlay(c,c.getSystemService(WindowManager.class),pet,(x,y,w,h)->{placement[0]=x;placement[1]=y;placement[2]=w;placement[3]=h;},closed::incrementAndGet,value->{});root=game.content();game.root=root;root.measure(View.MeasureSpec.makeMeasureSpec(360,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(780,View.MeasureSpec.EXACTLY));root.layout(0,0,360,780);}
 @After public void cleanup(){game.close();PetService.playing=false;}
 void touch(int action,float x,float y){MotionEvent e=MotionEvent.obtain(0,0,action,x,y,0);game.board.onTouchEvent(e);e.recycle();}
 @Test public void draggingOnlyMovesPetHorizontallyAndReleaseLaunches(){int y=placement[1];touch(MotionEvent.ACTION_DOWN,280,600);touch(MotionEvent.ACTION_MOVE,75,200);assertEquals(y,placement[1]);assertEquals(75,game.model.paddleX,0);touch(MotionEvent.ACTION_UP,75,200);assertEquals(BrickGameModel.State.PLAYING,game.model.state);game.pause.performClick();assertEquals(BrickGameModel.State.PAUSED,game.model.state);}
 @Test public void screenOffPausesAndClosingIsIdempotentWithIndependentRecord(){game.model.launch();game.screen(false);assertEquals(BrickGameModel.State.PAUSED,game.model.state);game.screen(true);assertEquals(BrickGameModel.State.PAUSED,game.model.state);game.model.score=240;game.close();game.close();assertEquals(1,closed.get());assertFalse(game.isShowing());assertEquals(240,c.getSharedPreferences("arcade",0).getInt("best_mimir",0));assertEquals(0,c.getSharedPreferences("arcade",0).getInt("best_bryan",0));}
 @Test public void emptyPlayingFieldIsTransparentAndDoesNotTintTheAppBehind(){game.model.launch();Bitmap transparent=Bitmap.createBitmap(360,780,Bitmap.Config.ARGB_8888);root.draw(new Canvas(transparent));assertEquals(0,Color.alpha(transparent.getPixel(180,340)));assertEquals(0,Color.alpha(transparent.getPixel(1,400)));assertTrue(Color.alpha(transparent.getPixel(30,143))>0);Bitmap underneath=Bitmap.createBitmap(360,780,Bitmap.Config.ARGB_8888);int appColor=Color.rgb(230,238,245);underneath.eraseColor(appColor);root.draw(new Canvas(underneath));assertEquals(appColor,underneath.getPixel(180,340));assertEquals(appColor,underneath.getPixel(1,400));}
 @Test public void rendersBricksControlsAndTheActualPetAsPaddle(){Bitmap b=Bitmap.createBitmap(360,780,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);root.draw(canvas);pet.measure(View.MeasureSpec.makeMeasureSpec(placement[2],View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(placement[3],View.MeasureSpec.EXACTLY));pet.layout(0,0,placement[2],placement[3]);canvas.save();canvas.translate(placement[0],placement[1]);pet.draw(canvas);canvas.restore();assertNotNull(root.findViewWithTag("close_game"));assertEquals(28,game.model.remaining);ByteArrayOutputStream out=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,out);System.out.println("DESIGN_PREVIEW_ARCADE="+Base64.getEncoder().encodeToString(out.toByteArray()));}
}
