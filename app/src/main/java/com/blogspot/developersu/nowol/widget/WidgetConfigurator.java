/*
    Copyright 2017-2020 Dmitry Isaenko

    This file is part of NoWOL.

    NoWOL is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    NoWOL is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with NoWOL.  If not, see <https://www.gnu.org/licenses/>.
*/
package com.blogspot.developersu.nowol.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.blogspot.developersu.nowol.R;
import com.blogspot.developersu.nowol.SendRequestService;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class WidgetConfigurator extends AppCompatActivity {

    private SeekBar opacityBar;
    private SwitchMaterial bgColorSwitch;
    private Context context;

    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setResult(RESULT_CANCELED);

        setContentView(R.layout.activity_widget_configurator);

        Button submitBtn = findViewById(R.id.configBtnSubmit);
        opacityBar = findViewById(R.id.configSeekBar);
        bgColorSwitch = findViewById(R.id.configSwitch);
        final TextView opacityLbl = findViewById(R.id.configOpacityLbl);
        opacityLbl.setText(getString(R.string.confOpacity, 0));
        opacityBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                opacityLbl.setText(getString(R.string.confOpacity, i));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        this.context = getApplicationContext();

        // Cover widget intent
        Intent initialConfIntent = getIntent();
        Bundle initialConfBundle = initialConfIntent.getExtras();

        if (initialConfBundle != null) {
            appWidgetId = initialConfBundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }

        submitBtn.setOnClickListener(view -> createWidget());
    }

    private void createWidget(){
        RemoteViews remoteViews = new RemoteViews(this.getPackageName(), R.layout.no_wol_widget);

        SharedPreferences sharedSettings = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
        String hostIP = sharedSettings.getString("Host", context.getResources().getString(R.string.hostNameDefault));

        SharedPreferences.Editor settingsEditor = sharedSettings.edit();

        remoteViews.setOnClickPendingIntent(R.id.widgetBntRefresh,
                createPendingIntent(hostIP, appWidgetId+1)); // Refresh
        remoteViews.setOnClickPendingIntent(R.id.widgetBtnReset,
                createPendingIntent(hostIP + "/?RESET=on", appWidgetId+2));
        remoteViews.setOnClickPendingIntent(R.id.widgetBtnPwr,
                createPendingIntent(hostIP + "/?POWER0=on", appWidgetId+3));
        remoteViews.setOnClickPendingIntent(R.id.widgetBtnPwr5,
                createPendingIntent(hostIP + "/?POWER1=on", appWidgetId+4));
        int bgColor = getBackgoundColor();
        remoteViews.setInt(R.id.widgetBodyLayout, "setBackgroundColor", bgColor);
        settingsEditor.putInt("WidgetBgColor" + appWidgetId, bgColor).apply();

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        // Send intent to widget
        Intent resultIntent = new Intent();
        resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        setResult(RESULT_OK, resultIntent);
        finish();
    }
    private PendingIntent createPendingIntent(String linkAddress, int code){
        Intent intent = new Intent(context, SendRequestService.class);
        intent.putExtra("url", linkAddress);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        return PendingIntent.getService(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
    private int getBackgoundColor(){
        if (bgColorSwitch.isChecked())
            return Color.argb(255-opacityBar.getProgress()*255/100, 0xff,0xff,0xff);
        else
            return Color.argb(255-opacityBar.getProgress()*255/100, 0x00,0x00,0x00);
    }

    static int loadBgColorPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
        return  prefs.getInt("WidgetBgColor" + appWidgetId, Color.argb(255, 0x00,0x00,0x00));
    }

    static void deleteBgColorPref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE).edit();
        prefs.remove("WidgetBgColor" + appWidgetId);
        prefs.apply();
    }
}
