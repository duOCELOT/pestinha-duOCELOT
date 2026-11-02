package org.pestinha.android;

import org.xmlpull.v1.*;
import java.io.*;
import java.net.URI;
import java.util.*;

/** RSS/Atom text data only. No scripts, embedded browsing or action execution. */
public final class FeedParser {
    public static final class Item {
        public String title="",link="",snippet="",date="";
    }
    public static URI https(String text) throws Exception {
        URI uri=new URI(text.trim()).normalize();
        if(!"https".equalsIgnoreCase(uri.getScheme())||uri.getHost()==null||uri.getRawUserInfo()!=null)
            throw new IOException("Use uma URL HTTPS sem usuário ou senha.");
        return uri;
    }
    private static String text(XmlPullParser p) throws Exception {
        int depth=p.getDepth();StringBuilder b=new StringBuilder();
        for(int t=p.nextToken();t!=XmlPullParser.END_DOCUMENT;t=p.nextToken()){
            if(t==XmlPullParser.DOCDECL)throw new IOException("DTD não permitido.");
            if(t==XmlPullParser.END_TAG&&p.getDepth()==depth)break;
            if((t==XmlPullParser.TEXT||t==XmlPullParser.CDSECT||t==XmlPullParser.ENTITY_REF)&&p.getText()!=null&&b.length()<4096)b.append(p.getText(),0,Math.min(p.getText().length(),4096-b.length()));
        }
        return b.toString().trim();
    }
    public static List<Item> parse(InputStream in,String source) throws Exception {
        URI base=https(source);
        XmlPullParserFactory factory=XmlPullParserFactory.newInstance();factory.setNamespaceAware(true);
        XmlPullParser p=factory.newPullParser();
        p.setFeature(XmlPullParser.FEATURE_PROCESS_DOCDECL,false);p.setInput(in,null);
        ArrayList<Item> result=new ArrayList<>();Item entry=null;int entryDepth=-1;
        for(int t=p.getEventType();t!=XmlPullParser.END_DOCUMENT;t=p.nextToken()){
            if(t==XmlPullParser.DOCDECL)throw new IOException("Feed com DTD não é aceito.");
            if(t==XmlPullParser.START_TAG){
                String name=p.getName();
                if(entry==null&&(name.equals("item")||name.equals("entry"))){entry=new Item();entryDepth=p.getDepth();}
                else if(entry!=null&&p.getDepth()==entryDepth+1){
                    if(name.equals("title"))entry.title=text(p);
                    else if(name.equals("description")||name.equals("summary")||name.equals("content")||name.equals("encoded")) {String s=text(p);if(entry.snippet.isEmpty())entry.snippet=s;}
                    else if(name.equals("pubDate")||name.equals("published")||name.equals("updated"))entry.date=text(p);
                    else if(name.equals("link")){
                        String href=p.getAttributeValue(null,"href"),rel=p.getAttributeValue(null,"rel");
                        if(href==null)entry.link=text(p);
                        else if(rel==null||rel.equals("alternate"))entry.link=href;
                    }
                }
            }else if(t==XmlPullParser.END_TAG&&entry!=null&&p.getDepth()==entryDepth){
                if(!entry.title.isEmpty()){
                    try{entry.link=https(base.resolve(entry.link.trim()).toString()).toString();}catch(Exception badLink){entry.link=base.toString();}
                    result.add(entry);
                }
                entry=null;if(result.size()>=30)break;
            }
        }
        return result;
    }
}
