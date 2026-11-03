package org.pestinha.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import java.io.InputStream;
import java.util.LinkedHashMap;

/** Shared, bounded artwork cache. Recolour only saturated frame pixels, preserving texture. */
final class PetArt {
    private static Bitmap atlas;
    private static final LinkedHashMap<String,Bitmap> cache=new LinkedHashMap<>(8,.75f,true);
    static synchronized Bitmap load(Context context,String id,int frame){
        String key=id+":"+frame;
        if(cache.containsKey(key))return cache.get(key);
        try {
            if(atlas==null)try(InputStream in=context.getAssets().open("art/pets-hd.png")){atlas=BitmapFactory.decodeStream(in);}
            if(atlas==null)return null;
            int index=Characters.index(id),w=atlas.getWidth()/3,h=atlas.getHeight()/2;
            Bitmap image=Bitmap.createBitmap(atlas,(index%3)*w,(index/3)*h,w,h).copy(Bitmap.Config.ARGB_8888,true);
            int[] pixels=new int[w*h];image.getPixels(pixels,0,w,0,0,w,h);
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
}
