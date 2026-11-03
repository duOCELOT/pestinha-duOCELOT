package org.pestinha.android;

import android.content.Context;
import android.graphics.*;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.*;
import android.view.View;

/** Cached vector cloth and caption, animated with a light mesh rather than a GIF. */
final class ProtestView extends View {
    final String style;private final Bitmap cloth;private final Paint paint=new Paint(3);private final float[] vertices=new float[19*6*2];
    private final boolean motion;
    ProtestView(Context c,String text,String requested){super(c);style=requested.equals("flag")||requested.equals("banner")?requested:"placard";
        motion=Settings.Global.getFloat(c.getContentResolver(),Settings.Global.ANIMATOR_DURATION_SCALE,1)>0;
        cloth=Bitmap.createBitmap(480,192,Bitmap.Config.ARGB_8888);Canvas layer=new Canvas(cloth);Drawable art=c.getDrawable(style.equals("flag")?R.drawable.protest_flag:style.equals("banner")?R.drawable.protest_banner:R.drawable.protest_placard);art.setBounds(0,0,480,192);art.draw(layer);
        int accent=Design.ACCENT;try{accent=Color.parseColor(Characters.appearance(c,MemoryActivity.petId(c)).getString("moldura"));}catch(Exception ignored){}
        paint.setColor(accent);layer.drawRect(10,10,452,23,paint);layer.drawRect(10,169,452,182,paint);
        TextPaint ink=new TextPaint(3);ink.setColor(0xff29251c);ink.setTextSize(30);ink.setTypeface(Typeface.create("sans-serif-medium",Typeface.BOLD));
        String caption=text.length()>150?text.substring(0,147)+"…":text;
        StaticLayout words=StaticLayout.Builder.obtain(caption,0,caption.length(),ink,410).setAlignment(Layout.Alignment.ALIGN_CENTER).setMaxLines(3).setEllipsize(TextUtils.TruncateAt.END).setIncludePad(false).build();layer.save();layer.translate(18,Math.max(29,(192-words.getHeight())/2f));words.draw(layer);layer.restore();
        setContentDescription((style.equals("flag")?"Bandeira":style.equals("banner")?"Faixa":"Cartaz")+" do pet: "+text+". Toque para recolher.");setFocusable(true);
    }
    @Override protected void onDraw(Canvas c){super.onDraw(c);float scale=Math.min(getWidth()/270f,getHeight()/166f);c.save();c.scale(scale,scale);float time=motion?SystemClock.uptimeMillis()/420f:0;
        paint.setColor(0xff8b6748);paint.setStrokeWidth(5);paint.setStrokeCap(Paint.Cap.ROUND);
        if(style.equals("flag"))c.drawLine(18,12,18,155,paint);else if(style.equals("banner")){c.drawLine(20,24,20,148,paint);c.drawLine(251,24,251,148,paint);}else{c.rotate(motion?(float)Math.sin(time)*1.7f:0,135,105);c.drawLine(135,80,135,154,paint);}
        c.translate(style.equals("flag")?20:14,16);
        int index=0;for(int y=0;y<=5;y++)for(int x=0;x<=18;x++){float fraction=x/18f;vertices[index++]=fraction*236;vertices[index++]=y/5f*96+(motion&&!style.equals("placard")?(float)Math.sin(time+fraction*5)*(style.equals("flag")?7:3)*fraction:0);}
        c.drawBitmapMesh(cloth,18,5,vertices,0,null,0,paint);c.restore();if(motion&&isShown()&&isAttachedToWindow())postInvalidateDelayed(50);
    }
}
