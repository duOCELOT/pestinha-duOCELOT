package org.pestinha.android;

import java.io.*;
import java.net.*;
import java.util.*;

final class FeedClient {
    static List<FeedParser.Item> fetch(String address) throws Exception {
        URI uri=FeedParser.https(address);
        for(int redirects=0;redirects<5;redirects++){
            HttpURLConnection c=(HttpURLConnection)uri.toURL().openConnection();
            c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setInstanceFollowRedirects(false);
            c.setRequestProperty("User-Agent","PestinhaAndroid/0.7 (RSS reader)");
            c.setRequestProperty("Accept","application/rss+xml, application/atom+xml, application/xml, text/xml");
            try{
                int code=c.getResponseCode();
                if(code==301||code==302||code==303||code==307||code==308){
                    String location=c.getHeaderField("Location");if(location==null)throw new IOException("Redirecionamento sem destino.");
                    uri=FeedParser.https(uri.resolve(location).toString());continue;
                }
                if(code!=200)throw new IOException("HTTP "+code);
                if(c.getContentLengthLong()>1_500_000)throw new IOException("Feed maior que 1,5 MB.");
                try(InputStream in=c.getInputStream();ByteArrayOutputStream out=new ByteArrayOutputStream()){
                    byte[] buffer=new byte[8192];int n;
                    while((n=in.read(buffer))!=-1){out.write(buffer,0,n);if(out.size()>1_500_000)throw new IOException("Feed maior que 1,5 MB.");}
                    return FeedParser.parse(new ByteArrayInputStream(out.toByteArray()),uri.toString());
                }
            }finally{c.disconnect();}
        }
        throw new IOException("Redirecionamentos demais.");
    }
}
