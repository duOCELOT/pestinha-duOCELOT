package org.pestinha.android;

import android.graphics.Color;
import java.util.Locale;

/** One colour shared by HEX, additive RGB, complementary CMY and HSL controls. */
final class ColorState {
    int color;float hue,saturation,lightness;
    ColorState(String hex){if(!hex(hex))rgb(99,229,111);}
    boolean hex(String text){if(!text.matches("#[0-9a-fA-F]{6}"))return false;color=Color.parseColor(text);read();return true;}
    String hex(){return String.format(Locale.ROOT,"#%06X",color&0xffffff);}
    int rgb(int channel){return channel==0?Color.red(color):channel==1?Color.green(color):Color.blue(color);}
    int cmy(int channel){return Math.round((1-rgb(channel)/255f)*100);}
    void channel(int channel,int value){int[] rgb={rgb(0),rgb(1),rgb(2)};rgb[channel]=Math.max(0,Math.min(255,value));rgb(rgb[0],rgb[1],rgb[2]);}
    void complementary(int channel,int percent){channel(channel,Math.round(255*(1-Math.max(0,Math.min(100,percent))/100f)));}
    void rgb(int r,int g,int b){color=Color.rgb(r,g,b);read();}
    private void read(){
        float r=Color.red(color)/255f,g=Color.green(color)/255f,b=Color.blue(color)/255f,max=Math.max(r,Math.max(g,b)),min=Math.min(r,Math.min(g,b)),d=max-min;
        lightness=(max+min)/2;saturation=d==0?0:d/(1-Math.abs(2*lightness-1));
        if(d>0){hue=60*(max==r?((g-b)/d)%6:max==g?(b-r)/d+2:(r-g)/d+4);if(hue<0)hue+=360;}
    }
    void hsl(float h,float s,float l){
        hue=(h%360+360)%360;saturation=Math.max(0,Math.min(1,s));lightness=Math.max(0,Math.min(1,l));
        float c=(1-Math.abs(2*lightness-1))*saturation,x=c*(1-Math.abs((hue/60)%2-1)),m=lightness-c/2;
        float r=0,g=0,b=0;if(hue<60){r=c;g=x;}else if(hue<120){r=x;g=c;}else if(hue<180){g=c;b=x;}else if(hue<240){g=x;b=c;}else if(hue<300){r=x;b=c;}else{r=c;b=x;}
        color=Color.rgb(Math.round((r+m)*255),Math.round((g+m)*255),Math.round((b+m)*255));
    }
}
