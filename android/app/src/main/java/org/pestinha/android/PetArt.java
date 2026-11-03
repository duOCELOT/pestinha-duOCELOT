package org.pestinha.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.io.InputStream;
import java.util.LinkedHashMap;

/** Shared, bounded artwork cache. Recolour only saturated frame pixels, preserving texture. */
final class PetArt {
    private static Bitmap atlas,president;
    private static final LinkedHashMap<String,Bitmap> cache=new LinkedHashMap<>(8,.75f,true);
    static synchronized Bitmap load(Context context,String id,int frame){
        String key=id+":"+frame;
        if(cache.containsKey(key))return cache.get(key);
        try {
            Bitmap image;
            if(id.equals("lula")){
                if(president==null)try(InputStream in=context.getAssets().open("art/presidente-hd.png")){
                    BitmapFactory.Options options=new BitmapFactory.Options();options.inSampleSize=2;president=BitmapFactory.decodeStream(in,null,options);
                }
                if(president==null)return null;
                image=president.copy(Bitmap.Config.ARGB_8888,true);
            }else{
                if(atlas==null)try(InputStream in=context.getAssets().open("art/pets-hd.png")){atlas=BitmapFactory.decodeStream(in);}
                if(atlas==null)return null;
                int index=Characters.index(id),cw=atlas.getWidth()/3,ch=atlas.getHeight()/2;
                image=Bitmap.createBitmap(atlas,(index%3)*cw,(index/3)*ch,cw,ch).copy(Bitmap.Config.ARGB_8888,true);
            }
            int w=image.getWidth(),h=image.getHeight();
            int[] pixels=new int[w*h];image.getPixels(pixels,0,w,0,0,w,h);
            clean(pixels,w,h);
            float[] target=new float[3],hsv=new float[3];Color.colorToHSV(frame,target);
            for(int i=0;i<pixels.length;i++){
                int value=pixels[i];if(Color.alpha(value)<8)continue;
                Color.colorToHSV(value,hsv);
                boolean border=id.equals("lula")?(hsv[0]<12||hsv[0]>345):(hsv[0]>75&&hsv[0]<150);
                if(border&&hsv[1]>.55f){hsv[0]=target[0];hsv[1]*=target[1];hsv[2]*=.45f+.55f*target[2];pixels[i]=Color.HSVToColor(Color.alpha(value),hsv);}
            }
            image.setPixels(pixels,0,w,0,0,w,h);cache.put(key,image);
            // Do not recycle: a visible PetView can still hold a previous bitmap.
            while(cache.size()>8)cache.remove(cache.keySet().iterator().next());
            return image;
        }catch(java.io.IOException|IllegalArgumentException e){return null;}
    }
    private static void clean(int[] pixels,int w,int h){
        boolean[] seen=new boolean[pixels.length];int[] queue=new int[pixels.length],best=new int[0];
        for(int start=0;start<pixels.length;start++){
            if(seen[start]||Color.alpha(pixels[start])<64)continue;
            int head=0,tail=1;queue[0]=start;seen[start]=true;
            while(head<tail){int at=queue[head++],x=at%w,y=at/w;
                int[] next={x>0?at-1:-1,x+1<w?at+1:-1,y>0?at-w:-1,y+1<h?at+w:-1};
                for(int n:next)if(n>=0&&!seen[n]&&Color.alpha(pixels[n])>=64){seen[n]=true;queue[tail++]=n;}
            }
            if(tail>best.length)best=java.util.Arrays.copyOf(queue,tail);
        }
        boolean[] keep=new boolean[pixels.length];for(int at:best)keep[at]=true;
        for(int i=0;i<pixels.length;i++)if(!keep[i])pixels[i]=0;
    }
}
