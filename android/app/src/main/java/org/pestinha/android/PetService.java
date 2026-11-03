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
    private static volatile PetService instance;
    static void assistantNotice(String text,String event){
        PetService service=instance;if(service==null||service.closed)return;
        if(event!=null)service.event(event);if(playing)return;String spoken=Characters.speak(service,text);
        service.ui.post(()->{if(!service.closed&&service.world!=null&&service.screenOn){lastNoticeAt=SystemClock.uptimeMillis();service.world.message(spoken,false,service.pos.x,service.pos.y,()->{});}});
    }
    private static volatile long lastNoticeAt=0;
    static void newsSettingsChanged(){PetService service=instance;if(service!=null)service.ui.post(()->{if(!service.closed&&service.newsMonitor!=null)service.newsMonitor.update(service.screenOn);});}
    static void newsNotice(JSONObject row,String generation,boolean manual){PetService service=instance;if(service==null||service.closed)return;
        service.ui.post(()->{if(service.closed||playing||!running||service.world==null||!service.screenOn||!UsageRepository.interactive(service)||InterestsDialog.visible||!generation.equals(NewsRepository.generation(service)))return;
            if(!manual&&(!NewsRepository.prefs(service).getBoolean("radarAuto",false)||service.pressed||service.dragging||(service.quickMenu!=null&&service.quickMenu.isShowing())||SystemClock.uptimeMillis()-lastNoticeAt<30000))return;
            if(!manual&&NewsRepository.candidate(service,System.currentTimeMillis())==null)return;
            NewsRepository.shown(service,row);lastNoticeAt=SystemClock.uptimeMillis();service.world.messageAction(NewsRepository.message(service,row).replace("\nToque para ler a notícia.",""),false,service.pos.x,service.pos.y,()->{},"Ler notícia",()->service.startActivity(new Intent(service,NewsActivity.class).putExtra("article",row.toString()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        });
    }
    static void taskNotice(String text,String taskId,boolean create){
        PetService service=instance;if(service==null||service.closed)return;
        service.ui.post(()->{if(service.closed||playing||service.world==null||!service.screenOn||service.getSystemService(KeyguardManager.class).isKeyguardLocked())return;
            lastNoticeAt=SystemClock.uptimeMillis();service.world.message(text,false,service.pos.x,service.pos.y,()->service.startActivity(new Intent(service,TaskActivity.class).putExtra("task",taskId).putExtra("new",create).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)));
        });
    }
    private final Runnable taskInvitation=new Runnable(){public void run(){
        if(closed)return;
        if(running&&!playing&&screenOn&&world!=null&&!getSystemService(KeyguardManager.class).isKeyguardLocked()&&Tasks.invitationDue(PetService.this,System.currentTimeMillis())){
            Tasks.prefs(PetService.this).edit().putLong("invitedAt",System.currentTimeMillis()).apply();taskNotice(TaskActivity.invitation(),null,true);
        }
        ui.postDelayed(this,60000);
    }};
    static void usageSettingsChanged(){PetService service=instance;if(service!=null)service.ui.post(()->{if(!service.closed&&service.usageMonitor!=null)service.usageMonitor.update(service.screenOn);});}
    public static volatile boolean running = false, busy = false, playing=false;
    public static volatile String liveSnapshot = "";
    public static volatile String emotion = "observando";
    public static volatile double energy = 1.0;
    private final Handler ui = new Handler(Looper.getMainLooper());
    private HandlerThread thread;
    private Handler worker;
    private PyObject brain;
    private WindowManager wm;
    private WindowManager.LayoutParams pos;
    private PetView pet;
    private PetWorld world;
    private UsageMonitor usageMonitor;
    private NewsMonitor newsMonitor;
    private QuickMenu quickMenu;
    private BrickGameOverlay arcade;
    private int beforeGameX,beforeGameY;
    private boolean menuOpenedByHold=false;
    private MotionSensors motion;
    private boolean tiltEnabled=false,fixed=false;
    private float tiltSensitivity=1;
    private long lastImpact=0;
    private String petId;
    private float xFloat, yFloat, userSpeed=1;
    private long lastAction=0;
    private boolean inCorner=false, notebook=false, facingSleep=false;
    private volatile boolean closed = false, screenOn = true;
    private boolean dragging = false, pressed = false, receiverRegistered = false;
    private double speed = 20, drive = 0;
    private float vx = 0, vy = 0;
    private long lastFrame, lastSave;
    private final BroadcastReceiver screen = new BroadcastReceiver() {
        public void onReceive(Context c, Intent i) {
            screenOn = Intent.ACTION_SCREEN_ON.equals(i.getAction());
            updateMotion();
            if(usageMonitor!=null)usageMonitor.update(screenOn);if(newsMonitor!=null)newsMonitor.update(screenOn);
            if(!screenOn){pressed=dragging=false;event("release");if(quickMenu!=null)quickMenu.dismiss();}
            ui.removeCallbacks(frame);
            if (screenOn) { lastFrame = SystemClock.uptimeMillis(); ui.post(frame); }
            if(arcade!=null)arcade.screen(screenOn);
            if(world!=null)world.show(screenOn&&!playing);
            if(pet!=null)pet.setVisibility(screenOn?View.VISIBLE:View.INVISIBLE);
            if (worker != null) worker.post(() -> { if(brain!=null)brain.callAttr("event",screenOn?"resume":"pause"); save(); });
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
        instance=this;
        busy = true; liveSnapshot = "";
        petId = MemoryActivity.petId(this);
        getSharedPreferences("pet", 0).edit().remove("error").apply();
        if (!Settings.canDrawOverlays(this)) { stopSelf(); return; }
        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(new NotificationChannel("pet", "Pet flutuante", NotificationManager.IMPORTANCE_LOW));
        PendingIntent panel = PendingIntent.getActivity(this, 0, new Intent(this, MainActivity.class), PendingIntent.FLAG_IMMUTABLE);
        PendingIntent stop = PendingIntent.getService(this, 1, new Intent(this, PetService.class).setAction("stop"), PendingIntent.FLAG_IMMUTABLE);
        Notification note = new Notification.Builder(this, "pet").setSmallIcon(org.pestinha.android.R.drawable.ic_notification)
                .setContentTitle(Characters.name(this,petId)+" está por aqui").setContentText("Toque para abrir os controles.")
                .setContentIntent(panel).setOngoing(true)
                .addAction(new Notification.Action.Builder(null, "Recolher pet", stop).build()).build();
        if (Build.VERSION.SDK_INT >= 34) startForeground(1, note, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        else startForeground(1, note);
        wm = getSystemService(WindowManager.class);
        pet = new PetView(this);
        pet.jorge = petId.equals("jorge");
        world = new PetWorld(this, wm, this::event);
        usageMonitor=new UsageMonitor(this);newsMonitor=new NewsMonitor(this);
        quickMenu = new QuickMenu(this,wm,action->{
            if(action.equals("stop"))stopSelf();
            else if(action.equals("pin"))setFixed(!fixed);
            else if(action.equals("arcade"))startArcade();
            else if(action.equals("care"))startActivity(new Intent(this,CareActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            else if(action.equals("panel")||action.equals("memory")||action.equals("assistant")){
                Class<?> screen=action.equals("panel")?MainActivity.class:action.equals("memory")?MemoryActivity.class:AssistantActivity.class;
                startActivity(new Intent(this,screen).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            }else if(action.equals("ball"))world.toggleBall(pos.x,pos.y);
            else event(action);
        });
        motion = new MotionSensors(this, () -> {
            if(!closed && !playing && !fixed && running && !dragging && !pressed && !inCorner && !notebook){
                vy=-180*getResources().getDisplayMetrics().density;event("motion");
            }
        });
        float density = getResources().getDisplayMetrics().density;
        pos = new WindowManager.LayoutParams((int)(150*density), (int)(122*density),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
        pos.gravity = Gravity.TOP | Gravity.LEFT;
        android.content.SharedPreferences prefs = getSharedPreferences("pet", 0);
        pos.x = prefs.getInt("x", 20); pos.y = prefs.getInt("y", 220);
        xFloat=pos.x; yFloat=pos.y; applySettings();
        try { wm.addView(pet, pos); clamp(); } catch (RuntimeException e) { fail(e); return; }
        GestureDetector gestures = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(android.view.MotionEvent e) { return true; }
            @Override public boolean onSingleTapConfirmed(android.view.MotionEvent e) { event("poke"); return true; }
            @Override public boolean onDoubleTap(android.view.MotionEvent e) { event("caress"); return true; }
            @Override public void onLongPress(android.view.MotionEvent e) {
                if (!dragging && running){
                    menuOpenedByHold=true;vx=vy=0;
                    pet.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    quickMenu.show(pos.x,pos.y,pos.width,pos.height,emotion.equals("dormindo"),notebook,world.ball!=null);
                }
            }
        });
        pet.setOnTouchListener(new View.OnTouchListener() {
            float x, y, previousX, previousY, flingX, flingY; long movedAt; int initialX, initialY;
            public boolean onTouch(View v, android.view.MotionEvent e) {
                if(playing&&arcade!=null)return arcade.touch(e);
                gestures.onTouchEvent(e);
                switch (e.getActionMasked()) {
                    case MotionEvent.ACTION_DOWN:
                        pressed=true;
                        x = e.getRawX(); y = e.getRawY(); initialX = pos.x; initialY = pos.y;
                        vx = vy = 0; dragging = false;
                        previousX=x; previousY=y; movedAt=e.getEventTime(); flingX=flingY=0;
                        menuOpenedByHold=false;if(quickMenu!=null)quickMenu.dismiss();
                        event("press"); break;
                    case MotionEvent.ACTION_MOVE:
                        if(menuOpenedByHold)return true;
                        float dx = e.getRawX() - x, dy = e.getRawY() - y;
                        if (!dragging && Math.hypot(dx, dy) > ViewConfiguration.get(PetService.this).getScaledTouchSlop()) {
                            dragging = true; event("drag");
                        }
                        if (dragging) {
                            long delta=e.getEventTime()-movedAt;
                            if(delta>0){flingX=(e.getRawX()-previousX)*1000/delta;flingY=(e.getRawY()-previousY)*1000/delta;}
                            movedAt=e.getEventTime();previousX=e.getRawX();previousY=e.getRawY();
                            pos.x = initialX + (int)dx; pos.y = initialY + (int)dy; clamp(); xFloat=pos.x;yFloat=pos.y;
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                        if(!fixed && !menuOpenedByHold && dragging && e.getEventTime()-movedAt<150 && Math.hypot(flingX,flingY)>500*density){
                            vx=Math.max(-1500*density,Math.min(1500*density,flingX));
                            vy=Math.max(-1500*density,Math.min(1500*density,flingY)); event("throw");
                        }
                    case MotionEvent.ACTION_CANCEL:
                        event("release"); pressed=false; dragging = false; storePosition(); break;
                }
                return true;
            }
        });
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF); filter.addAction(Intent.ACTION_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= 33) registerReceiver(screen, filter, Context.RECEIVER_NOT_EXPORTED);
        else registerReceiver(screen, filter);
        receiverRegistered = true;
        screenOn = getSystemService(PowerManager.class).isInteractive();
        updateMotion();usageMonitor.update(screenOn);newsMonitor.update(screenOn);
        thread = new HandlerThread("PestinhaBrain"); thread.start(); worker = new Handler(thread.getLooper());
        worker.post(() -> {
            try {
                if (!Python.isStarted()) Python.start(new AndroidPlatform(this));
                String definition;
                try (java.io.InputStream in = getAssets().open("pets/"+petId+".json")) {
                    definition = readText(in);
                }
                JSONObject appearance=Characters.appearance(this,petId);
                ui.post(()->{if(!closed)pet.appearance(appearance);});
                brain = Python.getInstance().getModule("android_brain").get("AndroidBrain")
                        .call(new java.io.File(getFilesDir(), "memory/"+petId).toString(), definition);
                brain.callAttr("event", "context:"+getSharedPreferences("pet",0).getString("context","livre"));
                if(getSharedPreferences("pet",0).getBoolean("mischief",false))brain.callAttr("event","mischief");
                JSONObject focus=LocalAssistant.focus(this);
                if(focus!=null&&focus.optLong("when")>System.currentTimeMillis()&&focus.optString("kind").equals("focus"))brain.callAttr("event","focus_start");
                if (!closed) { running = true; worker.post(tick); }
            } catch (Exception e) { ui.post(() -> fail(e)); }
        });
        lastFrame = SystemClock.uptimeMillis(); if (screenOn) ui.post(frame);ui.postDelayed(taskInvitation,20000);
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
                    String snapshot=data.toString();
                    ui.post(() -> { if (!closed) {
                        boolean celebrate=next.equals("feliz")&&!emotion.equals("feliz");
                        liveSnapshot=snapshot; emotion=next; energy=en; speed=sp; drive=dr; if(!playing){pet.emotion=next;pet.face(data.optJSONObject("face"),data.optBoolean("caressing"));}
                        boolean wasCorner=inCorner; inCorner=data.optBoolean("corner"); notebook=data.optBoolean("notebook");
                        if(!playing){pet.notebook=notebook; pet.corner=inCorner;}
                        if(!playing&&!fixed&&inCorner&&!wasCorner){pos.x=0;clamp();xFloat=pos.x;yFloat=pos.y;}
                        if(!playing&&celebrate&&getSharedPreferences("pet",0).getBoolean("fireworks",true))world.fireworks(pos.x,pos.y);
                        JSONObject action=data.optJSONObject("action");
                        if(action!=null&&action.optLong("id")!=lastAction){
                            lastAction=action.optLong("id");String kind=action.optString("kind");
                            if(!playing&&kind.equals("care"))world.message(action.optString("text"),false,pos.x,pos.y,()->{});
                            if(!playing&&(kind.equals("note")||kind.equals("sign")||kind.equals("image")))world.message(action.optString("text"),kind.equals("image"),pos.x,pos.y,()->event("dismiss"));
                            if(!playing&&!fixed&&kind.equals("roam"))vx=(float)(getResources().getDisplayMetrics().density*speed);
                        }
                    } });
                }
                if (!screenOn && brain != null) brain.callAttr("rest", 3.0);
                if (SystemClock.uptimeMillis() - lastSave > 15000) { save(); lastSave = SystemClock.uptimeMillis(); }
                worker.postDelayed(this, screenOn ? 200 : 3000);
            } catch (Exception e) { ui.post(() -> fail(e)); }
        }
    };
    private void save() { if (brain != null) try { brain.callAttr("save"); } catch (Exception e) { android.util.Log.e("Pestinha", "Saving memory", e); } }
    private final Runnable frame = new Runnable() {
        public void run() {
            if (closed || pet == null || !screenOn) return;
            if(playing){pet.invalidate();ui.postDelayed(this,100);return;}
            long now = SystemClock.uptimeMillis(); float dt = Math.min(0.05f, (now-lastFrame)/1000f); lastFrame = now;
            boolean useTilt=tiltEnabled && motion!=null && motion.active;
            if(fixed){vx=vy=0;xFloat=pos.x;yFloat=pos.y;}
            float density=getResources().getDisplayMetrics().density;
            float[] force=useTilt?motion.force(displayRotation()):new float[]{0,0};
            if (!fixed && !dragging && !pressed && running && !inCorner && !notebook && !quickMenu.isShowing() && (useTilt || !emotion.equals("dormindo"))) {
                if(useTilt){
                    vx=TiltPhysics.velocity(vx,force[0]*90*density*tiltSensitivity,dt,650*density);
                    vy=TiltPhysics.velocity(vy,force[1]*90*density*tiltSensitivity,dt,650*density);
                }else{
                    if(Math.random()<dt*(.12+drive*.4))vx=(float)((Math.random()<.5?-1:1)*speed*.4*density*userSpeed);
                    vy+=dt*420*density;
                }
                xFloat+=vx*dt;yFloat+=vy*dt;
                pos.x=Math.round(xFloat);pos.y=Math.round(yFloat);clamp();
            }
            float angle=!fixed&&useTilt&&!dragging&&!pressed&&!quickMenu.isShowing()?motion.angle(displayRotation()):0;
            pet.bodyAngle+=(angle-pet.bodyAngle)*Math.min(1,dt*8);
            world.gravity(useTilt,force[0]*90*tiltSensitivity,force[1]*90*tiltSensitivity);
            world.frame(dt,pos.x,pos.y,pos.width,pos.height);
            pet.invalidate(); ui.postDelayed(this, emotion.equals("dormindo") && !useTilt ? 200 : 33);
        }
    };
    private void clamp() {
        if (pet == null || !pet.isAttachedToWindow()||playing) return;
        android.graphics.Rect bounds;
        if (Build.VERSION.SDK_INT >= 30) bounds = wm.getCurrentWindowMetrics().getBounds();
        else { android.util.DisplayMetrics metrics = new android.util.DisplayMetrics(); wm.getDefaultDisplay().getMetrics(metrics); bounds = new android.graphics.Rect(0, 0, metrics.widthPixels, metrics.heightPixels); }
        int maxX = Math.max(0, bounds.width() - pos.width), maxY = Math.max(0, bounds.height() - pos.height - 36);
        world.bounds(bounds.width(),bounds.height());
        if (pos.x <= 0 || pos.x >= maxX) { impact(vx); vx = -vx*.7f; xFloat=Math.max(0,Math.min(maxX,pos.x)); }
        if (pos.y >= maxY) { impact(vy); vy=tiltEnabled?-Math.abs(vy)*.4f:(vy>100?-vy*.3f:0); yFloat=maxY; }
        if(pos.y<0){impact(vy);vy=Math.abs(vy)*.5f;yFloat=0;}
        pos.x = Math.max(0, Math.min(maxX, pos.x)); pos.y = Math.max(0, Math.min(maxY, pos.y));
        try { wm.updateViewLayout(pet, pos); } catch (IllegalArgumentException ignored) { }
    }
    private void applySettings() {
        android.content.SharedPreferences prefs=getSharedPreferences("pet",0);
        boolean nextFixed=prefs.getBoolean("fixed",false);if(nextFixed!=fixed){vx=vy=0;xFloat=pos.x;yFloat=pos.y;if(pet!=null)pet.bodyAngle=0;}fixed=nextFixed;
        float scale=prefs.getInt("size",100)/100f, density=getResources().getDisplayMetrics().density;
        pos.width=Math.round(150*density*scale);pos.height=Math.round(122*density*scale);
        userSpeed=prefs.getInt("speed",100)/100f;
        tiltEnabled=prefs.getBoolean("tilt",false);
        tiltSensitivity=prefs.getInt("sensitivity",100)/100f;
        updateMotion();
    }
    private void startArcade(){
        if(closed||!running||playing||pet==null)return;
        beforeGameX=pos.x;beforeGameY=pos.y;storePosition();pressed=dragging=false;vx=vy=0;quickMenu.dismiss();world.clearMessage();world.show(false);playing=true;updateMotion();
        arcade=new BrickGameOverlay(this,wm,pet,(x,y,w,h)->{if(closed||!playing)return;pos.x=x;pos.y=y;pos.width=w;pos.height=h;xFloat=x;yFloat=y;if(pet.isAttachedToWindow())try{wm.updateViewLayout(pet,pos);}catch(IllegalArgumentException ignored){}},this::finishArcade,value->event(value.equals("restart")?"game_start":"game_"+value));
        if(!arcade.show()){finishArcade();android.widget.Toast.makeText(this,"Não foi possível abrir o jogo. Confira a permissão de sobreposição.",android.widget.Toast.LENGTH_LONG).show();return;}
        try{if(pet.isAttachedToWindow())wm.removeView(pet);wm.addView(pet,pos);}catch(RuntimeException error){arcade.close();return;}
        event("release");event("game_start");
    }
    private void finishArcade(){
        playing=false;arcade=null;pos.x=beforeGameX;pos.y=beforeGameY;xFloat=pos.x;yFloat=pos.y;vx=vy=0;pet.setScaleX(1);pet.setScaleY(1);pet.bodyAngle=0;pet.caressing=false;
        if(closed)return;
        applySettings();try{if(!pet.isAttachedToWindow())wm.addView(pet,pos);clamp();}catch(RuntimeException error){fail(error);return;}
        pet.emotion=emotion;pet.notebook=notebook;pet.corner=inCorner;world.show(screenOn);updateMotion();storePosition();event("game_end");
    }
    private void setFixed(boolean enabled){
        getSharedPreferences("pet",0).edit().putBoolean("fixed",enabled).apply();
        vx=vy=0;xFloat=pos.x;yFloat=pos.y;storePosition();applySettings();
        android.widget.Toast.makeText(this,enabled?"Posição fixa. Arraste para escolher outro lugar.":"Posição solta. Movimento normal restaurado.",android.widget.Toast.LENGTH_SHORT).show();
    }
    private int displayRotation(){return wm.getDefaultDisplay().getRotation();}
    private void updateMotion(){if(motion!=null){if(tiltEnabled&&screenOn&&!closed&&!playing)motion.start();else motion.stop();}}
    private void impact(float velocity){
        if(Math.abs(velocity)>350*getResources().getDisplayMetrics().density && SystemClock.uptimeMillis()-lastImpact>800){lastImpact=SystemClock.uptimeMillis();event("impact");}
    }
    private void storePosition() { if (pos != null) getSharedPreferences("pet", 0).edit().putInt("x", pos.x).putInt("y", pos.y).apply(); }
    private void fail(Exception e) {
        android.util.Log.e("Pestinha", "Pet failed", e);
        getSharedPreferences("pet", 0).edit().putString("error", String.valueOf(e.getMessage())).apply(); stopSelf();
    }
    @Override public void onConfigurationChanged(Configuration c) { super.onConfigurationChanged(c); pressed=dragging=false; if(quickMenu!=null)quickMenu.dismiss(); if(motion!=null)motion.resetCalibration();if(playing&&arcade!=null)arcade.resized();else clamp(); }
    @Override public int onStartCommand(Intent i, int flags, int id) {
        if (i != null && "stop".equals(i.getAction())) stopSelf();
        else if(i!=null&&"ball".equals(i.getAction())&&world!=null) world.toggleBall(pos.x,pos.y);
        else if(i!=null&&"calibrate".equals(i.getAction())&&motion!=null){motion.calibrate(displayRotation());vx=vy=0;}
        else if(i!=null&&"reset_tilt".equals(i.getAction())&&motion!=null){motion.resetCalibration();vx=vy=0;}
        else if(i!=null&&"settings".equals(i.getAction())) { if(!playing){applySettings();clamp();} }
        else if(i!=null&&"arcade".equals(i.getAction()))startArcade();
        else if(i!=null&&"appearance".equals(i.getAction())){try{pet.appearance(Characters.appearance(this,petId));}catch(Exception ignored){}}
        else if(i!=null&&"export".equals(i.getAction())&&worker!=null){
            String uri=i.getStringExtra("uri");worker.post(()->{save();try{
                MemoryActivity.backup(this,android.net.Uri.parse(uri));ui.post(()->android.widget.Toast.makeText(this,"Backup salvo.",android.widget.Toast.LENGTH_LONG).show());
            }catch(Exception e){ui.post(()->android.widget.Toast.makeText(this,"Erro no backup: "+e.getMessage(),android.widget.Toast.LENGTH_LONG).show());}});
        }
        else if (i != null && i.getAction() != null) event(i.getAction());
        return START_NOT_STICKY;
    }
    @Override public void onDestroy() {
        if(instance==this)instance=null;
        closed = true; running = false;if(arcade!=null)arcade.close();playing=false;ui.removeCallbacksAndMessages(null); storePosition();
        if (receiverRegistered) unregisterReceiver(screen);
        if(usageMonitor!=null)usageMonitor.stop();if(newsMonitor!=null)newsMonitor.stop();
        if(motion!=null)motion.stop();
        if(quickMenu!=null)quickMenu.dismiss();
        if(world!=null)world.close();
        if (pet != null && pet.isAttachedToWindow()) wm.removeView(pet);
        if (worker != null) { worker.removeCallbacks(tick); worker.post(() -> { save(); busy=false; thread.quitSafely(); }); } else busy=false;
        stopForeground(STOP_FOREGROUND_REMOVE); super.onDestroy();
    }
    @Override public IBinder onBind(Intent i) { return null; }
}
