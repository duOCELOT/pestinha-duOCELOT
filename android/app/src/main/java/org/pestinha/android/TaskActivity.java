package org.pestinha.android;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.os.*;
import android.widget.*;
import org.json.*;
import java.text.DateFormat;
import java.util.*;

/** Task entry and manual progress. The pet never marks work complete by inference. */
public class TaskActivity extends Activity {
    private LinearLayout rows;private boolean formOpen=false;
    private final Handler ui=new Handler(Looper.getMainLooper());
    private final Runnable refresh=new Runnable(){public void run(){render();ui.postDelayed(this,30000);}};
    private void toast(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
    private void text(LinearLayout parent,String value,int size){TextView v=Design.text(this,value,size,size>=18?Design.TEXT:Design.MUTED);v.setPadding(0,Design.dp(this,8),0,Design.dp(this,10));parent.addView(v);}
    @Override public void onCreate(Bundle state){super.onCreate(state);LocalAssistant.channel(this);Tasks.rearm(this);ScrollView scroll=new ScrollView(this);LinearLayout layout=Design.column(this);layout.setPadding(Design.dp(this,22),Design.dp(this,28),Design.dp(this,22),Design.dp(this,40));scroll.addView(layout);
        text(layout,"O que você tem para fazer?",28);text(layout,"Conte a tarefa e o prazo. Eu lembro e pergunto como está indo; você me diz quando começou e quando terminou.",16);
        Design.button(layout,"Contar uma nova tarefa","primary",()->form(null));
        CheckBox invitation=new CheckBox(this);invitation.setText("O pet pode me perguntar uma vez por dia");invitation.setChecked(Tasks.prefs(this).getBoolean("invite",true));invitation.setOnCheckedChangeListener((b,on)->Tasks.prefs(this).edit().putBoolean("invite",on).apply());layout.addView(invitation);
        Design.button(layout,"Pedir ao pet para perguntar agora","secondary",()->{if(PetService.running)PetService.taskNotice(invitation(),null,true);else toast("Solte o pet para receber o convite junto dele.");});
        Design.button(layout,"Permitir notificações das tarefas","secondary",()->{if(Build.VERSION.SDK_INT>=33&&checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS},40);else startActivity(new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName()));});
        text(layout,"Perguntas periódicas: entre 08h e 22h. Avisos de uma hora antes e do prazo podem tocar no horário escolhido, inclusive à noite. Se ficar pendente após o prazo, há um aviso por dia. Bloqueios do Android/economia de bateria podem atrasar avisos. Sem notificações autorizadas, as tarefas ficam salvas, mas não há aviso de fundo.",14);
        rows=Design.column(this);layout.addView(rows);setContentView(scroll);Design.install(this,scroll);render();if(getIntent().getBooleanExtra("new",false))ui.post(()->form(null));
    }
    static String invitation(){return "O que você tem para fazer hoje? Toque aqui e me conte a tarefa e o prazo.";}
    private void run(Change action){try{action.apply();render();}catch(Exception e){toast(e.getMessage());}}
    interface Change{void apply() throws Exception;}
    private void render(){if(rows==null)return;rows.removeAllViews();JSONArray all=Tasks.list(this);ArrayList<JSONObject> tasks=new ArrayList<>();for(int i=0;i<all.length();i++)if(all.optJSONObject(i)!=null)tasks.add(all.optJSONObject(i));String focused=getIntent().getStringExtra("task");tasks.sort((a,b)->{if(a.optString("id").equals(b.optString("id")))return 0;if(a.optString("id").equals(focused))return -1;if(b.optString("id").equals(focused))return 1;int state=Boolean.compare(a.optString("status").equals("done"),b.optString("status").equals("done"));return state!=0?state:Long.compare(a.optLong("due"),b.optLong("due"));});
        if(tasks.isEmpty())text(rows,"Ainda sem tarefas. Me conte a primeira!",18);
        for(JSONObject task:tasks){String id=task.optString("id"),status=task.optString("status");boolean done=status.equals("done");LinearLayout card=Design.card(this,rows);Design.label(card,done?"CONCLUÍDA":status.equals("doing")?"EM ANDAMENTO":"A FAZER");text(card,task.optString("title"),20);text(card,"Prazo: "+DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date(task.optLong("due")))+"\nAcompanhada por "+Characters.name(this,task.optString("character")),14);
            if(!done){long left=task.optLong("due")-System.currentTimeMillis();text(card,left<0?"Prazo passou há cerca de "+MemoryActivity.duration(-left/1000):"Faltam cerca de "+MemoryActivity.duration(left/1000),16);int hours=task.optInt("hours");text(card,(hours==0?"Sem perguntas periódicas":"Perguntas a cada "+hours+" h, no período diurno")+(task.optLong("snoozeUntil")>System.currentTimeMillis()?"\nPróximo aviso adiado por você.":""),14);
                if(!status.equals("doing"))Design.button(card,"Já comecei","secondary",()->run(()->Tasks.update(this,id,"doing")));
                Design.button(card,"Concluí a tarefa","primary",()->run(()->{Tasks.update(this,id,"done");PetService.assistantNotice("Tarefa concluída. Boa! Vamos guardar essa conquista.",null);}));
                Design.button(card,"Adiar só o aviso por 30 min","secondary",()->run(()->Tasks.update(this,id,"snooze")));
                Design.button(card,"Editar tarefa / mudar prazo","secondary",()->form(Tasks.find(this,id)));
            }
            Design.button(card,"Apagar tarefa…","danger",()->new AlertDialog.Builder(this).setTitle("Apagar esta tarefa?").setMessage(task.optString("title")).setNegativeButton("Cancelar",null).setPositiveButton("Apagar",(d,w)->run(()->Tasks.delete(this,id))).show());
        }
    }
    private void form(JSONObject task){if(formOpen)return;formOpen=true;LinearLayout content=Design.column(this);content.setPadding(Design.dp(this,20),Design.dp(this,8),Design.dp(this,20),0);EditText title=new EditText(this);title.setHint("Ex.: entregar relatório");title.setFilters(new android.text.InputFilter[]{new android.text.InputFilter.LengthFilter(160)});title.setText(task==null?"":task.optString("title"));content.addView(title);
        final long[] deadline={task==null?System.currentTimeMillis()+86400000L:task.optLong("due")};Button date=new Button(this);content.addView(date);Runnable showDate=()->date.setText("Prazo: "+DateFormat.getDateTimeInstance(DateFormat.SHORT,DateFormat.SHORT).format(new Date(deadline[0])));showDate.run();date.setOnClickListener(v->{Calendar cal=Calendar.getInstance();cal.setTimeInMillis(deadline[0]);new DatePickerDialog(this,(picker,y,m,d)->{Calendar chosen=Calendar.getInstance();chosen.set(y,m,d);new TimePickerDialog(this,(clock,h,min)->{chosen.set(Calendar.HOUR_OF_DAY,h);chosen.set(Calendar.MINUTE,min);chosen.set(Calendar.SECOND,0);chosen.set(Calendar.MILLISECOND,0);deadline[0]=chosen.getTimeInMillis();showDate.run();},cal.get(Calendar.HOUR_OF_DAY),cal.get(Calendar.MINUTE),true).show();},cal.get(Calendar.YEAR),cal.get(Calendar.MONTH),cal.get(Calendar.DAY_OF_MONTH)).show();});
        text(content,"Perguntar pelo andamento:",14);Spinner interval=new Spinner(this);interval.setAdapter(new ArrayAdapter<String>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Só avisos do prazo","A cada 1 hora","A cada 2 horas","A cada 4 horas","Uma vez por dia"}));int[] hours={0,1,2,4,24};int selected=2;if(task!=null){selected=0;for(int i=0;i<hours.length;i++)if(hours[i]==task.optInt("hours"))selected=i;}interval.setSelection(selected);content.addView(interval);Design.styleTree(content);
        ScrollView scroll=new ScrollView(this);scroll.addView(content);AlertDialog dialog=new AlertDialog.Builder(this).setTitle(task==null?"Me conte sua tarefa":"Reorganizar tarefa").setView(scroll).setNegativeButton("Cancelar",null).setPositiveButton("Salvar",null).create();dialog.setOnDismissListener(d->formOpen=false);dialog.setOnShowListener(d->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{try{Tasks.save(this,task==null?null:task.optString("id"),title.getText().toString(),deadline[0],hours[interval.getSelectedItemPosition()]);dialog.dismiss();render();toast(LocalAssistant.notificationsAllowed(this)?"Tarefa salva. Vou acompanhar com você.":"Tarefa salva. Autorize notificações para receber os avisos.");}catch(Exception error){title.setError(error.getMessage());}}));dialog.show();
    }
    @Override protected void onNewIntent(Intent intent){super.onNewIntent(intent);setIntent(intent);render();if(intent.getBooleanExtra("new",false))form(null);}
    @Override protected void onResume(){super.onResume();ui.post(refresh);}
    @Override protected void onPause(){ui.removeCallbacks(refresh);super.onPause();}
    @Override protected void onDestroy(){ui.removeCallbacksAndMessages(null);super.onDestroy();}
}
