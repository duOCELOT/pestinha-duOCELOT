package org.pestinha.android;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Transparent native canvas. No bitmap assets or desktop compositor needed. */
public class PetView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public String emotion = "observando";
    public float bodyAngle=0;
    public boolean jorge=false, notebook=false, corner=false;
    public PetView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(getWidth() / 160f, getHeight() / 130f);
        p.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawOval(28, 112, 132, 120, p);
        canvas.rotate(bodyAngle,80,65);
        p.setColor(Color.rgb(45, 66, 78));
        canvas.drawRoundRect(11, 18, 149, 110, 25, 25, p);
        p.setColor(Color.rgb(13, 26, 33));
        canvas.drawRoundRect(16, 22, 144, 105, 21, 21, p);
        p.setColor(jorge?Color.rgb(255,209,102):Color.rgb(115,230,202));
        float height = 37, y = 43;
        if (emotion.equals("curioso") || emotion.equals("assustado")) height = 47;
        if (emotion.equals("triste")) { height = 29; y = 54; }
        if (emotion.equals("dormindo")) { height = 5; y = 66; }
        boolean happy = emotion.equals("feliz") || emotion.equals("contente");
        // Blink without changing the neural emotion.
        if (!happy && !emotion.equals("dormindo") && android.os.SystemClock.uptimeMillis() % 5200 < 120) height = 5;
        for (int i = 0; i < 2; i++) {
            float x = i == 0 ? 37 : 94;
            if (happy) {
                p.setStyle(Paint.Style.STROKE); p.setStrokeWidth(9); p.setStrokeCap(Paint.Cap.ROUND);
                canvas.drawArc(x, 50, x + 29, 79, 195, 150, false, p);
                p.setStyle(Paint.Style.FILL);
            } else {
                canvas.drawRoundRect(x, y, x + 29, y + height, 10, 10, p);
                if (emotion.equals("bravo") || emotion.equals("triste")) {
                    p.setColor(Color.rgb(13, 26, 33));
                    Path lid = new Path(); lid.moveTo(x - 2, y - 2); lid.lineTo(x + 31, y - 2);
                    boolean angry = emotion.equals("bravo");
                    lid.lineTo(x + 31, y + ((i == 0) == angry ? 18 : 1));
                    lid.lineTo(x - 2, y + ((i == 0) == angry ? 1 : 18)); lid.close();
                    canvas.drawPath(lid, p); p.setColor(jorge?Color.rgb(255,209,102):Color.rgb(115,230,202));
                }
            }
        }
        if (emotion.equals("dormindo")) { p.setTextSize(18); canvas.drawText("z", 121, 20, p); }
        if(jorge){p.setColor(Color.rgb(179,38,30));Path tie=new Path();tie.moveTo(80,96);tie.lineTo(72,112);tie.lineTo(80,119);tie.lineTo(88,112);tie.close();canvas.drawPath(tie,p);}
        if(notebook){p.setColor(Color.rgb(97,112,129));canvas.drawRoundRect(39,80,122,115,4,4,p);p.setColor(Color.rgb(23,32,43));canvas.drawCircle(80,98,5,p);p.setColor(Color.LTGRAY);canvas.drawRoundRect(32,114,130,120,3,3,p);}
        if(corner){p.setColor(Color.rgb(255,205,110));p.setTextSize(12);canvas.drawText("CANTINHO",47,15,p);}
        canvas.restore();
    }
}
