package org.pestinha.android;

public final class TiltPhysicsTest {
    static void check(boolean value,String message){if(!value)throw new AssertionError(message);}
    static void close(float a,float b,float tolerance,String message){check(Math.abs(a-b)<tolerance,message+": "+a+" vs "+b);}
    public static void main(String[] args){
        // The same physical direction must remain horizontal after display rotation.
        float[] portrait=TiltPhysics.screenGravity(3,4,0);
        float[] landscape=TiltPhysics.screenGravity(4,-3,1);
        close(portrait[0],landscape[0],.001f,"Rotation preserves screen horizontal gravity");
        close(portrait[1],landscape[1],.001f,"Rotation preserves screen vertical gravity");
        float[] reverse=TiltPhysics.screenGravity(-3,-4,2);
        close(portrait[0],reverse[0],.001f,"Upside-down mapping");
        check(TiltPhysics.deadZone(.1f)==0,"Tiny sensor noise must not move the pet");
        check(TiltPhysics.deadZone(Float.NaN)==0,"Invalid samples must not contaminate velocity");
        float v=0;
        for(int i=0;i<1000;i++)v=TiltPhysics.velocity(v,100000,.033f,650);
        check(v<=650&&v>0,"Velocity remains bounded under extreme input");
        for(int i=0;i<200;i++)v=TiltPhysics.velocity(v,0,.033f,650);
        check(v<.1f,"Friction settles movement without input");
        v=200;
        for(int i=0;i<50;i++)v=TiltPhysics.velocity(v,-900,.033f,650);
        check(v<0,"Tilting the opposite way reverses motion");
        close(TiltPhysics.velocity(100,900,10,650),TiltPhysics.velocity(100,900,.05f,650),.001f,"Resume cannot integrate a giant frame");
        System.out.println("Tilt physics: rotation, noise, damping, bounds and reversal OK");
    }
}
