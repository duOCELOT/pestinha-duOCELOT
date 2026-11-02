package org.pestinha.android;

import android.app.*;
import android.graphics.*;
import android.os.Build;
import android.view.*;
import android.widget.*;
import java.util.*;
import org.json.*;

/** Daily native topic picker, with explicit save before any subscription/network change. */
final class InterestsDialog {
    static volatile boolean visible=false;
    private static final int[] COLORS={0xff78d995,0xff76b8ef,0xffe8c87c,0xffb2dfa0,0xffc1a5f1,0xff7ed6dd,0xffefa68d};
    static Dialog show(Activity activity,Runnable saved){
        if(activity.isFinishing()||activity.isDestroyed()||visible)return null;visible=true;Dialog dialog=new Dialog(activity);dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);dialog.setTitle("Seus interesses de hoje");
        Set<String> selected=new LinkedHashSet<>();JSONArray old=NewsRepository.array(activity,"radarTopics");for(int i=0;i<old.length();i++)selected.add(old.optString(i));
        LinearLayout panel=Design.column(activity);panel.setPadding(Design.dp(activity,20),Design.dp(activity,22),Design.dp(activity,20),Design.dp(activity,16));panel.setBackground(Design.shape(activity,Design.CARD,28,Design.BORDER));
        Design.label(panel,"SEU RADAR • HOJE");TextView title=Design.text(activity,"O que desperta sua\ncuriosidade hoje?",25,Design.TEXT);panel.addView(title);TextView hint=Design.text(activity,"Escolha os assuntos. Eu encontro as manchetes e trago para perto de você.",14,Design.MUTED);hint.setPadding(0,Design.dp(activity,10),0,Design.dp(activity,14));panel.addView(hint);
        ScrollView scroll=new ScrollView(activity);scroll.setClipToPadding(false);LinearLayout cards=Design.column(activity);scroll.addView(cards);panel.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        TextView count=Design.text(activity,"",13,Design.MUTED);ArrayList<LinearLayout> tiles=new ArrayList<>();ArrayList<TextView> marks=new ArrayList<>();
        Runnable paint=()->{count.setText(selected.size()+" assuntos • 1 fonte oficial por assunto");for(int i=0;i<tiles.size();i++){boolean on=selected.contains(NewsRepository.TOPICS[i]);LinearLayout tile=tiles.get(i);tile.setBackground(Design.touch(activity,on?Color.rgb(32,49,38):Design.RAISED,on?COLORS[i]:Design.BORDER));tile.setSelected(on);tile.setContentDescription(NewsRepository.TITLES[i]+", "+NewsRepository.SOURCES[i]+(on?", selecionado":", não selecionado"));if(Build.VERSION.SDK_INT>=30)tile.setStateDescription(on?"Selecionado":"Não selecionado");marks.get(i).setText(on?"✓":"+");marks.get(i).setTextColor(on?COLORS[i]:Design.MUTED);}};
        LinearLayout row=null;
        for(int i=0;i<NewsRepository.TOPICS.length;i++){
            if(i%2==0){row=new LinearLayout(activity);LinearLayout.LayoutParams rp=new LinearLayout.LayoutParams(-1,-2);rp.bottomMargin=Design.dp(activity,10);cards.addView(row,rp);}
            final int index=i;LinearLayout tile=Design.column(activity);tile.setMinimumHeight(Design.dp(activity,112));tile.setFocusable(true);tile.setClickable(true);tile.setTag("topic:"+NewsRepository.TOPICS[i]);tile.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);tile.setPadding(Design.dp(activity,12),Design.dp(activity,12),Design.dp(activity,12),Design.dp(activity,12));
            LinearLayout top=new LinearLayout(activity);top.setGravity(Gravity.CENTER_VERTICAL);TopicIcon icon=new TopicIcon(activity,i,COLORS[i]);top.addView(icon,new LinearLayout.LayoutParams(Design.dp(activity,26),Design.dp(activity,26)));TextView check=Design.text(activity,"+",19,Design.MUTED);check.setGravity(Gravity.RIGHT);top.addView(check,new LinearLayout.LayoutParams(0,-2,1));tile.addView(top);
            TextView label=Design.text(activity,NewsRepository.TITLES[i],16,Design.TEXT);label.setPadding(0,Design.dp(activity,6),0,Design.dp(activity,2));tile.addView(label);TextView source=Design.text(activity,NewsRepository.SOURCES[i],11,Design.MUTED);tile.addView(source);
            for(int child=0;child<tile.getChildCount();child++)tile.getChildAt(child).setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
            LinearLayout.LayoutParams tp=new LinearLayout.LayoutParams(0,-2,1);if(i%2==0)tp.rightMargin=Design.dp(activity,8);row.addView(tile,tp);tiles.add(tile);marks.add(check);tile.setOnClickListener(v->{String name=NewsRepository.TOPICS[index];if(!selected.remove(name))selected.add(name);paint.run();});
        }
        if(row!=null&&row.getChildCount()==1)row.addView(new View(activity),new LinearLayout.LayoutParams(0,1,1));
        count.setPadding(0,Design.dp(activity,8),0,Design.dp(activity,4));panel.addView(count);
        CheckBox automatic=new CheckBox(activity);automatic.setText("Pet comenta as manchetes automaticamente");automatic.setChecked(NewsRepository.prefs(activity).getBoolean("radarAuto",true));panel.addView(automatic);Design.styleTree(automatic);
        TextView privacy=Design.text(activity,"Salvar ajusta os RSS oficiais e permite consultar essas fontes pela internet. URLs personalizadas continuam. Alguns feeds são em inglês.",11,Design.MUTED);panel.addView(privacy);
        TextView error=Design.text(activity,"",12,0xffffb4a2);error.setVisibility(View.GONE);panel.addView(error);
        Design.button(panel,"Salvar meu radar","primary",()->{try{NewsRepository.select(activity,selected,automatic.isChecked());dialog.dismiss();saved.run();}catch(Exception problem){error.setText(problem.getMessage());error.setVisibility(View.VISIBLE);}});
        Design.button(panel,"Agora não","secondary",()->{NewsRepository.skipToday(activity);dialog.dismiss();});
        paint.run();android.util.DisplayMetrics metrics=activity.getResources().getDisplayMetrics();View dialogContent=panel;if(metrics.heightPixels/metrics.density<650||activity.getResources().getConfiguration().fontScale>1.3f){scroll.setLayoutParams(new LinearLayout.LayoutParams(-1,-2));ScrollView outer=new ScrollView(activity);outer.addView(panel);dialogContent=outer;}dialog.setContentView(dialogContent);dialog.setOnDismissListener(d->{visible=false;NewsRepository.skipToday(activity);});dialog.show();Window window=dialog.getWindow();if(window!=null){window.setBackgroundDrawableResource(android.R.color.transparent);window.setLayout(metrics.widthPixels-Design.dp(activity,24),(int)(metrics.heightPixels*.9f));}return dialog;
    }
    private static final class TopicIcon extends View {
        final int topic,color;final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        TopicIcon(android.content.Context c,int t,int tint){super(c);topic=t;color=tint;}
        @Override protected void onDraw(Canvas canvas){float size=Math.min(getWidth(),getHeight())/24f;canvas.save();canvas.scale(size,size);p.setColor(color);p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(1.5f);p.setStrokeCap(Paint.Cap.ROUND);
            if(topic==0){canvas.drawRoundRect(3,4,21,21,2,2,p);for(int y=9;y<20;y+=4)canvas.drawLine(7,y,17,y,p);}
            else if(topic==1){canvas.drawCircle(12,12,9,p);canvas.drawOval(8,3,16,21,p);canvas.drawLine(3,12,21,12,p);}
            else if(topic==2){Path roof=new Path();roof.moveTo(3,8);roof.lineTo(12,3);roof.lineTo(21,8);canvas.drawPath(roof,p);canvas.drawLine(3,21,21,21,p);for(int x=6;x<20;x+=6)canvas.drawLine(x,10,x,18,p);}
            else if(topic==3){canvas.drawLine(3,3,3,21,p);canvas.drawLine(3,21,21,21,p);Path trend=new Path();trend.moveTo(6,16);trend.lineTo(11,11);trend.lineTo(15,14);trend.lineTo(21,5);canvas.drawPath(trend,p);}
            else if(topic==4){canvas.drawCircle(12,12,9,p);canvas.drawLine(10,5,10,19,p);canvas.drawArc(8,6,17,12,-90,180,false,p);canvas.drawArc(8,11,17,18,-90,180,false,p);}
            else if(topic==5){canvas.drawRoundRect(6,6,18,18,2,2,p);for(int x=8;x<=16;x+=4){canvas.drawLine(x,3,x,6,p);canvas.drawLine(x,18,x,21,p);canvas.drawLine(3,x,6,x,p);canvas.drawLine(18,x,21,x,p);}}
            else {canvas.drawCircle(12,12,2,p);for(int rotation=0;rotation<3;rotation++){canvas.save();canvas.rotate(rotation*60,12,12);canvas.drawOval(2,8,22,16,p);canvas.restore();}}canvas.restore();
        }
    }
}
