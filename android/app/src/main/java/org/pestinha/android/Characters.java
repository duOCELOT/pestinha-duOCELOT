package org.pestinha.android;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;

/** One registry for selection, safe memory paths and definition-based backup validation. */
final class Characters {
    private static final String[] IDS={"bryan","jorge","mimir"};
    static String id(int index){return index>=0&&index<IDS.length?IDS[index]:IDS[0];}
    static int index(String id){for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;return 0;}
    static String normalize(String id){return IDS[index(id)];}
    static JSONObject definition(Context context,String id) throws Exception {
        try(InputStream in=context.getAssets().open("pets/"+normalize(id)+".json")){return new JSONObject(MemoryActivity.read(in));}
    }
    static String name(Context context,String id){try{return definition(context,id).getString("nome");}catch(Exception e){String safe=normalize(id);return Character.toUpperCase(safe.charAt(0))+safe.substring(1);}}
    static String[] names(Context context){String[] names=new String[IDS.length];for(int i=0;i<IDS.length;i++)names[i]=name(context,IDS[i]);return names;}
}
