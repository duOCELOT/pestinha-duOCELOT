package org.pestinha.android;

import android.app.*;
import android.graphics.*;
import android.graphics.drawable.ColorDrawable;
import android.os.Looper;
import android.view.*;
import android.widget.*;
import java.io.ByteArrayOutputStream;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import org.json.*;
import org.junit.*;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.robolectric.*;
import org.robolectric.annotation.*;

@RunWith(RobolectricTestRunner.class) @Config(sdk=28,manifest=Config.NONE,qualifiers="w360dp-h780dp-mdpi") @GraphicsMode(GraphicsMode.Mode.NATIVE)
public class CustomizationTest {
    private Activity activity;
    @Before public void setup(){org.robolectric.android.controller.ActivityController<Activity> controller=Robolectric.buildActivity(Activity.class);controller.get().setTheme(R.style.PestinhaTheme);activity=controller.setup().get();activity.getSharedPreferences("ui_sections",0).edit().clear().commit();}
    @After public void close(){activity.finish();}
    private void preview(String key,View v,int w,int h){v.measure(View.MeasureSpec.makeMeasureSpec(w,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(h,View.MeasureSpec.AT_MOST));h=v.getMeasuredHeight();v.layout(0,0,w,h);Bitmap b=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);Canvas canvas=new Canvas(b);if(!key.startsWith("PROTEST_"))canvas.drawColor(Design.BG);v.draw(canvas);ByteArrayOutputStream out=new ByteArrayOutputStream();b.compress(Bitmap.CompressFormat.PNG,100,out);System.out.println("DESIGN_PREVIEW_"+key+"="+Base64.getEncoder().encodeToString(out.toByteArray()));}
    @Test public void knownRgbCmyHslConversionsAndHexValidation(){ColorState s=new ColorState("#FF0000");assertEquals(0,s.cmy(0));assertEquals(100,s.cmy(1));assertEquals(1,s.saturation,.001);assertEquals(.5,s.lightness,.001);s.hsl(120,1,.5f);assertEquals("#00FF00",s.hex());s.hsl(240,1,.5f);assertEquals("#0000FF",s.hex());s.complementary(2,100);assertEquals("#000000",s.hex());s.hsl(0,0,1);assertEquals("#FFFFFF",s.hex());assertFalse(s.hex("#FF00"));assertEquals("#FFFFFF",s.hex());assertTrue(s.hex("#123abc"));assertEquals("#123ABC",s.hex());}
    @Test public void colorDialogSynchronizesHexAndCancelsWithoutSaving() throws Exception {AtomicReference<String> saved=new AtomicReference<>();AlertDialog d=ColorPickerDialog.show(activity,Characters.appearance(activity,"bryan"),"moldura",saved::set);Shadows.shadowOf(Looper.getMainLooper()).idle();EditText hex=d.findViewById(android.R.id.content).findViewWithTag("color_hex");hex.setText("#FF0000");assertEquals(255,((SeekBar)d.findViewById(android.R.id.content).findViewWithTag("color_channel_3")).getProgress());assertEquals(0,((SeekBar)d.findViewById(android.R.id.content).findViewWithTag("color_channel_4")).getProgress());hex.setText("#12");assertFalse(d.getButton(-1).isEnabled());hex.setText("#336699");assertTrue(d.getButton(-1).isEnabled());Shadows.shadowOf(Looper.getMainLooper()).idleFor(java.time.Duration.ofMillis(100));preview("COLOR_PICKER",d.findViewById(android.R.id.content).findViewWithTag("color_picker_content"),360,1100);d.getButton(-2).performClick();assertNull(saved.get());AlertDialog second=ColorPickerDialog.show(activity,Characters.appearance(activity,"bryan"),"olho",saved::set);Shadows.shadowOf(Looper.getMainLooper()).idle();((EditText)second.findViewById(android.R.id.content).findViewWithTag("color_hex")).setText("#abcdef");second.getButton(-1).performClick();assertEquals("#ABCDEF",saved.get());}
    @Test public void appSearchRetainsSelectionOutsideFilter() throws Exception {AtomicReference<JSONObject> saved=new AtomicReference<>();List<AppPickerDialog.Entry> entries=Arrays.asList(new AppPickerDialog.Entry("example.social","Social",new ColorDrawable(Color.MAGENTA),true),new AppPickerDialog.Entry("example.clock","Relógio",new ColorDrawable(Color.CYAN),true));AlertDialog d=AppPickerDialog.show(activity,entries,new JSONObject().put("example.social","Social"),saved::set);Shadows.shadowOf(Looper.getMainLooper()).idle();EditText search=d.findViewById(android.R.id.content).findViewWithTag("app_search");search.setText("Relógio");ListView list=d.findViewById(android.R.id.content).findViewWithTag("app_list");assertEquals(1,list.getAdapter().getCount());View row=list.getAdapter().getView(0,null,list);((CheckBox)row.findViewWithTag("app:example.clock")).performClick();search.setText("");preview("APP_PICKER",d.findViewById(android.R.id.content).findViewWithTag("app_picker_content"),360,460);d.getButton(-1).performClick();Shadows.shadowOf(Looper.getMainLooper()).idle();assertTrue(saved.get().has("example.social"));assertTrue(saved.get().has("example.clock"));}
    @Test public void protestAssetsHaveTransparentMarginsAndAccessibleCaptions(){for(String style:new String[]{"placard","banner","flag"}){ProtestView sign=new ProtestView(activity,"Queremos carinho!",style);assertTrue(sign.getContentDescription().toString().contains("Queremos carinho"));preview("PROTEST_"+style.toUpperCase(Locale.ROOT),sign,540,332);}}
}
