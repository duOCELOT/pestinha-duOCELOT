package org.pestinha.android;

import android.app.*;
import android.graphics.drawable.Drawable;
import android.text.*;
import android.view.*;
import android.widget.*;
import java.util.*;
import java.util.function.Consumer;
import org.json.*;

/** Labels and icons in the UI, package identifiers only in private storage. */
final class AppPickerDialog {
    static final class Entry {final String id,name;final Drawable icon;final boolean available;Entry(String id,String name,Drawable icon,boolean available){this.id=id;this.name=name;this.icon=icon;this.available=available;}}
    static AlertDialog show(Activity a,List<Entry> apps,JSONObject old,Consumer<JSONObject> save){
        LinkedHashSet<String> chosen=new LinkedHashSet<>();for(Entry e:apps)if(old.has(e.id))chosen.add(e.id);
        LinearLayout box=Design.column(a);int pad=Design.dp(a,16);box.setPadding(pad,pad,pad,pad);
        EditText search=new EditText(a);search.setSingleLine();search.setHint("Buscar aplicativo pelo nome");search.setTag("app_search");box.addView(search);
        TextView count=Design.text(a,"",14,Design.MUTED);box.addView(count);Runnable summary=()->count.setText(chosen.size()+" de 20 aplicativos selecionados");summary.run();
        ArrayList<Entry> visible=new ArrayList<>(apps);ListView list=new ListView(a);list.setTag("app_list");list.setDivider(null);list.setEmptyView(null);box.addView(list,new LinearLayout.LayoutParams(-1,Design.dp(a,320)));
        TextView empty=Design.text(a,"Nenhum aplicativo encontrado",15,Design.MUTED);box.addView(empty);list.setEmptyView(empty);
        BaseAdapter adapter=new BaseAdapter(){public int getCount(){return visible.size();}public Object getItem(int p){return visible.get(p);}public long getItemId(int p){return p;}
            public View getView(int position,View reusable,ViewGroup parent){Entry e=visible.get(position);LinearLayout row=new LinearLayout(a);row.setGravity(Gravity.CENTER_VERTICAL);row.setPadding(0,Design.dp(a,8),0,Design.dp(a,8));row.setMinimumHeight(Design.dp(a,64));
                ImageView icon=new ImageView(a);icon.setImageDrawable(e.icon);icon.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);row.addView(icon,new LinearLayout.LayoutParams(Design.dp(a,40),Design.dp(a,40)));
                LinearLayout labels=Design.column(a);labels.setPadding(Design.dp(a,12),0,Design.dp(a,6),0);labels.addView(Design.text(a,e.name,16,Design.TEXT));if(!e.available)labels.addView(Design.text(a,"Indisponível neste perfil",12,Design.MUTED));row.addView(labels,new LinearLayout.LayoutParams(0,-2,1));
                CheckBox check=new CheckBox(a);check.setContentDescription("Acompanhar "+e.name);check.setTag("app:"+e.id);check.setChecked(chosen.contains(e.id));row.addView(check,new LinearLayout.LayoutParams(Design.dp(a,48),Design.dp(a,48)));check.setOnCheckedChangeListener((b,on)->{if(on&&!chosen.contains(e.id)&&chosen.size()>=20){check.setChecked(false);Toast.makeText(a,"Escolha no máximo 20 aplicativos",Toast.LENGTH_SHORT).show();return;}if(on)chosen.add(e.id);else chosen.remove(e.id);summary.run();});row.setOnClickListener(v->check.performClick());Design.styleTree(row);return row;
            }};list.setAdapter(adapter);
        search.addTextChangedListener(new TextWatcher(){public void beforeTextChanged(CharSequence s,int start,int count,int after){}public void onTextChanged(CharSequence s,int start,int before,int count){String q=s.toString().trim().toLowerCase(Locale.ROOT);visible.clear();for(Entry e:apps)if(e.name.toLowerCase(Locale.ROOT).contains(q))visible.add(e);adapter.notifyDataSetChanged();}public void afterTextChanged(Editable e){}});
        AlertDialog dialog=new AlertDialog.Builder(a).setTitle("Aplicativos acompanhados").setView(box).setNegativeButton("Cancelar",null).setPositiveButton("Salvar seleção",(d,w)->{JSONObject selected=new JSONObject();try{for(Entry e:apps)if(chosen.contains(e.id))selected.put(e.id,e.name);save.accept(selected);}catch(JSONException ignored){}}).create();dialog.setOnShowListener(v->{Design.styleTree(box);dialog.getWindow().setBackgroundDrawable(Design.shape(a,Design.BG,20,Design.BORDER));});dialog.show();return dialog;
    }
}
