package org.pestinha.android;

import android.app.*;
import android.content.*;
import android.content.pm.ServiceInfo;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.*;
import android.provider.Settings;
import android.view.*;
import com.chaquo.python.*;
import com.chaquo.python.android.AndroidPlatform;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;

public class PetService extends Service {
    public static volatile boolean running = false;
    public static volatile String emotion = "observando";
    public static volatile double energy = 1.0;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private HandlerThread thread;
    private Handler worker;
    private PyObject brain;
    private WindowManager wm;
    private WindowManager.LayoutParams pos;
    private PetView pet;
    private volatile boolean closed = false, screenOn = true;
    private boolean dragging = false, receiverRegistered = false;
    private double speed = 20, drive = 0;
    private float vx = 0, vy = 0;
    private long lastFrame, lastSave;
    private final BroadcastReceiver screen = new BroadcastReceiver() {
        public void onReceive(Context c, Intent i) {
            screenOn = Intent.ACTION_SCREEN_ON.equals(i.getAction());
            ui.removeCallbacks(frame);
            if (screenOn) { lastFrame = SystemClock.uptimeMillis(); ui.post(frame); }
            else if (worker != null) worker.post(() -> save());
        }
    };
    private static String readText(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int count;
        while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
        return out.toString("UTF-8");
    }

