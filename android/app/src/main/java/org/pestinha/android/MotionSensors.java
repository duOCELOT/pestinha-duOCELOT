package org.pestinha.android;

import android.content.Context;
import android.hardware.*;
import android.os.SystemClock;
import android.view.Surface;

/** Registers only while enabled and screen-on. No raw samples are persisted. */
final class MotionSensors implements SensorEventListener {
    interface Shake { void detected(); }
    final SensorManager manager;
    final Sensor gravity, accelerometer, gyro;
    final Shake shake;
    boolean active=false, hasGravity=false, initialized=false;
    float gx=0,gy=0,gz=9.81f, spin=0,zeroX=0,zeroY=0;
    long lastSample=0,lastShake=0;
    MotionSensors(Context context,Shake callback) {
        manager=context.getSystemService(SensorManager.class);shake=callback;
        gravity=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        accelerometer=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyro=manager==null?null:manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }
    boolean available(){return gravity!=null||accelerometer!=null;}
    void start(){
        if(active||manager==null||!available())return;
        initialized=false;lastSample=0;spin=0;
        boolean registered=false;
        if(gravity!=null)registered=manager.registerListener(this,gravity,SensorManager.SENSOR_DELAY_GAME);
        hasGravity=registered;
        if(accelerometer!=null)registered=manager.registerListener(this,accelerometer,SensorManager.SENSOR_DELAY_GAME)||registered;
        if(gyro!=null)manager.registerListener(this,gyro,SensorManager.SENSOR_DELAY_GAME);
        active=registered;
        if(!active)manager.unregisterListener(this);
    }
    void stop(){if(manager!=null)manager.unregisterListener(this);active=false;initialized=false;spin=0;}
    void calibrate(int rotation){float[] g=TiltPhysics.screenGravity(gx,gy,rotation);zeroX=g[0];zeroY=g[1];}
    void resetCalibration(){zeroX=zeroY=0;}
    float[] force(int rotation){
        if(!active||!initialized||SystemClock.elapsedRealtime()-lastSample>600)return new float[]{0,0};
        float[] g=TiltPhysics.screenGravity(gx,gy,rotation);
        return new float[]{TiltPhysics.deadZone(g[0]-zeroX),TiltPhysics.deadZone(g[1]-zeroY)};
    }
    float angle(int rotation){float[] f=force(rotation);float roll=(float)Math.toDegrees(Math.atan2(f[0],Math.max(2,Math.abs(f[1]))));return Math.max(-18,Math.min(18,roll*.25f-spin*5));}
    @Override public void onSensorChanged(SensorEvent e){
        if(!active)return;
        int type=e.sensor.getType();long now=SystemClock.elapsedRealtime();
        if(type==Sensor.TYPE_GYROSCOPE){if(Float.isFinite(e.values[2]))spin=Math.max(-10,Math.min(10,e.values[2]));return;}
        float x=e.values[0],y=e.values[1],z=e.values[2];
        if(!Float.isFinite(x)||!Float.isFinite(y)||!Float.isFinite(z))return;
        if(type==Sensor.TYPE_GRAVITY){gx=x;gy=y;gz=z;initialized=true;lastSample=now;return;}
        if(type==Sensor.TYPE_ACCELEROMETER){
            if(!hasGravity){float k=initialized?.12f:1;gx+=(x-gx)*k;gy+=(y-gy)*k;gz+=(z-gz)*k;initialized=true;lastSample=now;}
            float magnitude=(float)Math.sqrt(x*x+y*y+z*z);
            if(Math.abs(magnitude-9.81f)>10&&now-lastShake>2000){lastShake=now;shake.detected();}
        }
    }
    @Override public void onAccuracyChanged(Sensor sensor,int accuracy){}
}
