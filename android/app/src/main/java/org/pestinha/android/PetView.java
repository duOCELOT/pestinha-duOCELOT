package org.pestinha.android;

import android.content.Context;
import android.graphics.*;
import android.os.SystemClock;
import android.view.View;
import org.json.JSONObject;

/** Native port of pestinha/body/desenho_rosto.py: same body, palette and eye cuts. */
public class PetView extends View {
    private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
    public String emotion="observando";
    public float bodyAngle=0;
    public boolean jorge=false,notebook=false,corner=false,caressing=false,autoAnimate=false;
    private int frame=Color.parseColor("#63e56f"),shell=Color.parseColor("#101512"),screen=Color.parseColor("#171e19"),eye=Color.parseColor("#3ee877"),shine=Color.parseColor("#c8ffd8");
    private boolean lulaStyle=false;
    private Bitmap detailed;
    private String artId="bryan";
    private boolean detailedAccessories;
    private int tie=0,glasses=0,helmet=0,bow=0,beard=0,sash=0;
    private final float[] shape={30,40,.14f,0,0,0};
    private float[] target={30,40,.14f,0,0,0};
    private long previous=0;
    public PetView(Context c){super(c);setLayerType(View.LAYER_TYPE_SOFTWARE,null);setContentDescription("Pestinha");detailed=PetArt.load(c,"bryan",frame);}
    public void appearance(JSONObject data){
        frame=Color.parseColor("#63e56f");shell=Color.parseColor("#101512");screen=Color.parseColor("#171e19");eye=Color.parseColor("#3ee877");shine=Color.parseColor("#c8ffd8");tie=glasses=helmet=bow=beard=sash=0;lulaStyle=false;
        if(data!=null){lulaStyle="lula".equals(data.optString("estilo"));frame=color(data,"moldura",frame);shell=color(data,"casca",shell);screen=color(data,"tela",screen);eye=color(data,"olho",eye);shine=color(data,"brilho",shine);JSONObject accessories=data.optJSONObject("acessorios");if(accessories!=null){if(accessories.has("faixa"))sash=color(accessories,"faixa",Color.GREEN);if(accessories.has("laco"))bow=color(accessories,"laco",Color.MAGENTA);if(accessories.has("barba"))beard=color(accessories,"barba",Color.WHITE);if(accessories.has("gravata"))tie=color(accessories,"gravata",Color.parseColor("#b3261e"));if(accessories.has("oculos"))glasses=color(accessories,"oculos",Color.LTGRAY);if(accessories.has("elmo_viking"))helmet=color(accessories,"elmo_viking",Color.rgb(119,146,156));}}artId=data==null?"bryan":data.optString("arte",lulaStyle?"lula":helmet!=0?"mimir":bow!=0?"luna":tie!=0?"jorge":"bryan");detailedAccessories=!artId.equals("bryan");detailed=PetArt.load(getContext(),artId,frame);invalidate();
    }
    private int color(JSONObject d,String key,int fallback){try{String value=d.optString(key);return value.matches("#[0-9a-fA-F]{6}")?Color.parseColor(value):fallback;}catch(Exception e){return fallback;}}
    public void face(JSONObject data,boolean caring){caressing=caring;if(data!=null)target=new float[]{(float)data.optDouble("w",30),(float)data.optDouble("h",40),(float)data.optDouble("inner",.14),(float)data.optDouble("outer",0),(float)data.optDouble("smile",0),(float)data.optDouble("dy",0)};else target=new float[]{30,40,.14f,0,0,0};}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);long now=SystemClock.uptimeMillis();float dt=previous==0?.033f:Math.min(.1f,(now-previous)/1000f);previous=now;
        float k=1-(float)Math.exp(-dt/.12);for(int i=0;i<shape.length;i++)shape[i]+=(target[i]-shape[i])*k;
        float scale=Math.min(getWidth()/160f,getHeight()/130f);canvas.save();canvas.translate(getWidth()/2f,getHeight()/2f);canvas.scale(scale,scale);
        if(helmet!=0&&detailed==null){canvas.translate(0,4);canvas.scale(.78f,.78f);}
        if(detailed!=null)canvas.scale(.84f,.84f);
        canvas.rotate(bodyAngle);if(emotion.equals("dormindo"))canvas.translate(0,(float)Math.sin(now/1000.0*1.6)*1.5f);
        if(detailed!=null){p.setShader(null);p.setAlpha(255);canvas.drawBitmap(detailed,null,new RectF(-65,-65,65,65),p);}
        else {
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(-60,-47,60,47,new int[]{mix(frame,Color.WHITE,.65f),frame,shade(frame,.53f)},new float[]{0,.35f,1},Shader.TileMode.CLAMP));canvas.drawRoundRect(-62,-47,62,47,22,22,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1);p.setColor(mix(frame,Color.WHITE,.75f));canvas.drawRoundRect(-60,-45,60,45,20,20,p);
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,-39,0,39,new int[]{Color.BLACK,shell,screen},new float[]{0,.45f,1},Shader.TileMode.CLAMP));canvas.drawRoundRect(-53,-38,53,38,17,17,p);p.setShader(null);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.2f);p.setColor(shade(frame,.35f));canvas.drawRoundRect(-53,-38,53,38,17,17,p);p.setStyle(Paint.Style.FILL);
        p.setShader(new LinearGradient(0,-45,0,-25,new int[]{0x88ffffff,0x00ffffff},null,Shader.TileMode.CLAMP));canvas.drawRoundRect(-53,-44,53,-36,7,7,p);p.setShader(null);
        if(bow!=0)drawHair(canvas);
        if(lulaStyle){p.setColor(0xffe7b090);canvas.drawOval(-71,-7,-58,19,p);Path ear=new Path();ear.moveTo(59,-6);ear.cubicTo(79,-14,76,6,66,17);ear.quadTo(58,21,59,-6);canvas.drawPath(ear,p);p.setColor(0xffa66c57);p.setStrokeWidth(1.5f);canvas.drawArc(-68,-3,-60,13,110,200,false,p);canvas.drawArc(62,-5,72,10,200,190,false,p);}
        if(lulaStyle){p.setColor(Color.rgb(208,211,208));Path hair=new Path();hair.moveTo(-59,-19);hair.cubicTo(-66,-47,-46,-58,-27,-49);hair.lineTo(-40,-40);hair.quadTo(-55,-39,-54,-17);hair.close();canvas.drawPath(hair,p);canvas.save();canvas.scale(-1,1);canvas.drawPath(hair,p);canvas.restore();p.setColor(Color.rgb(241,240,231));p.setStrokeWidth(2);p.setStyle(Paint.Style.STROKE);canvas.drawArc(-59,-47,-33,-23,190,100,false,p);canvas.drawArc(33,-47,59,-23,250,100,false,p);p.setStyle(Paint.Style.FILL);}
        if(lulaStyle){p.setColor(0xffe5e5e0);Path forelock=new Path();forelock.moveTo(-43,-45);forelock.cubicTo(-25,-65,-8,-53,6,-54);forelock.quadTo(23,-63,37,-50);forelock.quadTo(10,-46,-5,-44);forelock.quadTo(-22,-55,-43,-45);canvas.drawPath(forelock,p);p.setColor(0xfffaf9f4);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);canvas.drawArc(-33,-59,27,-42,195,145,false,p);p.setStyle(Paint.Style.FILL);}
        }
        if(tie!=0&&!detailedAccessories)drawTie(canvas);
        float blink=now%4800<110?.08f:1;
        for(int side:new int[]{-1,1}){
            float w=shape[0]*(detailed!=null?.78f:lulaStyle?.9f:1),h=Math.max(3,shape[1]*blink*(detailed!=null?.8f:lulaStyle?.9f:1)),cx=side*(detailed!=null?18:24),cy=shape[5]+(detailed==null?0:artId.equals("lula")?-8:artId.equals("jorge")?-8:artId.equals("mimir")?9:artId.equals("luna")?7:0);if(emotion.equals("curioso")&&side==1)h*=1.15f;
            if(caressing){p.setStyle(Paint.Style.STROKE);p.setColor(eye);p.setStrokeWidth(5);p.setStrokeCap(Paint.Cap.ROUND);canvas.drawArc(cx-15,cy-8,cx+15,cy+14,180,180,false,p);p.setStyle(Paint.Style.FILL);continue;}
            float top=cy-h/2;Path eyePath=new Path();eyePath.addRoundRect(cx-w/2,top,cx+w/2,top+h,Math.min(w,h)*.38f,Math.min(w,h)*.38f,Path.Direction.CW);
            if(blink>.5f){float outer=cx+side*w/2,inner=cx-side*w/2;Path lid=new Path();lid.moveTo(outer+side*4,top-40);lid.lineTo(inner-side*4,top-40);lid.lineTo(inner-side*4,top+shape[2]*h);lid.lineTo(outer+side*4,top+shape[3]*h);lid.close();eyePath.op(lid,Path.Op.DIFFERENCE);
                if(shape[4]>.02f){Path cut=new Path();float ch=h*shape[4]*1.6f;cut.addOval(cx-w*.75f,top+h-ch*.45f,cx+w*.75f,top+h-ch*.45f+ch,Path.Direction.CW);eyePath.op(cut,Path.Op.DIFFERENCE);}
            }
            p.setColor(eye);p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,top,0,top+h,mix(eye,Color.WHITE,.35f),shade(eye,.85f),Shader.TileMode.CLAMP));canvas.drawPath(eyePath,p);p.setShader(null);
            if(blink>.5f&&h>=8){canvas.save();canvas.clipPath(eyePath);p.setColor(shine);canvas.drawCircle(cx-w*.18f,top+h*(.24f+Math.max(shape[2],shape[3])*.6f),3.6f,p);canvas.restore();}
        }
        if(!detailedAccessories){
        if(bow!=0){p.setColor(bow);Path ribbon=new Path();ribbon.moveTo(-35,-47);ribbon.lineTo(-53,-57);ribbon.lineTo(-53,-37);ribbon.close();ribbon.moveTo(-35,-47);ribbon.lineTo(-17,-57);ribbon.lineTo(-17,-37);ribbon.close();canvas.drawPath(ribbon,p);canvas.drawCircle(-35,-47,5,p);}
        if(lulaStyle){p.setColor(Color.rgb(221,222,212));p.setStrokeWidth(4);p.setStrokeCap(Paint.Cap.ROUND);canvas.drawLine(-40,-24,-12,-28,p);canvas.drawLine(12,-28,40,-24,p);}
        if(beard!=0&&lulaStyle)drawLulaBeard(canvas);else if(beard!=0){p.setColor(beard);Path hair=new Path();hair.moveTo(-40,18);hair.quadTo(-37,47,0,49);hair.quadTo(37,47,40,18);hair.quadTo(23,35,0,29);hair.quadTo(-23,35,-40,18);canvas.drawPath(hair,p);}
        if(helmet!=0){drawFur(canvas);drawHelmet(canvas);}
        if(sash!=0)drawSash(canvas);
        }
        if(glasses!=0){p.setColor(glasses);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(2);for(int side:new int[]{-1,1}){canvas.drawRoundRect(side*24-21,-25,side*24+21,27,10,10,p);p.setStrokeWidth(5);canvas.drawLine(side*24-17,-25,side*24+17,-25,p);p.setStrokeWidth(2);}canvas.drawLine(-3,-18,3,-18,p);}
        p.setStyle(Paint.Style.FILL);
        if(notebook){p.setColor(Color.rgb(95,110,103));canvas.drawRoundRect(-42,16,42,49,4,4,p);p.setColor(Color.rgb(30,42,33));canvas.drawCircle(0,32,4,p);p.setColor(Color.rgb(160,179,164));canvas.drawRoundRect(-48,47,48,52,3,3,p);}
        if(corner){p.setColor(frame);p.setTextSize(10);p.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));canvas.drawText("CANTINHO",-25,-52,p);}
        if(emotion.equals("dormindo")){for(int i=0;i<3;i++){float t=((now/2500f)+i/3f)%1;p.setColor(eye);p.setAlpha((int)(255*Math.min(1,t*5)*Math.pow(1-t,.6)));p.setTextSize(8+9*t);canvas.drawText("Z",38+25*t,-38-17*t,p);}p.setAlpha(255);}
        canvas.restore();if(autoAnimate&&isShown())postInvalidateDelayed(33);
    }
    private int mix(int a,int b,float t){return Color.rgb(Math.round(Color.red(a)*(1-t)+Color.red(b)*t),Math.round(Color.green(a)*(1-t)+Color.green(b)*t),Math.round(Color.blue(a)*(1-t)+Color.blue(b)*t));}
    private void drawHair(Canvas c){
        p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(-65,-55,65,50,new int[]{0xff3a211b,0xffa36540,0xff46271f},null,Shader.TileMode.CLAMP));Path hair=new Path();hair.moveTo(-56,25);hair.cubicTo(-77,-15,-59,-65,-13,-56);hair.cubicTo(20,-66,58,-47,58,-25);hair.cubicTo(65,7,76,40,61,54);hair.lineTo(47,47);hair.quadTo(60,23,50,-28);hair.quadTo(18,-53,-38,-28);hair.quadTo(-59,-9,-48,34);hair.lineTo(-48,50);hair.quadTo(-71,53,-56,25);c.drawPath(hair,p);p.setShader(null);p.setColor(0xffc38a5e);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);c.drawArc(-60,-53,32,-5,190,125,false,p);p.setStyle(Paint.Style.FILL);
    }
    private void drawFur(Canvas c){p.setStyle(Paint.Style.FILL);for(int i=0;i<16;i++){float x=-61+i*8,y=35+(float)Math.sin(i*.8)*4;p.setColor(i%2==0?0xffbfa184:0xff765a44);c.drawOval(x-8,y-5,x+10,y+12,p);}p.setColor(0xffd8b867);c.drawCircle(0,47,9,p);p.setColor(0xff463728);c.drawCircle(0,47,6,p);p.setStyle(Paint.Style.STROKE);p.setColor(0xffe9d28d);p.setStrokeWidth(1);c.drawCircle(-2,46,3,p);c.drawCircle(2,46,3,p);c.drawCircle(0,49,3,p);p.setStyle(Paint.Style.FILL);}
    private void drawSash(Canvas c){c.save();Path clip=new Path();clip.addRoundRect(-60,-45,60,45,20,20,Path.Direction.CW);c.clipPath(clip);p.setColor(sash);Path band=new Path();band.moveTo(-63,45);band.lineTo(63,10);band.lineTo(63,31);band.lineTo(-63,66);band.close();c.drawPath(band,p);p.setColor(0xffefcd58);Path stripe=new Path();stripe.moveTo(-63,53);stripe.lineTo(63,18);stripe.lineTo(63,24);stripe.lineTo(-63,59);stripe.close();c.drawPath(stripe,p);p.setColor(0xffefcd58);c.drawCircle(11,39,8,p);p.setColor(0xff194c92);c.drawCircle(11,39,5,p);p.setColor(Color.WHITE);c.drawCircle(10,37,1,p);c.restore();}
    private int shade(int color,float factor){return Color.rgb(Math.round(Color.red(color)*factor),Math.round(Color.green(color)*factor),Math.round(Color.blue(color)*factor));}
    private void drawLulaBeard(Canvas c){
        p.setStyle(Paint.Style.FILL);p.setColor(shade(beard,.82f));Path outline=new Path();outline.moveTo(-43,11);outline.cubicTo(-43,35,-24,53,0,52);outline.cubicTo(24,53,43,35,43,11);outline.lineTo(32,22);outline.quadTo(17,30,0,26);outline.quadTo(-17,30,-32,22);outline.close();c.drawPath(outline,p);
        p.setColor(beard);Path center=new Path();center.moveTo(-36,21);center.quadTo(-30,46,0,49);center.quadTo(30,46,36,21);center.quadTo(17,35,0,31);center.quadTo(-17,35,-36,21);c.drawPath(center,p);
        p.setColor(Color.rgb(249,247,239));c.drawOval(-23,20,-1,27,p);c.drawOval(1,20,23,27,p);p.setColor(shade(beard,.70f));p.setStrokeWidth(1.1f);p.setStrokeCap(Paint.Cap.ROUND);for(int side:new int[]{-1,1})for(int i=0;i<3;i++){float x=side*(10+i*8);c.drawLine(x,36,x*.88f,43-i,p);}p.setStyle(Paint.Style.FILL);
    }
    private void drawHelmet(Canvas c){
        final int brass=Color.rgb(211,180,125),ivory=Color.rgb(234,223,195),outline=Color.rgb(48,63,68);
        Path horn=new Path();horn.moveTo(-42,-42);horn.cubicTo(-60,-38,-75,-48,-67,-65);horn.cubicTo(-66,-53,-57,-48,-44,-51);horn.close();
        for(int side:new int[]{1,-1}){c.save();c.scale(side,1);p.setStyle(Paint.Style.FILL);p.setColor(ivory);c.drawPath(horn,p);p.setStyle(Paint.Style.STROKE);p.setColor(outline);p.setStrokeWidth(1.6f);c.drawPath(horn,p);c.restore();}
        Path cap=new Path();cap.moveTo(-47,-35);cap.cubicTo(-45,-53,-27,-65,0,-64);cap.cubicTo(27,-65,45,-53,47,-35);cap.lineTo(47,-30);cap.lineTo(-47,-30);cap.close();
        p.setStyle(Paint.Style.FILL);p.setColor(helmet);c.drawPath(cap,p);
        c.save();c.clipPath(cap);p.setColor(shade(helmet,.82f));c.drawRect(0,-66,50,-29,p);c.restore();
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.8f);p.setColor(outline);c.drawPath(cap,p);
        p.setStyle(Paint.Style.FILL);p.setColor(shade(helmet,.75f));c.drawRoundRect(-49,-39,49,-29,4,4,p);
        p.setStyle(Paint.Style.STROKE);p.setColor(brass);p.setStrokeWidth(1.7f);c.drawRoundRect(-49,-39,49,-29,4,4,p);
        p.setStyle(Paint.Style.FILL);p.setColor(brass);c.drawRoundRect(-5,-63,5,-29,2,2,p);
        for(int x:new int[]{-40,-25,25,40})c.drawCircle(x,-34,2,p);
        p.setColor(outline);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);c.drawLine(-2,-54,-2,-43,p);c.drawLine(-2,-54,3,-48,p);c.drawLine(-2,-43,3,-48,p);
        p.setStyle(Paint.Style.FILL);
    }
    private void drawTie(Canvas c){p.setStyle(Paint.Style.FILL);p.setShader(new LinearGradient(0,16,0,44,0xffffd76b,0xff9d6b1f,Shader.TileMode.CLAMP));Path coat=new Path();coat.moveTo(-58,17);coat.lineTo(-2,29);coat.lineTo(58,17);coat.lineTo(58,43);coat.quadTo(0,55,-58,43);coat.close();c.drawPath(coat,p);p.setShader(null);p.setColor(0xfffaf4dc);Path collar=new Path();collar.moveTo(-42,18);collar.lineTo(0,25);collar.lineTo(42,18);collar.lineTo(20,36);collar.lineTo(0,26);collar.lineTo(-20,36);collar.close();c.drawPath(collar,p);p.setColor(tie);Path path=new Path();path.moveTo(-5,26);path.lineTo(5,26);path.lineTo(4,32);path.lineTo(7,43);path.lineTo(0,46);path.lineTo(-7,43);path.lineTo(-4,32);path.close();c.drawPath(path,p);}
}