    @Override public void onCreate() {
        super.onCreate();
        getSharedPreferences("pet", 0).edit().remove("error").apply();
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("pet", "Pet flutuante", NotificationManager.IMPORTANCE_LOW));
        PendingIntent panel = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(this, 1, new Intent(this, PetService.class).setAction("stop"), PendingIntent.FLAG_IMMUTABLE);
        Notification note = new Notification.Builder(this, "pet").setSmallIcon(org.pestinha.android.R.drawable.ic_pet)
                .setContentTitle("Bryan está por aqui").setContentText("Toque para abrir os controles.")
                .setContentIntent(panel).setOngoing(true)
                .addAction(new Notification.Action.Builder(null, "Guardar", stop).build()).build();
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(1, note);
        wm = getSystemService(WindowManager.class);
        pet = new PetView(this);
        float density = getResources().getDisplayMetrics().density;
        pos = new WindowManager.LayoutParams((int)(150*density), (int)(122*density),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        pos.gravity = Gravity.TOP | Gravity.LEFT;
        android.content.SharedPreferences prefs = getSharedPreferences("pet", 0);
        pos.x = prefs.getInt("x", 20); pos.y = prefs.getInt("y", 220);
        try { wm.addView(pet, pos); clamp(); } catch (RuntimeException e) { fail(e); return; }
        GestureDetector gestures = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(android.view.MotionEvent e) { return true; }
            @Override public boolean onSingleTapConfirmed(android.view.MotionEvent e) { event("poke"); return true; }
            @Override public boolean onDoubleTap(android.view.MotionEvent e) { event("caress"); return true; }
            @Override public void onLongPress(android.view.MotionEvent e) {
                if (!dragging) startActivity(new Intent(PetService.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }
        });
        pet.setOnTouchListener(new View.OnTouchListener() {
            float x, y; int initialX, initialY;
            public boolean onTouch(View v, android.view.MotionEvent e) {
                gestures.onTouchEvent(e);
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        x = e.getRawX(); y = e.getRawY(); initialX = pos.x; initialY = pos.y;
                        vx = vy = 0; dragging = false; break;
                    case MotionEvent.ACTION_MOVE:
                        float dx = e.getRawX() - x, dy = e.getRawY() - y;
                        if (!dragging && Math.hypot(dx, dy) > ViewConfiguration.get(PetService.this).getScaledTouchSlop()) {
                            dragging = true; event("drag");
                        }
                        if (dragging) { pos.x = initialX + (int)dx; pos.y = initialY + (int)dy; clamp(); }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        dragging = false; storePosition(); break;
                }
                return true;
            }
        });
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF); filter.addAction(Intent.ACTION_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(screen, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(screen, filter);
        receiverRegistered = true;
        screenOn = getSystemService(PowerManager.class).isInteractive();
        thread = new HandlerThread("PestinhaBrain"); thread.start(); worker = new Handler(thread.getLooper());
        worker.post(() -> {
            try {
                if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
                String definition;
                try (java.io.InputStream in = getAssets().open("pets/bryan.json")) {
                    definition = readText(in);
                }
                brain = Python.getInstance().getModule("android_brain").get("AndroidBrain")
                        .call(new java.io.File(getFilesDir(), "memory/bryan").toString(), definition);
                if (!closed) { running = true; worker.post(tick); }
            } catch (Exception e) { ui.post(() -> fail(e)); }
        });
        lastFrame = SystemClock.uptimeMillis(); if (screenOn) ui.post(frame);
    }
    private void event(String kind) { if (worker != null && !closed) worker.post(() -> {
        if (brain != null) try { brain.callAttr("event", kind); } catch (Exception e) { ui.post(() -> fail(e)); }
    }); }
    private final Runnable tick = new Runnable() {
        public void run() {
            if (closed) return;
            try {
                if (screenOn && brain != null) {
                    JSONObject data = new JSONObject(brain.callAttr("tick", 0.2).toString());
                    String next = data.getString("emotion"); double en = data.getDouble("energy");
                    double sp = data.getDouble("speed"), dr = data.getDouble("drive");
                    ui.post(() -> { if (!closed) { emotion = next; energy = en; speed = sp; drive = dr; pet.emotion = next; } });
                }
                if (SystemClock.uptimeMillis() - lastSave > 15000) { save(); lastSave = SystemClock.uptimeMillis(); }
                worker.postDelayed(this, screenOn ? 200 : 3000);
            } catch (Exception e) { ui.post(() -> fail(e)); }
        }
    };
    private void save() { if (brain != null) try { brain.callAttr("save"); } catch (Exception e) { android.util.Log.e("Pestinha", "Saving memory", e); } }
    private final Runnable frame = new Runnable() {
        public void run() {
            if (closed || pet == null || !screenOn) return;
            long now = SystemClock.uptimeMillis(); float dt = Math.min(0.05f, (now-lastFrame)/1000f); lastFrame = now;
            if (!dragging && running && !emotion.equals("dormindo")) {
                if (Math.random() < dt * (0.12 + drive * 0.4)) vx = (float)((Math.random() < 0.5 ? -1 : 1) * speed * 0.4);
                vy += dt * 420 * getResources().getDisplayMetrics().density;
                pos.x += Math.round(vx * dt); pos.y += Math.round(vy * dt);
                clamp();
            }
            pet.invalidate(); ui.postDelayed(this, emotion.equals("dormindo") ? 200 : 33);
        }
    };
    private void clamp() {
        if (pet == null || !pet.isAttachedToWindow()) return;
        android.graphics.Rect bounds;
        if (Build.VERSION.SDK_INT >= 30) bounds = wm.getCurrentWindowMetrics().getBounds();
        else { android.util.DisplayMetrics metrics = new android.util.DisplayMetrics(); wm.getDefaultDisplay().getMetrics(metrics); bounds = new android.graphics.Rect(0, 0, metrics.widthPixels, metrics.heightPixels); }
        int maxX = Math.max(0, bounds.width() - pos.width), maxY = Math.max(0, bounds.height() - pos.height - 36);
        if (pos.x <= 0 || pos.x >= maxX) vx = -vx;
        if (pos.y >= maxY) vy = 0;
        pos.x = Math.max(0, Math.min(maxX, pos.x)); pos.y = Math.max(0, Math.min(maxY, pos.y));
        try { wm.updateViewLayout(pet, pos); } catch (IllegalArgumentException ignored) { }
    }
    private void storePosition() { if (pos != null) getSharedPreferences("pet", 0).edit().putInt("x", pos.x).putInt("y", pos.y).apply(); }
    private void fail(Exception e) {
        android.util.Log.e("Pestinha", "Pet failed", e);
        getSharedPreferences("pet", 0).edit().putString("error", String.valueOf(e.getMessage())).apply(); stopSelf();
    }
    @Override public void onConfigurationChanged(Configuration c) { super.onConfigurationChanged(c); clamp(); }
    @Override public int onStartCommand(Intent i, int flags, int id) {
        if (i != null && "stop".equals(i.getAction())) stopSelf();
        else if (i != null && i.getAction() != null) event(i.getAction());
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() {
        closed = true; running = false; ui.removeCallbacksAndMessages(null); storePosition();
        if (receiverRegistered) unregisterReceiver(screen);
        if (pet != null && pet.isAttachedToWindow()) wm.removeView(pet);
        if (worker != null) { worker.removeCallbacks(tick); worker.post(() -> { save(); thread.quitSafely(); }); }
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
