package org.pestinha.android;

/** Deterministic, Android-independent arcade simulation in a 360-unit-wide field. */
public final class ArkanoidModel {
    public enum State { READY, PLAYING, PAUSED, WON, OVER }
    public interface Events { void event(String kind,float x,float y); }
    public static final float WIDTH=360,RADIUS=5,PADDLE_WIDTH=90,TOP=110;
    public static final int COLUMNS=7,ROWS=4;
    public final boolean[] bricks=new boolean[COLUMNS*ROWS];
    public float height,paddleX=180,paddleY,ballX,ballY,vx,vy;
    public int score=0,lives=3,level=1,remaining;
    public State state=State.READY;
    private State beforePause=State.READY;
    private final Events events;
    public ArkanoidModel(float h,Events callback){events=callback;resize(h);fill();attach();}
    public float brickX(int i){return 14+(i%COLUMNS)*48;}
    public float brickY(int i){return 134+(i/COLUMNS)*25;}
    public float brickW(){return 44;}
    public float brickH(){return 18;}
    private float speed(){return Math.min(320,205+(level-1)*22);}
    private void emit(String kind,float x,float y){if(events!=null)events.event(kind,x,y);}
    private void fill(){java.util.Arrays.fill(bricks,true);remaining=bricks.length;}
    private void attach(){ballX=paddleX;ballY=paddleY-RADIUS-3;vx=vy=0;}
    public void resize(float h){height=Math.max(520,h);paddleY=height-90;ballY=Math.max(TOP+RADIUS,Math.min(ballY,paddleY-RADIUS-3));move(paddleX);if(state==State.PLAYING)pause();}
    public void move(float x){if(!Float.isFinite(x))return;paddleX=Math.max(10+PADDLE_WIDTH/2,Math.min(WIDTH-10-PADDLE_WIDTH/2,x));if(state==State.READY||(state==State.PAUSED&&beforePause==State.READY))attach();}
    public void launch(){if(state!=State.READY)return;state=State.PLAYING;vx=speed()*.3f;vy=-(float)Math.sqrt(speed()*speed()-vx*vx);emit("launch",ballX,ballY);}
    public void pause(){if(state==State.PLAYING||state==State.READY){beforePause=state;state=State.PAUSED;}}
    public void resume(){if(state==State.PAUSED)state=beforePause;}
    public void next(){if(state!=State.WON)return;level++;fill();state=State.READY;attach();}
    public void restart(){score=0;lives=3;level=1;fill();state=State.READY;attach();emit("restart",paddleX,paddleY);}
    public void tick(float seconds){if(state!=State.PLAYING||!Float.isFinite(seconds))return;float left=Math.max(0,Math.min(.05f,seconds));while(left>0&&state==State.PLAYING){float dt=Math.min(1f/240,left);step(dt);left-=dt;}}
    private void step(float dt){float oldX=ballX,oldY=ballY;ballX+=vx*dt;ballY+=vy*dt;
        if(ballX<RADIUS+7){ballX=RADIUS+7;vx=Math.abs(vx);}if(ballX>WIDTH-7-RADIUS){ballX=WIDTH-7-RADIUS;vx=-Math.abs(vx);}if(ballY<TOP+RADIUS){ballY=TOP+RADIUS;vy=Math.abs(vy);}
        if(vy>0&&oldY+RADIUS<=paddleY+1&&ballY+RADIUS>=paddleY&&Math.abs(ballX-paddleX)<=PADDLE_WIDTH/2+RADIUS){float hit=Math.max(-1,Math.min(1,(ballX-paddleX)/(PADDLE_WIDTH/2)));float speed=speed();float previous=vx;vx=(float)Math.sin(hit*1.05)*speed;if(Math.abs(vx)<speed*.12f)vx=(previous<0?-1:1)*speed*.12f;vy=-(float)Math.sqrt(speed*speed-vx*vx);ballY=paddleY-RADIUS-.1f;emit("hit",ballX,paddleY);}
        for(int i=0;i<bricks.length;i++)if(bricks[i]){float x=brickX(i),y=brickY(i),nearX=Math.max(x,Math.min(x+brickW(),ballX)),nearY=Math.max(y,Math.min(y+brickH(),ballY));if((ballX-nearX)*(ballX-nearX)+(ballY-nearY)*(ballY-nearY)<=RADIUS*RADIUS){
            if(oldY+RADIUS<=y||oldY-RADIUS>=y+brickH())vy=-vy;else if(oldX+RADIUS<=x||oldX-RADIUS>=x+brickW())vx=-vx;else vy=-vy;
            ballX=oldX;ballY=oldY;bricks[i]=false;remaining--;score+=(ROWS-i/COLUMNS)*10;emit("brick",x+brickW()/2,y+brickH()/2);if(remaining==0){state=State.WON;emit("win",180,220);}break;
        }}
        if(ballY-RADIUS>height){lives--;emit("life",paddleX,paddleY);if(lives<=0){state=State.OVER;emit("over",paddleX,paddleY);}else {state=State.READY;attach();}}
    }
}
