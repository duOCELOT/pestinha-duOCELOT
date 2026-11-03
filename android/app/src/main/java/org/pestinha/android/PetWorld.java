package org.pestinha.android;

import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.os.*;
import android.view.*;
import android.widget.*;
import java.io.File;

/** Small independent overlay windows; touches outside each object pass through. */
final class PetWorld {
    interface Events { void event(String value); }
    final Context context; final WindowManager wm; final Events events; final Handler ui=new Handler(Looper.getMainLooper());
    final float density; View ball,bubble,sparks; WindowManager.LayoutParams ballPos,bubblePos;
    float bx,by,bvx=130,bvy,forceX=0,forceY=750;boolean tilted=false; boolean held=false,visible=true; long lastNear=0; int width,height;
    PetWorld(Context c,WindowManager w,Events e){context=c;wm=w;events=e;density=c.getResources().getDisplayMetrics().density;}
    int dp(float n){return Math.round(n*density);}
    WindowManager.LayoutParams params(int w,int h,boolean touchable){
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(dp(w),dp(h),WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|(touchable?0:WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE),PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.LEFT; return p;
    }
    void bounds(int w,int h){width=w;height=h;}
    void toggleBall(int x,int y){if(ball!=null){remove(ball);ball=null;events.event("toy_taken");return;}
        ballPos=params(46,46,true); bx=x;by=y; ballPos.x=x;ballPos.y=y;
        ball=new View(context){final Paint p=new Paint(3); protected void onDraw(Canvas c){
            p.setColor(Color.rgb(192,224,55));c.drawCircle(getWidth()/2f,getHeight()/2f,getWidth()*.43f,p);
            p.setColor(Color.WHITE);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(2));
            c.drawArc(getWidth()*.07f,-getHeight()*.1f,getWidth()*.6f,getHeight()*1.1f,-75,150,false,p);p.setStyle(Paint.Style.FILL);
        }};
        ball.setOnTouchListener(new View.OnTouchListener(){float rawX,rawY,startX,startY; public boolean onTouch(View v,MotionEvent e){
            if(e.getActionMasked()==MotionEvent.ACTION_DOWN){held=true;rawX=e.getRawX();rawY=e.getRawY();startX=bx;startY=by;events.event("toy_taken");}
            if(e.getActionMasked()==MotionEvent.ACTION_MOVE){bx=startX+e.getRawX()-rawX;by=startY+e.getRawY()-rawY;limitBall();}
            if(e.getActionMasked()==MotionEvent.ACTION_UP||e.getActionMasked()==MotionEvent.ACTION_CANCEL){held=false;bvx=(float)((Math.random()-.5)*dp(450));bvy=-dp(260);}
            return true;
        }});
        wm.addView(ball,ballPos);ball.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
    }
    void limitBall(){bx=Math.max(0,Math.min(Math.max(0,width-dp(46)),bx));by=Math.max(0,Math.min(Math.max(0,height-dp(80)),by));ballPos.x=Math.round(bx);ballPos.y=Math.round(by);if(ball!=null)wm.updateViewLayout(ball,ballPos);}
    void gravity(boolean enabled,float x,float y){tilted=enabled;forceX=enabled?x:0;forceY=enabled?y:750;}
    void frame(float dt,int petX,int petY,int petW,int petH){
        if(ball==null)return;
        if(!held){
            if(tilted){bvx=TiltPhysics.velocity(bvx,forceX*density,dt,dp(650));bvy=TiltPhysics.velocity(bvy,forceY*density,dt,dp(650));}
            else bvy+=dp(750)*dt;
            bx+=bvx*dt;by+=bvy*dt;
            if(by<0)bvy=Math.abs(bvy)*.55f;
            if(bx<0||bx>width-dp(46))bvx=-bvx*.8f;
            if(by>height-dp(80)){bvy=-Math.abs(bvy)*.55f;bvx*=.96f;}
            limitBall();
            if(Math.hypot(bx-(petX+petW/2f),by-(petY+petH/2f))<dp(95)&&SystemClock.uptimeMillis()-lastNear>1000){
                lastNear=SystemClock.uptimeMillis();events.event("toy_near");bvx=bx>petX?dp(140):-dp(140);bvy=-dp(190);
            }
        }
    }
    void message(String text,boolean image,int x,int y,Runnable dismissed){messageAction(text,image,x,y,dismissed,null,null);}
    void messageAction(String text,boolean image,int x,int y,Runnable dismissed,String label,Runnable open){
        clearMessage();
        bubblePos=params(image?220:240,image?190:100,true);bubblePos.x=Math.max(0,Math.min(width-bubblePos.width,x));bubblePos.y=Math.max(0,Math.min(height-bubblePos.height,y-dp(105)));
        GradientDrawable bg=new GradientDrawable();bg.setColor(Color.rgb(255,241,176));bg.setCornerRadius(dp(12));
        if(image){ImageView view=new ImageView(context); view.setScaleType(ImageView.ScaleType.FIT_CENTER);
            File f=new File(MemoryActivity.directory(context),"custom-meme.png");view.setImageBitmap(BitmapFactory.decodeFile(f.toString()));bubble=view;
        }else{ TextView view=new TextView(context);view.setText(text+"\n\nToque para dispensar");view.setTextColor(Color.rgb(35,32,20));view.setTextSize(15);view.setGravity(Gravity.CENTER);view.setPadding(dp(12),dp(6),dp(12),dp(6));bubble=view; }
        if(open!=null){LinearLayout box=Design.column(context);box.addView(bubble);Button read=new Button(context);read.setText(label);read.setTag("message_action");box.addView(read);read.setOnClickListener(v->{clearMessage();open.run();});Button close=new Button(context);close.setText("Fechar");close.setTag("message_close");box.addView(close);close.setOnClickListener(v->clearMessage());bubble=box;}
        bubble.setBackground(bg);
        bubblePos.width=Math.min(bubblePos.width,Math.max(dp(100),width-dp(20)));
        if(!image){bubble.measure(View.MeasureSpec.makeMeasureSpec(bubblePos.width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(Math.max(dp(80),height-dp(24)),View.MeasureSpec.AT_MOST));bubblePos.height=bubble.getMeasuredHeight();}
        bubblePos.x=Math.max(0,Math.min(width-bubblePos.width,x));
        int above=y-bubblePos.height-dp(8);bubblePos.y=Math.max(0,Math.min(height-bubblePos.height,above>=0?above:y+dp(90)));
        bubble.setOnClickListener(v->{dismissed.run();clearMessage();});
        wm.addView(bubble,bubblePos);bubble.setVisibility(visible?View.VISIBLE:View.INVISIBLE);
        ui.postDelayed(this::clearMessage,19000);
    }
    void clearMessage(){ui.removeCallbacksAndMessages(null);if(bubble!=null){remove(bubble);bubble=null;}if(sparks!=null){remove(sparks);sparks=null;}}
    void fireworks(int x,int y){
        if(sparks!=null)return;
        WindowManager.LayoutParams p=params(180,180,false);p.x=Math.max(0,x);p.y=Math.max(0,y-dp(100));
        long start=SystemClock.uptimeMillis();
        sparks=new View(context){final Paint paint=new Paint(3);protected void onDraw(Canvas c){
            float t=(SystemClock.uptimeMillis()-start)/1500f; paint.setStrokeWidth(dp(3));
            for(int i=0;i<20;i++){double a=i*Math.PI/10;paint.setColor(Color.HSVToColor(new float[]{i*18, .65f,1}));
                float r=dp(65)*t;c.drawLine(getWidth()/2f+(float)Math.cos(a)*r,getHeight()/2f+(float)Math.sin(a)*r,
                getWidth()/2f+(float)Math.cos(a)*(r+dp(9)),getHeight()/2f+(float)Math.sin(a)*(r+dp(9)),paint);}
            if(t<1)postInvalidateDelayed(33);
        }};wm.addView(sparks,p);ui.postDelayed(()->{if(sparks!=null){remove(sparks);sparks=null;}},1600);
    }
    void show(boolean show){visible=show;for(View v:new View[]{ball,bubble,sparks})if(v!=null)v.setVisibility(show?View.VISIBLE:View.INVISIBLE);}
    void remove(View v){if(v.isAttachedToWindow())wm.removeView(v);}
    void close(){ui.removeCallbacksAndMessages(null);for(View v:new View[]{ball,bubble,sparks})if(v!=null)remove(v);ball=bubble=sparks=null;}
}
