package org.pestinha.android;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Vector icons with consistent stroke widths; no emoji or downloaded image assets. */
final class ActionIcon extends View {
    final String kind;final Paint p=new Paint(3);int color=Design.ACCENT;
    ActionIcon(Context c,String action){super(c);kind=action;setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);}
    @Override protected void onDraw(Canvas c){super.onDraw(c);c.save();float s=Math.min(getWidth(),getHeight())/24f;c.translate((getWidth()-24*s)/2,(getHeight()-24*s)/2);c.scale(s,s);p.setColor(color);p.setStrokeWidth(1.7f);p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeJoin(Paint.Join.ROUND);
        Path path=new Path();
        switch(kind){
            case "caress":path.moveTo(12,20);path.cubicTo(1,13,2,5,7,5);path.cubicTo(10,5,12,8,12,8);path.cubicTo(12,8,14,5,17,5);path.cubicTo(22,5,23,13,12,20);c.drawPath(path,p);break;
            case "arcade":for(int row=0;row<2;row++)for(int col=0;col<3;col++)c.drawRoundRect(3+col*6,3+row*5,7+col*6,6+row*5,1,1,p);c.drawCircle(14,14,1.5f,p);c.drawRoundRect(7,20,18,22,1,1,p);break;
            case "ball":c.drawCircle(12,12,8,p);c.drawArc(0,4,13,20,-75,150,false,p);c.drawArc(11,4,24,20,105,150,false,p);break;
            case "sleep":path.moveTo(17,3);path.cubicTo(6,-1,-2,13,8,20);path.cubicTo(14,24,22,18,21,11);path.cubicTo(15,16,9,8,17,3);c.drawPath(path,p);break;
            case "work":c.drawRoundRect(5,4,19,16,2,2,p);c.drawLine(2,19,22,19,p);c.drawLine(5,16,2,19,p);c.drawLine(19,16,22,19,p);break;
            case "memory":c.drawRoundRect(6,6,18,18,3,3,p);for(int i=8;i<=16;i+=4){c.drawLine(i,3,i,6,p);c.drawLine(i,18,i,21,p);c.drawLine(3,i,6,i,p);c.drawLine(18,i,21,i,p);}c.drawCircle(12,12,2,p);break;
            case "assistant":c.drawRoundRect(4,4,20,17,4,4,p);c.drawLine(8,17,6,21,p);c.drawLine(6,21,14,17,p);c.drawLine(8,9,16,9,p);c.drawLine(8,13,13,13,p);break;
            case "panel":c.drawRoundRect(3,3,10,10,2,2,p);c.drawRoundRect(14,3,21,10,2,2,p);c.drawRoundRect(3,14,10,21,2,2,p);c.drawRoundRect(14,14,21,21,2,2,p);break;
            case "stop":c.drawRoundRect(5,5,19,19,4,4,p);c.drawLine(9,12,15,12,p);break;
            case "close":c.drawLine(7,7,17,17,p);c.drawLine(17,7,7,17,p);break;
            case "pin":c.drawLine(8,3,16,3,p);c.drawLine(9,3,9,9,p);c.drawLine(15,3,15,9,p);path.moveTo(9,9);path.lineTo(5,14);path.lineTo(19,14);path.lineTo(15,9);c.drawPath(path,p);c.drawLine(12,14,12,22,p);break;
            case "note":c.drawRoundRect(5,3,19,21,2,2,p);c.drawLine(8,8,16,8,p);c.drawLine(8,12,16,12,p);c.drawLine(8,16,13,16,p);break;
            default:c.drawCircle(12,12,7,p);c.drawLine(12,8,12,12,p);c.drawLine(12,12,15,14,p);
        }c.restore();}
}
