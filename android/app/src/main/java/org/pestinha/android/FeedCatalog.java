package org.pestinha.android;

import android.content.Context;
import org.json.*;
import java.io.InputStream;

final class FeedCatalog {
    static JSONArray entries(Context c) throws Exception {try(InputStream in=c.getAssets().open("feed-catalog.json")){return new JSONObject(MemoryActivity.read(in)).getJSONArray("feeds");}}
    static String name(Context c,String url){try{JSONArray list=entries(c);for(int i=0;i<list.length();i++){JSONObject row=list.getJSONObject(i);if(row.getString("url").equals(url))return row.getString("name");}}catch(Exception ignored){}return url;}
}
