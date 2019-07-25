package com.blogspot.developersu.nowol.nowol.com.blogspot.developersu.nowol.nowol.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.blogspot.developersu.nowol.nowol.R;
import com.blogspot.developersu.nowol.nowol.SendRequestService;

public class WidgetConfigurator extends AppCompatActivity {

    private SeekBar opacityBar;
    private Switch bkgoundSwitch;

    private void generateWidgetAndDie(int awID, Context context){
        SharedPreferences.Editor settingsEditor;

        RemoteViews rv = new RemoteViews(this.getPackageName(), R.layout.no_wol_widget);
        AppWidgetManager awm = AppWidgetManager.getInstance(this);

        // get shared preferences
        SharedPreferences sharedSettings = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
        String hostIP = sharedSettings.getString("Host", context.getResources().getString(R.string.hostNameDefault));
        // set setting editor to store background color set for all our widgets
        settingsEditor = sharedSettings.edit();

        // @TODO set pending intents linkage to buttons
        Intent refreshIntent = new Intent(context, SendRequestService.class);
        refreshIntent.putExtra("url", hostIP);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
        PendingIntent refreshPendingIntent = PendingIntent.getService(context, awID+1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widgetBntRefresh, refreshPendingIntent);
        //reset
        Intent resetIntent = new Intent(context, SendRequestService.class);
        resetIntent.putExtra("url", hostIP + "/?RESET=on");
        resetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
        PendingIntent resetPendingIntent = PendingIntent.getService(context, awID+2, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widgetBtnReset, resetPendingIntent);
        //power
        Intent powerIntent = new Intent(context, SendRequestService.class);
        powerIntent.putExtra("url", hostIP + "/?POWER0=on");
        powerIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
        PendingIntent powerPendingIntent = PendingIntent.getService(context, awID+3, powerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widgetBtnPwr, powerPendingIntent);
        //power5
        Intent power5Intent = new Intent(context, SendRequestService.class);
        power5Intent.putExtra("url", hostIP + "/?POWER1=on");
        power5Intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
        PendingIntent power5PendingIntent = PendingIntent.getService(context, awID+4, power5Intent, PendingIntent.FLAG_UPDATE_CURRENT);
        rv.setOnClickPendingIntent(R.id.widgetBtnPwr5, power5PendingIntent);

        if (bkgoundSwitch.isChecked()){
            rv.setInt(R.id.widgetBasicLayout, "setBackgroundColor", Color.argb(255-opacityBar.getProgress()*255/100, 0xff,0xff,0xff));
            settingsEditor.putInt("WidgetBgColor", Color.argb(255-opacityBar.getProgress()*255/100, 0xff,0xff,0xff));
            Log.d("qwerty1212", Integer.toString(Color.argb(255-opacityBar.getProgress()*255/100, 0xff,0xff,0xff)));
        }
        else{
            rv.setInt(R.id.widgetBasicLayout, "setBackgroundColor", Color.argb(255-opacityBar.getProgress()*255/100, 0x00,0x00,0x00));
            settingsEditor.putInt("WidgetBgColor", Color.argb(255-opacityBar.getProgress()*255/100, 0x00,0x00,0x00));
            Log.d("qwerty1212", Integer.toString(Color.argb(255-opacityBar.getProgress()*255/100, 0x00,0x00,0x00)));
        }
        settingsEditor.commit();

        awm.updateAppWidget(awID,rv);
        // Send intent to widget
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, awID);
        setResult(RESULT_OK, resultIntent);

        //send intent to service to request widget status
        startService(refreshIntent);

        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_configurator);

        Button submitBtn = findViewById(R.id.configBtnSubmit);
        opacityBar = findViewById(R.id.configSeekBar);
        bkgoundSwitch = findViewById(R.id.configSwitch);
        final TextView opacityLbl = findViewById(R.id.configOpacityLbl);

        // Prepare seekBar element
        opacityLbl.setText(getString(R.string.confOpacity) + " " + Integer.toString(opacityBar.getProgress())+"%");
        opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opacityLbl.setText(getString(R.string.confOpacity) + " " + Integer.toString(i) + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        bkgoundSwitch.setText(getString(R.string.confWhite));

        // Cover widget intent
        Intent initialConfIntent = getIntent();
        Bundle initialConfIntentBundle = initialConfIntent.getExtras();

        // Set logic
        if (initialConfIntentBundle != null){
            final int awID = initialConfIntentBundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

            submitBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Log.d("qwerty1212", Integer.toString(opacityBar.getProgress()*255/100));
                    generateWidgetAndDie(awID, getApplicationContext());
                }
            });

        } else
            finish();
    }
}
