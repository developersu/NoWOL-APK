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
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.blogspot.developersu.nowol.R;
import com.blogspot.developersu.nowol.SendRequestService;


public class NoWolWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)){
            int[] widgetIDs = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            for (int widgetID : widgetIDs){
                updateAppWidget(context, AppWidgetManager.getInstance(context), widgetID);
            }
        }
        else
            super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigurator.deleteBgColorPref(context, appWidgetId);
        }
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        int widgetBgColor = WidgetConfigurator.loadBgColorPref(context, appWidgetId);
        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.no_wol_widget);

        remoteViews.setInt(R.id.widgetBodyLayout, "setBackgroundColor", widgetBgColor);
        //
        SharedPreferences sharedSettings = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
        String hostIP = sharedSettings.getString("Host", context.getResources().getString(R.string.hostNameDefault));

        remoteViews.setOnClickPendingIntent(R.id.widgetBntRefresh,
                createPendingIntent(context, appWidgetId, hostIP, appWidgetId+1)); // Refresh
        remoteViews.setOnClickPendingIntent(R.id.widgetBtnReset,
                createPendingIntent(context, appWidgetId, hostIP + "/?RESET=on", appWidgetId+2));
        remoteViews.setOnClickPendingIntent(R.id.widgetBtnPwr,
                createPendingIntent(context, appWidgetId, hostIP + "/?POWER0=on", appWidgetId+3));
        remoteViews.setOnClickPendingIntent(R.id.widgetBtnPwr5,
                createPendingIntent(context, appWidgetId, hostIP + "/?POWER1=on", appWidgetId+4));

        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        // Ping serv TODO: FIX
        Intent refreshIntent = new Intent(context, SendRequestService.class);
        refreshIntent.putExtra("url", hostIP);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        context.startService(refreshIntent);
        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
    }
    private static PendingIntent createPendingIntent(Context context, int widgetId, String linkAddress, int code){
        Intent intent = new Intent(context, SendRequestService.class);
        intent.putExtra("url", linkAddress);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getService(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }
}
