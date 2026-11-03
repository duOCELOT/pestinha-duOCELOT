package org.pestinha.android;

import android.content.*;
import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.*;
import org.json.JSONObject;
import java.util.*;

/** A single touchable game overlay; the original floating PetView remains the paddle. */
final class BrickGameOverlay {
    interface Placement {void move(int x,int y,int width,int height);}
    interface Events {void event(String value);}
    final Context context;final WindowManager wm;final PetView pet;final Placement placement;final Runnable onClose;final Events events;
    FrameLayout root;GameView board;Button pause;BrickGameModel model;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private boolean screenOn=true,closed=false;private long previous,reactUntil;private String reaction="";
    private float scale=1,left=0;private int best,accent=Design.ACCENT;private final String character;
    private final ArrayList<Particle> particles=new ArrayList<>();private final float[] trailX=new float[10],trailY=new float[10];private int trailCount;
    BrickGameOverlay(Context c,WindowManager manager,PetView view,Placement move,Runnable close,Events callback){context=c;wm=manager;pet=view;placement=move;onClose=close;events=callback;character=MemoryActivity.petId(c);best=c.getSharedPreferences("arcade",0).getInt("best_"+character,0);try{accent=Color.parseColor(Characters.appearance(c,character).getString("moldura"));}catch(Exception ignored){}}
    boolean isShowing(){return root!=null&&!closed;}
    FrameLayout content(){
        FrameLayout frame=new FrameLayout(context);board=new GameView(context);frame.addView(board,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout header=new LinearLayout(context);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(Design.dp(context,10),0,Design.dp(context,8),0);TextView title=Design.text(context,"PESTINHA / ARCADE",12,accent);title.setBackground(Design.shape(context,0xdf101512,12,0));title.setPadding(Design.dp(context,8),Design.dp(context,6),Design.dp(context,8),Design.dp(context,6));header.addView(title,new LinearLayout.LayoutParams(0,-2,1));
        pause=new Button(context);pause.setText("Pausar");pause.setContentDescription("Pausar ou continuar jogo");pause.setTag("secondary");Design.styleButton(pause);header.addView(pause,new LinearLayout.LayoutParams(Design.dp(context,86),Design.dp(context,48)));pause.setOnClickListener(v->{if(model==null)return;if(model.state==BrickGameModel.State.PAUSED)model.resume();else model.pause();previous=SystemClock.uptimeMillis();updateControls();board.invalidate();});
        ActionIcon close=new ActionIcon(context,"close");close.setContentDescription("Fechar Quebra-blocos e voltar ao pet");close.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);close.setBackground(Design.touch(context,Design.CARD,0));header.addView(close,new LinearLayout.LayoutParams(Design.dp(context,48),Design.dp(context,48)));close.setTag("close_game");close.setOnClickListener(v->close());frame.addView(header,new FrameLayout.LayoutParams(-1,Design.dp(context,56),Gravity.TOP));return frame;
    }
    boolean show(){if(isShowing())return true;closed=false;root=content();WindowManager.LayoutParams p=new WindowManager.LayoutParams(-1,-1,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,PixelFormat.TRANSLUCENT);p.gravity=Gravity.TOP|Gravity.LEFT;
        try{wm.addView(root,p);previous=SystemClock.uptimeMillis();ui.post(loop);return true;}catch(RuntimeException error){root=null;return false;}}
    private void geometry(int width,int height){if(width<=0||height<=0)return;scale=Math.min(width/360f,height/520f);left=(width-360*scale)/2;float h=height/scale;if(model==null)model=new BrickGameModel(h,this::react);else model.resize(h);particles.clear();trailCount=0;place();updateControls();}
    private void place(){if(model==null)return;placement.move(Math.round(left+(model.paddleX-60)*scale),Math.round((model.paddleY-15)*scale),Math.round(120*scale),Math.round(98*scale));}
    private void updateControls(){if(pause==null||model==null)return;pause.setEnabled(model.state==BrickGameModel.State.READY||model.state==BrickGameModel.State.PLAYING||model.state==BrickGameModel.State.PAUSED);pause.setText(model.state==BrickGameModel.State.PAUSED?"Continuar":"Pausar");board.setContentDescription("Quebra-blocos. "+model.score+" pontos, "+model.lives+" vidas. Arraste o pet para os lados. Toque para lançar ou continuar.");}
    private void saveBest(){if(model!=null&&model.score>best){best=model.score;context.getSharedPreferences("arcade",0).edit().putInt("best_"+character,best).apply();}}
    private void react(String event,float x,float y){saveBest();if(event.equals("hit")||event.equals("life")||event.equals("win")||event.equals("over")){reaction=event;reactUntil=SystemClock.uptimeMillis()+(event.equals("hit")?400:950);}if(event.equals("brick")||event.equals("win")){int count=event.equals("win")?45:12;for(int i=0;i<count;i++){double a=i*Math.PI*2/count;particles.add(new Particle(x,y,(float)Math.cos(a)*(35+i%4*25),(float)Math.sin(a)*95,accent));}while(particles.size()>130)particles.remove(0);}if(event.equals("life")||event.equals("over"))trailCount=0;if(event.equals("restart")){reaction="";reactUntil=0;particles.clear();trailCount=0;}events.event(event);updateControls();}
    private final Runnable loop=new Runnable(){public void run(){if(!isShowing()||!screenOn)return;long now=SystemClock.uptimeMillis();float dt=Math.min(.05f,Math.max(0,(now-previous)/1000f));previous=now;
        if(!UsageRepository.interactive(context)){if(model!=null)model.pause();root.setVisibility(View.INVISIBLE);pet.setVisibility(View.INVISIBLE);ui.postDelayed(this,250);return;}root.setVisibility(View.VISIBLE);pet.setVisibility(View.VISIBLE);
        if(model!=null){model.tick(dt);place();if(model.state==BrickGameModel.State.PLAYING){for(int i=trailX.length-1;i>0;i--){trailX[i]=trailX[i-1];trailY[i]=trailY[i-1];}trailX[0]=model.ballX;trailY[0]=model.ballY;trailCount=Math.min(trailX.length,trailCount+1);}for(Iterator<Particle> i=particles.iterator();i.hasNext();){Particle p=i.next();if(model.state!=BrickGameModel.State.PAUSED){p.life-=dt;p.x+=p.vx*dt;p.y+=p.vy*dt;p.vy+=90*dt;}if(p.life<=0)i.remove();}pose(now);board.invalidate();}
        ui.postDelayed(this,model!=null&&model.state==BrickGameModel.State.PAUSED?100:16);
    }};
    private void pose(long now){boolean active=now<reactUntil;pet.notebook=pet.corner=false;pet.emotion=active&&reaction.equals("life")?"assustado":active&&reaction.equals("over")?"triste":"feliz";pet.bodyAngle=active&&reaction.equals("life")?(float)Math.sin(now*.055)*9:0;pet.setScaleX(active&&reaction.equals("hit")?1.08f:1);pet.setScaleY(active&&reaction.equals("hit")?.9f:1);try{pet.face(new JSONObject().put("w",30).put("h",active&&reaction.equals("life")?46:35).put("inner",active&&reaction.equals("over")?.4:.1).put("outer",.05).put("smile",active&&(reaction.equals("hit")||reaction.equals("win"))?.5:0),active&&reaction.equals("hit"));}catch(Exception ignored){}pet.invalidate();}
    boolean touch(MotionEvent event){if(board==null)return true;int[] origin=new int[2];board.getLocationOnScreen(origin);return board.input(event,event.getRawX()-origin[0],event.getRawY()-origin[1]);}
    void screen(boolean on){screenOn=on;if(model!=null)model.pause();ui.removeCallbacks(loop);if(root!=null)root.setVisibility(on?View.VISIBLE:View.INVISIBLE);previous=SystemClock.uptimeMillis();if(on&&isShowing())ui.post(loop);updateControls();}
    void resized(){if(model!=null)model.pause();if(root!=null)root.requestLayout();}
    void close(){if(closed)return;closed=true;saveBest();ui.removeCallbacksAndMessages(null);View old=root;root=null;if(old!=null&&old.isAttachedToWindow())wm.removeView(old);pet.setScaleX(1);pet.setScaleY(1);pet.bodyAngle=0;particles.clear();onClose.run();}
    private static class Particle {float x,y,vx,vy,life=.7f;final int color;Particle(float a,float b,float c,float d,int tint){x=a;y=b;vx=c;vy=d;color=tint;}}
    final class GameView extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);private int pointer=-1;
        GameView(Context c){super(c);setFocusable(true);setClickable(true);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
        @Override protected void onSizeChanged(int w,int h,int ow,int oh){geometry(w,h);}
        @Override protected void onDraw(Canvas canvas){super.onDraw(canvas);if(model==null)return;canvas.save();canvas.translate(left,0);canvas.scale(scale,scale);p.setColor(0xdf101512);canvas.drawRoundRect(9,66,144,87,7,7,p);canvas.drawRoundRect(264,66,351,87,7,7,p);canvas.drawRoundRect(9,89,234,105,6,6,p);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));p.setTextSize(12);p.setColor(Design.TEXT);canvas.drawText("PONTOS  "+model.score,14,82,p);p.setColor(accent);p.setTextAlign(Paint.Align.RIGHT);canvas.drawText("VIDAS  "+model.lives,346,82,p);p.setTextAlign(Paint.Align.LEFT);p.setTextSize(10);p.setColor(Design.MUTED);canvas.drawText("FASE "+model.level+"   •   RECORDE "+best,14,100,p);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(Design.BORDER);canvas.drawRoundRect(6,110,354,model.height-8,18,18,p);p.setStyle(Paint.Style.FILL);
            int[] colors={accent,0xffe7c76e,0xff78cdda,0xffb2a3e7};for(int i=0;i<model.bricks.length;i++)if(model.bricks[i]){float x=model.brickX(i),y=model.brickY(i);p.setColor(colors[i/BrickGameModel.COLUMNS]);canvas.drawRoundRect(x,y,x+model.brickW(),y+model.brickH(),4,4,p);p.setColor(0x44ffffff);canvas.drawRoundRect(x+3,y+2,x+model.brickW()-3,y+4,1,1,p);}
            for(int i=trailCount-1;i>=0;i--){p.setColor(accent);p.setAlpha((int)(80*(1-i/10f)));canvas.drawCircle(trailX[i],trailY[i],Math.max(1,4-i*.3f),p);}p.setAlpha(255);p.setColor(Design.TEXT);canvas.drawCircle(model.ballX,model.ballY,BrickGameModel.RADIUS,p);
            for(Particle particle:particles){p.setColor(particle.color);p.setAlpha((int)(255*Math.max(0,particle.life/.7f)));canvas.drawCircle(particle.x,particle.y,2.2f,p);}p.setAlpha(255);
            if(model.state!=BrickGameModel.State.PLAYING){String title=model.state==BrickGameModel.State.READY?"Você é meu copiloto!":model.state==BrickGameModel.State.PAUSED?"Uma pausa no caos":model.state==BrickGameModel.State.WON?"Fase limpa!":"Fim de jogo";String subtitle=model.state==BrickGameModel.State.READY?"Arraste o pet para os lados":model.state==BrickGameModel.State.PAUSED?"Toque para continuar":model.state==BrickGameModel.State.WON?"Toque para a próxima fase":"Toque para jogar de novo";float y=Math.max(250,(model.paddleY+225)/2);p.setColor(0xf2171e19);canvas.drawRoundRect(27,y-49,333,y+48,18,18,p);p.setTextAlign(Paint.Align.CENTER);p.setColor(accent);p.setTextSize(21);canvas.drawText(title,180,y-13,p);p.setTextSize(13);p.setColor(Design.TEXT);canvas.drawText(subtitle,180,y+12,p);if(model.state==BrickGameModel.State.READY){p.setTextSize(11);p.setColor(Design.MUTED);canvas.drawText("Solte o dedo para lançar a bola",180,y+31,p);}p.setTextAlign(Paint.Align.LEFT);}
            canvas.restore();
        }
        boolean input(MotionEvent e,float x,float y){if(model==null)return true;int action=e.getActionMasked();if(action==MotionEvent.ACTION_DOWN){pointer=e.getPointerId(0);if(y>Design.dp(context,56))model.move((x-left)/scale);}else if(action==MotionEvent.ACTION_MOVE&&pointer!=-1){int index=e.findPointerIndex(pointer);if(index>=0){float delta=e.getX(index)-e.getX(0);model.move((x+delta-left)/scale);}}else if(action==MotionEvent.ACTION_POINTER_UP&&e.getPointerId(e.getActionIndex())==pointer)pointer=-1;else if(action==MotionEvent.ACTION_UP){if(pointer!=-1){pointer=-1;performClick();}}else if(action==MotionEvent.ACTION_CANCEL)pointer=-1;place();invalidate();return true;}
        @Override public boolean onTouchEvent(MotionEvent event){return input(event,event.getX(),event.getY());}
        @Override public boolean performClick(){super.performClick();if(model==null)return true;switch(model.state){case READY:model.launch();break;case PAUSED:model.resume();break;case WON:model.next();trailCount=0;break;case OVER:model.restart();break;default:break;}previous=SystemClock.uptimeMillis();updateControls();invalidate();return true;}
        @Override public boolean performAccessibilityAction(int action,android.os.Bundle args){if(model!=null&&(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD||action==AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD)){model.move(model.paddleX+(action==AccessibilityNodeInfo.ACTION_SCROLL_FORWARD?30:-30));place();invalidate();return true;}return super.performAccessibilityAction(action,args);}
        @Override public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info){super.onInitializeAccessibilityNodeInfo(info);info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);}
    }
}
