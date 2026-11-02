package org.pestinha.android;

/** Pure physics math, in screen coordinates (right/down), independent of Android. */
public final class TiltPhysics {
    private TiltPhysics() {}
    public static float[] screenGravity(float x,float y,int rotation) {
        switch(rotation) {
            case 1: return new float[]{y,x};
            case 2: return new float[]{x,-y};
            case 3: return new float[]{-y,-x};
            default: return new float[]{-x,y};
        }
    }
    public static float deadZone(float value) {
        if(!Float.isFinite(value)||Math.abs(value)<.22f)return 0;
        return Math.max(-9.81f,Math.min(9.81f,value));
    }
    public static float velocity(float previous,float acceleration,float seconds,float maxSpeed) {
        float dt=Math.max(0,Math.min(.05f,seconds));
        float next=(previous+acceleration*dt)*(float)Math.exp(-1.6f*dt);
        return Math.max(-maxSpeed,Math.min(maxSpeed,next));
    }
}
