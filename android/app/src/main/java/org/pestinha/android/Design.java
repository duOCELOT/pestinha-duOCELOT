package org.pestinha.android;

import android.app.Activity;
import android.content.*;
import android.content.res.ColorStateList;
import android.graphics.*;
import android.graphics.drawable.*;
import android.view.*;
import android.widget.*;

/** Native visual language based on the original pet's green frame and dark screen. */
final class Design {
    static final int BG=Color.rgb(16,21,18),CARD=Color.rgb(23,30,25),RAISED=Color.rgb(31,42,34),BORDER=Color.rgb(48,66,53),ACCENT=Color.rgb(99,229,111),TEXT=Color.rgb(238,246,239),MUTED=Color.rgb(160,179,164);
    static int dp(Context c,float value){return Math.round(value*c.getResources().getDisplayMetrics().density);}
    static GradientDrawable shape(Context c,int color,float radius,int stroke){GradientDrawable d=new GradientDrawable();d.setColor(color);d.setCornerRadius(dp(c,radius));if(stroke!=0)d.setStroke(dp(c,1),stroke);return d;}
    static android.graphics.drawable.Drawable touch(Context c,int color,int stroke){return new RippleDrawable(ColorStateList.valueOf(Color.argb(45,99,229,111)),shape(c,color,16,stroke),shape(c,Color.WHITE,16,0));}
    static TextView text(Context c,String title,int size,int color){TextView v=new TextView(c);v.setText(title);v.setTextSize(size);v.setTextColor(color);v.setLineSpacing(dp(c,2),1);return v;}
    static LinearLayout column(Context c){LinearLayout v=new LinearLayout(c);v.setOrientation(LinearLayout.VERTICAL);return v;}
    static LinearLayout card(Context c,LinearLayout parent){LinearLayout v=column(c);v.setBackground(shape(c,CARD,24,BORDER));v.setPadding(dp(c,18),dp(c,18),dp(c,18),dp(c,18));LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.bottomMargin=dp(c,14);parent.addView(v,p);return v;}
    static void label(LinearLayout parent,String text){TextView v=text(parent.getContext(),text,13,MUTED);v.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));v.setLetterSpacing(.06f);v.setPadding(0,0,0,dp(parent.getContext(),12));parent.addView(v);}
    static Button button(LinearLayout parent,String label,String style,Runnable action){Context c=parent.getContext();Button b=new Button(c);b.setText(label);b.setTag(style);LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.topMargin=dp(c,6);parent.addView(b,p);styleButton(b);b.setOnClickListener(v->action.run());return b;}
    static LinearLayout fold(Activity activity,LinearLayout parent,String key,String title,String subtitle,boolean initiallyOpen){
        LinearLayout card=card(activity,parent);
        LinearLayout heading=column(activity);heading.setTag("category:"+key);heading.setFocusable(true);heading.setClickable(true);heading.setMinimumHeight(dp(activity,56));
        TextView label=text(activity,"",18,TEXT);heading.addView(label);
        TextView detail=text(activity,subtitle,13,MUTED);detail.setPadding(0,dp(activity,4),0,dp(activity,8));heading.addView(detail);card.addView(heading);
        LinearLayout body=column(activity);body.setTag("body:"+key);card.addView(body);
        String pref=activity.getClass().getSimpleName()+"/"+key;
        android.content.SharedPreferences saved=activity.getSharedPreferences("ui_sections",0);
        Runnable sync=()->{boolean open=body.getVisibility()==View.VISIBLE;label.setText(title+(open?"   −":"   +"));heading.setContentDescription(title+". "+subtitle+(open?". Expandido. Toque para recolher.":". Recolhido. Toque para expandir."));};
        body.setVisibility(saved.getBoolean(pref,initiallyOpen)?View.VISIBLE:View.GONE);sync.run();
        heading.setOnClickListener(v->{boolean open=body.getVisibility()!=View.VISIBLE;body.setVisibility(open?View.VISIBLE:View.GONE);saved.edit().putBoolean(pref,open).apply();sync.run();});return body;
    }
    static void reveal(LinearLayout body){if(body.getVisibility()!=View.VISIBLE)((ViewGroup)body.getParent()).getChildAt(0).performClick();}
    static void styleButton(Button b){Context c=b.getContext();String style=String.valueOf(b.getTag());boolean primary=style.equals("primary"),danger=style.equals("danger");b.setAllCaps(false);b.setTextSize(15);b.setTypeface(Typeface.create("sans-serif-medium",Typeface.NORMAL));b.setTextColor(primary?BG:danger?Color.rgb(255,164,150):TEXT);b.setBackground(touch(c,primary?ACCENT:RAISED,primary?0:BORDER));b.setMinHeight(dp(c,50));b.setMinimumHeight(dp(c,50));b.setPadding(dp(c,14),dp(c,10),dp(c,14),dp(c,10));b.setStateListAnimator(null);}
    static void styleTree(View view){
        if(view instanceof CheckBox){CheckBox b=(CheckBox)view;b.setTextColor(TEXT);b.setButtonTintList(ColorStateList.valueOf(ACCENT));b.setMinHeight(dp(view.getContext(),48));b.setPadding(0,dp(view.getContext(),8),0,dp(view.getContext(),8));}
        else if(view instanceof Button){styleButton((Button)view);if(view.getLayoutParams() instanceof ViewGroup.MarginLayoutParams){ViewGroup.MarginLayoutParams lp=(ViewGroup.MarginLayoutParams)view.getLayoutParams();lp.topMargin=Math.max(lp.topMargin,dp(view.getContext(),6));view.setLayoutParams(lp);}}
        else if(view instanceof EditText){EditText e=(EditText)view;e.setTextColor(TEXT);e.setHintTextColor(MUTED);e.setBackgroundTintList(ColorStateList.valueOf(ACCENT));e.setPadding(dp(view.getContext(),8),dp(view.getContext(),12),dp(view.getContext(),8),dp(view.getContext(),12));}
        else if(view instanceof SeekBar){((SeekBar)view).setProgressTintList(ColorStateList.valueOf(ACCENT));((SeekBar)view).setThumbTintList(ColorStateList.valueOf(ACCENT));}
        if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++)styleTree(g.getChildAt(i));}
    }
    static void install(Activity activity,View root){activity.getWindow().setStatusBarColor(BG);activity.getWindow().setNavigationBarColor(BG);root.setBackgroundColor(BG);styleTree(root);
        root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets.consumeSystemWindowInsets();});root.requestApplyInsets();}
}
