package org.pestinha.android;

import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.function.Consumer;
import org.json.JSONObject;

final class ColorPickerDialog {
    private final Activity owner;final ColorState state;private final String key;private final JSONObject appearance;
    private EditText hex;private TextView swatch;private PetView pet;private boolean syncing;
    private final SeekBar[] channels=new SeekBar[9];private final TextView[] labels=new TextView[9];
    private final String[] names={"Matiz","Saturação","Luminosidade (HSL)","R · vermelho","G · verde","B · azul","C · ciano","M · magenta","Y · amarelo"};
    private AlertDialog dialog;
    private final android.os.Handler ui=new android.os.Handler(android.os.Looper.getMainLooper());
    private final Runnable updatePreview;
    private ColorPickerDialog(Activity a,JSONObject appearance,String key){owner=a;this.appearance=appearance;this.key=key;state=new ColorState(appearance.optString(key,"#63e56f"));updatePreview=()->{try{JSONObject next=new JSONObject(this.appearance.toString());next.put(this.key,state.hex());pet.appearance(next);}catch(org.json.JSONException ignored){}};}
    static AlertDialog show(Activity a,JSONObject appearance,String key,Consumer<String> save){return new ColorPickerDialog(a,appearance,key).open(save);}
    private AlertDialog open(Consumer<String> save){
        ScrollView scroll=new ScrollView(owner);LinearLayout root=Design.column(owner);root.setTag("color_picker_content");int pad=Design.dp(owner,18);root.setPadding(pad,pad,pad,pad);scroll.addView(root);
        pet=new PetView(owner);root.addView(pet,new LinearLayout.LayoutParams(-1,Design.dp(owner,95)));
        swatch=Design.text(owner,"",18,Design.TEXT);swatch.setGravity(Gravity.CENTER);swatch.setPadding(pad,pad,pad,pad);swatch.setTag("color_swatch");root.addView(swatch);
        LinearLayout hexRow=new LinearLayout(owner);hexRow.setGravity(Gravity.CENTER_VERTICAL);root.addView(hexRow);
        hex=new EditText(owner);hex.setTag("color_hex");hex.setSingleLine();hex.setHint("#RRGGBB");hex.setContentDescription("Cor hexadecimal, cerquilha e seis dígitos");hex.setInputType(android.text.InputType.TYPE_CLASS_TEXT|android.text.InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);hexRow.addView(hex,new LinearLayout.LayoutParams(0,-2,1));
        Button copy=Design.button(hexRow,"Copiar","secondary",()->{owner.getSystemService(android.content.ClipboardManager.class).setPrimaryClip(ClipData.newPlainText("Cor",state.hex()));Toast.makeText(owner,"Cor copiada",Toast.LENGTH_SHORT).show();});copy.setContentDescription("Copiar cor hexadecimal");LinearLayout.LayoutParams copyParams=new LinearLayout.LayoutParams(Design.dp(owner,86),-2);copyParams.leftMargin=Design.dp(owner,8);copy.setLayoutParams(copyParams);
        LinearLayout rgb=Design.fold(owner,root,"color_rgb","RGB","Luz · canais de 0 a 255",true);for(int i=3;i<6;i++)slider(rgb,i,255);
        LinearLayout hsl=Design.fold(owner,root,"color_hsl","Tom e luminosidade","Matiz, saturação e luminosidade HSL",false);for(int i=0;i<3;i++)slider(hsl,i,i==0?360:100);
        LinearLayout cmy=Design.fold(owner,root,"color_cmy","CMY","Complementos de RGB · sem canal preto",false);for(int i=6;i<9;i++)slider(cmy,i,100);
        root.addView(Design.text(owner,"A prévia não altera o pet até salvar. Luminosidade é o controle HSL; CMY é a conversão complementar de RGB, não um perfil de impressão.",13,Design.MUTED));
        hex.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int st,int count,int after){}public void onTextChanged(CharSequence s,int st,int before,int count){if(syncing)return;boolean valid=state.hex(s.toString().trim());hex.setError(valid?null:"Use # e seis dígitos");if(dialog!=null&&dialog.getButton(-1)!=null)dialog.getButton(-1).setEnabled(valid);if(valid)sync(false);}public void afterTextChanged(Editable e){}});
        dialog=new AlertDialog.Builder(owner).setTitle(key.equals("olho")?"Cor dos olhos":"Cor da moldura").setView(scroll).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",null).create();
        dialog.setOnShowListener(v->{Design.styleTree(root);dialog.getWindow().setBackgroundDrawable(Design.shape(owner,Design.BG,22,Design.BORDER));dialog.getButton(-1).setOnClickListener(b->{if(!state.hex(hex.getText().toString().trim())){hex.setError("Use #RRGGBB");return;}save.accept(state.hex());dialog.dismiss();});sync(true);});dialog.setOnDismissListener(v->ui.removeCallbacksAndMessages(null));dialog.show();return dialog;
    }
    private void slider(LinearLayout box,int index,int max){labels[index]=Design.text(owner,"",14,Design.TEXT);box.addView(labels[index]);SeekBar bar=new SeekBar(owner);bar.setMax(max);bar.setTag("color_channel_"+index);bar.setContentDescription(names[index]);channels[index]=bar;box.addView(bar,new LinearLayout.LayoutParams(-1,Design.dp(owner,48)));bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){public void onStartTrackingTouch(SeekBar b){}public void onStopTrackingTouch(SeekBar b){}public void onProgressChanged(SeekBar b,int value,boolean user){if(syncing||!user)return;if(index<3)state.hsl(index==0?value:state.hue,index==1?value/100f:state.saturation,index==2?value/100f:state.lightness);else if(index<6)state.channel(index-3,value);else state.complementary(index-6,value);sync(true);}});}
    private void sync(boolean writeHex){syncing=true;try{if(writeHex){hex.setText(state.hex());hex.setError(null);if(dialog!=null&&dialog.getButton(-1)!=null)dialog.getButton(-1).setEnabled(true);}swatch.setBackground(Design.shape(owner,state.color,14,Design.BORDER));swatch.setText(state.hex()+"   Aa");swatch.setTextColor(state.lightness>.58f?Color.BLACK:Color.WHITE);
        for(int i=0;i<9;i++){int value=i==0?Math.round(state.hue):i==1?Math.round(state.saturation*100):i==2?Math.round(state.lightness*100):i<6?state.rgb(i-3):state.cmy(i-6);channels[i].setProgress(value);labels[i].setText(names[i]+" · "+value+(i==0?"°":i<3||i>5?"%":""));}
        ui.removeCallbacks(updatePreview);ui.postDelayed(updatePreview,80);
    }finally{syncing=false;}}
}
