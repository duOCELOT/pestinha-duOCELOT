package org.pestinha.android;

import android.content.Context;
import android.graphics.*;
import android.view.View;

/** Transparent native canvas. No bitmap assets or desktop compositor needed. */
public class PetView extends View {
    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    public String emotion = "observando";
    public PetView(Context context) { super(context); setLayerType(View.LAYER_TYPE_SOFTWARE, null); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.scale(getWidth() / 160f, getHeight() / 130f);
        p.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawOval(28, 112, 132, 120, p);
        p.setColor(Color.rgb(45, 66, 78));
        canvas.drawRoundRect(11, 18, 149, 110, 25, 25, p);
        p.setColor(Color.rgb(13, 26, 33));
        canvas.drawRoundRect(16, 22, 144, 105, 21, 21, p);
        p.setColor(Color.rgb(115, 230, 202));
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
                    canvas.drawPath(lid, p); p.setColor(Color.rgb(115, 230, 202));
                }
            }
        }
        if (emotion.equals("dormindo")) { p.setTextSize(18); canvas.drawText("z", 121, 20, p); }
        canvas.restore();
    }
}
