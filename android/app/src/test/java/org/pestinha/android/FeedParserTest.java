package org.pestinha.android;

import org.junit.Test;
import static org.junit.Assert.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner.class)
@org.robolectric.annotation.Config(sdk=28,manifest=org.robolectric.annotation.Config.NONE)
public class FeedParserTest {
    private List<FeedParser.Item> parse(String xml) throws Exception {return FeedParser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)),"https://example.org/feed");}
    @Test public void rssPreservesSourceDateAndReadableEntry() throws Exception {
        FeedParser.Item i=parse("<rss><channel><title>Source</title><item><title>A &amp; B</title><link>/story</link><description><![CDATA[<b>Excerpt</b>]]></description><pubDate>Today</pubDate></item></channel></rss>").get(0);
        assertEquals("A & B",i.title);assertEquals("https://example.org/story",i.link);assertEquals("Today",i.date);assertEquals("<b>Excerpt</b>",i.snippet);
    }
    @Test public void atomSelectsAlternateLinkAndNestedSummary() throws Exception {
        FeedParser.Item i=parse("<feed xmlns='http://www.w3.org/2005/Atom'><entry><title>Hello</title><link rel='self' href='/api'/><link rel='alternate' href='/article'/><summary><div>Some <b>text</b></div></summary></entry></feed>").get(0);
        assertEquals("https://example.org/article",i.link);assertTrue(i.snippet.contains("text"));
    }
    @Test public void unsafeLinkCannotLaunchADeviceAction() throws Exception {
        FeedParser.Item i=parse("<rss><channel><item><title>Unsafe</title><link>intent://action</link></item></channel></rss>").get(0);
        assertEquals("https://example.org/feed",i.link);
    }
    @Test public void rejectsDoctypeBeforeEntityExpansion() throws Exception {
        try{parse("<!DOCTYPE rss [<!ENTITY x SYSTEM 'file:///etc/passwd'>]><rss><channel><item><title>&x;</title></item></channel></rss>");fail("DTD must be rejected");}catch(IOException|org.xmlpull.v1.XmlPullParserException expected){}
    }
    @Test public void capsEntriesAndText() throws Exception {
        StringBuilder xml=new StringBuilder("<rss><channel>");for(int n=0;n<100;n++)xml.append("<item><title>").append("a".repeat(5000)).append("</title></item>");xml.append("</channel></rss>");
        List<FeedParser.Item> list=parse(xml.toString());assertEquals(30,list.size());assertTrue(list.get(0).title.length()<=4096);
    }
    @Test public void requiresHttpsWithoutCredentials() throws Exception {
        for(String invalid:new String[]{"http://example.org/feed","file:///etc/passwd","https://user:password@example.org/feed","javascript:alert(1)"}){
            try{FeedParser.https(invalid);fail("URL should be rejected");}catch(Exception expected){}
        }
    }
}
