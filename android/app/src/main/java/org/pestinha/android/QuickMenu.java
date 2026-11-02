package org.pestinha.android;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Build;
import android.view.*;
import android.widget.*;

/** Compact non-modal overlay. Outside taps dismiss and still reach the app below. */
final class QuickMenu {
    interface Action{void selected(String name);}
    final Context context;final WindowManager wm;final Action action;
    View root;
    QuickMenu(Context c,WindowManager manager,Action callback){context=c;wm=manager;action=callback;}
    boolean isShowing(){return root!=null;}
    static int[] place(int screenWidth,int screenHeight,int width,int height,int petX,int petY,int petWidth,int petHeight,int gap){
        int x=Math.max(gap,Math.min(screenWidth-width-gap,petX+petWidth/2-width/2));
        int above=petY-height-gap;int y=above>=gap?above:petY+petHeight+gap;
        return new int[]{Math.max(0,x),Math.max(gap,Math.min(screenHeight-height-gap,y))};
    }
    LinearLayout content(boolean sleeping,boolean working,boolean ball,int columns){
        LinearLayout box=Design.column(context);box.setPadding(Design.dp(context,10),Design.dp(context,10),Design.dp(context,10),Design.dp(context,10));box.setBackground(Design.shape(context,Design.CARD,24,Design.BORDER));box.setElevation(Design.dp(context,8));
        LinearLayout header=new LinearLayout(context);header.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=Design.text(context,"O que vamos fazer?",13,Design.MUTED);title.setPadding(Design.dp(context,5),0,0,0);header.addView(title,new LinearLayout.LayoutParams(0,Design.dp(context,32),1));title.setGravity(Gravity.CENTER_VERTICAL);
        ActionIcon close=new ActionIcon(context,"close");close.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);close.setContentDescription("Fechar menu");close.setBackground(Design.touch(context,Design.CARD,0));header.addView(close,new LinearLayout.LayoutParams(Design.dp(context,48),Design.dp(context,48)));close.setOnClickListener(v->dismiss());box.addView(header);
        boolean fixed=context.getSharedPreferences("pet",0).getBoolean("fixed",false);
        LinearLayout pin=new LinearLayout(context);pin.setGravity(Gravity.CENTER_VERTICAL);pin.setPadding(Design.dp(context,12),Design.dp(context,8),Design.dp(context,12),Design.dp(context,8));pin.setBackground(Design.touch(context,fixed?Design.RAISED:Design.CARD,Design.BORDER));pin.setTag("pin");pin.setFocusable(true);pin.setSelected(fixed);pin.setContentDescription(fixed?"Soltar posição. Reativar movimento do pet.":"Fixar no lugar. Sem gravidade ou movimento automático.");
        ActionIcon pinIcon=new ActionIcon(context,"pin");pin.addView(pinIcon,new LinearLayout.LayoutParams(Design.dp(context,26),Design.dp(context,26)));LinearLayout pinText=Design.column(context);pinText.setPadding(Design.dp(context,10),0,0,0);pinText.addView(Design.text(context,fixed?"Soltar posição":"Fixar no lugar",14,Design.TEXT));pinText.addView(Design.text(context,fixed?"Voltar ao movimento normal":"Ficar onde você soltar • sem gravidade",11,Design.MUTED));pin.addView(pinText,new LinearLayout.LayoutParams(0,-2,1));LinearLayout.LayoutParams pinParams=new LinearLayout.LayoutParams(-1,-2);pinParams.bottomMargin=Design.dp(context,8);box.addView(pin,pinParams);pin.setOnClickListener(v->{dismiss();action.selected("pin");});
        String[] ids={"caress","ball","sleep","work","memory","assistant","panel","stop"};
        String[] labels={"Carinho",ball?"Sem bola":"Bolinha",sleeping?"Acordar":"Dormir",working?"Parar":"Notebook","Memória","Assistente","Painel","Guardar"};
        LinearLayout row=null;
        for(int i=0;i<ids.length;i++){
            if(i%columns==0){row=new LinearLayout(context);box.addView(row,new LinearLayout.LayoutParams(-1,Design.dp(context,76)));}
            String id=ids[i];LinearLayout tile=Design.column(context);tile.setGravity(Gravity.CENTER);tile.setFocusable(true);tile.setContentDescription(labels[i]);tile.setTag(id);tile.setBackground(Design.touch(context,Design.CARD,0));
            ActionIcon icon=new ActionIcon(context,id);if(id.equals("stop"))icon.color=android.graphics.Color.rgb(255,164,150);tile.addView(icon,new LinearLayout.LayoutParams(Design.dp(context,27),Design.dp(context,27)));
            TextView label=Design.text(context,labels[i],11,Design.TEXT);label.setGravity(Gravity.CENTER);label.setPadding(0,Design.dp(context,6),0,0);label.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));tile.addView(label);
            row.addView(tile,new LinearLayout.LayoutParams(0,-1,1));tile.setOnClickListener(v->{dismiss();action.selected(id);});
        }
        if(row!=null){while(row.getChildCount()<columns)row.addView(new View(context),new LinearLayout.LayoutParams(0,-1,1));}
        box.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_OUTSIDE){dismiss();return false;}return false;});return box;
    }
    void show(int petX,int petY,int petWidth,int petHeight,boolean asleep,boolean working,boolean ball){
        dismiss();Rect bounds;if(Build.VERSION.SDK_INT>=30)bounds=wm.getCurrentWindowMetrics().getBounds();else{android.util.DisplayMetrics d=new android.util.DisplayMetrics();wm.getDefaultDisplay().getMetrics(d);bounds=new Rect(0,0,d.widthPixels,d.heightPixels);}
        int gap=Design.dp(context,12),width=Math.min(Design.dp(context,280),bounds.width()-gap*2),columns=width<Design.dp(context,260)?3:4;
        LinearLayout box=content(asleep,working,ball,columns);box.measure(View.MeasureSpec.makeMeasureSpec(width,View.MeasureSpec.EXACTLY),View.MeasureSpec.makeMeasureSpec(0,View.MeasureSpec.UNSPECIFIED));
        int height=box.getMeasuredHeight();View popup=box;
        int available=Math.max(1,bounds.height()-gap*2);if(height>available){ScrollView scroll=new ScrollView(context);scroll.addView(box);scroll.setBackground(Design.shape(context,Design.CARD,24,Design.BORDER));scroll.setOnTouchListener((v,e)->{if(e.getAction()==MotionEvent.ACTION_OUTSIDE){dismiss();return false;}return false;});popup=scroll;height=available;}
        int[] position=place(bounds.width(),bounds.height(),width,height,petX,petY,petWidth,petHeight,gap);
        WindowManager.LayoutParams p=new WindowManager.LayoutParams(width,height,WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE|WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL|WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,android.graphics.PixelFormat.TRANSLUCENT);
        p.gravity=Gravity.TOP|Gravity.LEFT;p.x=position[0];p.y=position[1];
        try{wm.addView(popup,p);root=popup;popup.setAlpha(0);popup.setScaleX(.92f);popup.setScaleY(.92f);popup.animate().alpha(1).scaleX(1).scaleY(1).setDuration(140).start();}catch(RuntimeException e){root=null;}
    }
    void dismiss(){if(root!=null){View old=root;root=null;old.animate().cancel();if(old.isAttachedToWindow())wm.removeView(old);}}
}
