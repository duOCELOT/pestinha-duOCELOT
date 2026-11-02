package org.pestinha.android;

import android.Manifest;
import android.app.*;
import android.content.*;
import android.graphics.Color;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.view.View;
import android.widget.*;

public class MainActivity extends Activity {
    private TextView status;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable refresh = new Runnable() {
        public void run() { updateStatus(); handler.postDelayed(this, 1500); }
    };
    private int dp(int n) { return Math.round(n * getResources().getDisplayMetrics().density); }
    private static String readText(java.io.InputStream in) throws java.io.IOException {
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buffer = new byte[8192]; int count;
        while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
        return out.toString("UTF-8");
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(16, 29, 37));
        getWindow().setNavigationBarColor(Color.rgb(16, 29, 37));
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(16, 29, 37));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(26), dp(30), dp(26), dp(36));
        scroll.addView(layout);
        text(layout, "PESTINHA / ANDROID", 13, Color.rgb(115, 230, 202));
        text(layout, "Um pequeno caos.\nAgora no seu bolso.", 32, Color.WHITE);
        text(layout, "Bryan • versão experimental 0.1", 16, Color.LTGRAY);
        PetView preview = new PetView(this);
        layout.addView(preview, new LinearLayout.LayoutParams(dp(180), dp(140)));
        status = text(layout, "Pronto para conhecer você.", 17, Color.WHITE);
        text(layout, "Toque para cutucar. Dois toques fazem carinho. Arraste para levar junto. Segure para abrir este painel.", 16, Color.LTGRAY);
        button(layout, "1. Permitir aparecer sobre os apps", v -> {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName())));
        });
        button(layout, "2. Soltar o Bryan", v -> {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "Primeiro permita aparecer sobre outros apps.", Toast.LENGTH_LONG).show();
                return;
            }
            if (Build.VERSION.SDK_INT >= 33 && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 7);
            }
            try {
                startForegroundService(new Intent(this, PetService.class));
                Toast.makeText(this, "Preparando o cérebro…", Toast.LENGTH_SHORT).show();
            } catch (RuntimeException error) {
                new AlertDialog.Builder(this).setMessage("Não foi possível iniciar: " + error.getMessage())
                        .setPositiveButton("OK", null).show();
            }
        });
        button(layout, "Fazer carinho", v -> send("caress"));
        button(layout, "Dormir / acordar", v -> send("sleep"));
        button(layout, "Guardar o pet", v -> stopService(new Intent(this, PetService.class)));
        text(layout, "Tudo fica neste aparelho. Sem internet, leitura de tela ou acesso aos seus aplicativos. A animação pausa com a tela apagada. Alguns apps do sistema podem ocultar o pet.", 14, Color.LTGRAY);
        button(layout, "Sobre o projeto e licença", v -> new AlertDialog.Builder(this)
                .setTitle("Pestinha Android • GPL-3.0-or-later")
                .setMessage("Adaptação experimental de mille-okws/pestinha. Rede neural, emoções e homeostase originais; interface Android nova. Código-fonte: github.com/mille-okws/pestinha (original). Esta adaptação inclui seus fontes na entrega.\n\nAinda sem memes, bolinha ou leitura de janelas. A versão Android não é uma versão oficial do autor.\n\n" + license())
                .setPositiveButton("Fechar", null).show());
        setContentView(scroll);
    }
    private String license() {
        try (java.io.InputStream in = getAssets().open("LICENSE")) {
            return readText(in);
        } catch (Exception e) { return "GNU GPL versão 3 ou posterior."; }
    }
    private void send(String action) {
        if (!PetService.running) {
            Toast.makeText(this, "Solte o Bryan primeiro.", Toast.LENGTH_SHORT).show();
            return;
        }
        startService(new Intent(this, PetService.class).setAction(action));
    }
    private TextView text(LinearLayout parent, String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value); view.setTextSize(size); view.setTextColor(color);
        view.setPadding(0, dp(10), 0, dp(14)); parent.addView(view); return view;
    }
    private void button(LinearLayout parent, String title, View.OnClickListener listener) {
        Button b = new Button(this); b.setText(title); b.setAllCaps(false);
        b.setOnClickListener(listener);
        parent.addView(b, new LinearLayout.LayoutParams(-1, dp(58)));
    }
    private void updateStatus() {
        String error = getSharedPreferences("pet", 0).getString("error", "");
        if (!error.isEmpty()) status.setText("Não foi possível iniciar: " + error);
        else if (PetService.running) status.setText("Bryan está " + PetService.emotion + ".\nEnergia: " + Math.round(PetService.energy * 100) + "%");
        else status.setText("Bryan está guardado. Sua memória continua salva.");
    }
    @Override protected void onResume() { super.onResume(); handler.post(refresh); }
    @Override protected void onPause() { handler.removeCallbacks(refresh); super.onPause(); }
}
