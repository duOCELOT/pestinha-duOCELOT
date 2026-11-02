package org.pestinha.android;

import android.content.Context;
import android.graphics.*;
import android.view.View;
import java.util.*;

/** Accessible native bars: all values are from measured or persisted state. */
final class MetricChart extends View {
    private String[] names=new String[0],values=new String[0];private double[] amounts=new double[0];
    private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);private double maximum=1;private int color=Design.ACCENT;
    MetricChart(Context c){super(c);setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_YES);}
    void data(String[] labels,double[] data,String[] formatted,double max,int tint){names=labels;amounts=data;values=formatted;maximum=Math.max(.0001,max);color=tint;StringBuilder description=new StringBuilder();for(int i=0;i<names.length;i++)description.append(names[i]).append(": ").append(values[i]).append("; ");setContentDescription(description.toString());requestLayout();invalidate();}
    @Override protected void onMeasure(int w,int h){setMeasuredDimension(MeasureSpec.getSize(w),Design.dp(getContext(),Math.max(60,names.length*49+8)));}
    @Override protected void onDraw(Canvas c){super.onDraw(c);float d=getResources().getDisplayMetrics().density,width=getWidth();paint.setTypeface(Typeface.create("sans-serif",Typeface.NORMAL));paint.setTextSize(13*d);
        for(int i=0;i<names.length;i++){float y=(i*49+17)*d;paint.setColor(Design.MUTED);String label=android.text.TextUtils.ellipsize(names[i],new android.text.TextPaint(paint),Math.max(20,width-paint.measureText(values[i])-16*d),android.text.TextUtils.TruncateAt.END).toString();c.drawText(label,0,y,paint);paint.setTextAlign(Paint.Align.RIGHT);paint.setColor(Design.TEXT);c.drawText(values[i],width,y,paint);paint.setTextAlign(Paint.Align.LEFT);float top=y+9*d;paint.setColor(Design.BORDER);c.drawRoundRect(0,top,width,top+7*d,4*d,4*d,paint);paint.setColor(color);float fill=(float)Math.max(0,Math.min(1,amounts[i]/maximum))*width;if(fill>0)c.drawRoundRect(0,top,fill,top+7*d,4*d,4*d,paint);}
        if(names.length==0){paint.setColor(Design.MUTED);c.drawText("Ainda sem medições",0,24*d,paint);}
    }
}
