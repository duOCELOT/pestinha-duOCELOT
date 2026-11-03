package org.pestinha.android;

import android.content.Context;
import org.json.JSONObject;
import java.io.InputStream;

/** One registry for selection, safe memory paths and definition-based backup validation. */
final class Characters {
    private static final String[] IDS={"bryan","jorge","mimir","luna","lula"};
    static String id(int index){return index>=0&&index<IDS.length?IDS[index]:IDS[0];}
    static int index(String id){for(int i=0;i<IDS.length;i++)if(IDS[i].equals(id))return i;return 0;}
    static String normalize(String id){return IDS[index(id)];}
    static JSONObject definition(Context context,String id) throws Exception {
        try(InputStream in=context.getAssets().open("pets/"+normalize(id)+".json")){return new JSONObject(MemoryActivity.read(in));}
    }
    static JSONObject appearance(Context c,String id) throws Exception {
        JSONObject a=new JSONObject(definition(c,id).getJSONObject("aparencia").toString());
        android.content.SharedPreferences p=c.getSharedPreferences("appearance_"+normalize(id),0);
        for(String key:new String[]{"moldura","olho"}){String value=p.getString(key,"");if(value.matches("#[0-9a-fA-F]{6}"))a.put(key,value);}
        String item=p.getString("item","original");if(!item.equals("original")){JSONObject items=new JSONObject();switch(item){case "laco":items.put("laco","#eb86bf");break;case "elmo_viking":items.put("elmo_viking","#77929c");break;case "oculos":items.put("oculos","#b3dce1");break;case "gravata":items.put("gravata","#b3261e");break;case "barba":items.put("barba","#e8e9e6");break;default:break;}a.put("acessorios",items);}return a;
    }
    static String name(Context context,String id){try{return definition(context,id).getString("nome");}catch(Exception e){String safe=normalize(id);return Character.toUpperCase(safe.charAt(0))+safe.substring(1);}}
    static String[] names(Context context){String[] names=new String[IDS.length];for(int i=0;i<IDS.length;i++)names[i]=name(context,IDS[i]);return names;}
}
