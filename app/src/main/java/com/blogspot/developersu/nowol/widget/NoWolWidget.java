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
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.RemoteViews;
import androidx.core.app.JobIntentService;
import com.blogspot.developersu.nowol.R;
import com.blogspot.developersu.nowol.SendRequestService;

import static com.blogspot.developersu.nowol.SendRequestService.JOB_ID;


public class NoWolWidget extends AppWidgetProvider {
    public static final String ACTION_APPWIDGET_REBUILD = "com.blogspot.developersu.nowol.widget.ACTION_APPWIDGET_REBUILD";
    public static final String ACTION_APPWIDGET_REQUEST = "com.blogspot.developersu.nowol.widget.ACTION_APPWIDGET_REQUEST";

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        setWidgetsFunctionality(context);   // Calls twice when first widget created; Calls once after boot and set all widgets
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            requestServerStatusFromService(context, appWidgetId);
        }
    }
    private static void requestServerStatusFromService(Context context, int appWidgetId) {
        SharedPreferences sharedSettings = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
        String hostIP = sharedSettings.getString("Host", context.getResources().getString(R.string.hostNameDefault));
        requestServerStatusFromServiceByIp(context, appWidgetId, hostIP);
    }
    private static void requestServerStatusFromServiceByIp(Context context, int appWidgetId, String hostIP) {
        // Ping server
        Intent refreshIntent = new Intent(context, SendRequestService.class);
        refreshIntent.putExtra("url", hostIP);
        refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        JobIntentService.enqueueWork(context, SendRequestService.class, JOB_ID, refreshIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (intent.getAction() == null)
            return;

        switch (intent.getAction()){
            case ACTION_APPWIDGET_REBUILD:
                setWidgetsFunctionality(context);
                break;
            case ACTION_APPWIDGET_REQUEST:
                int widgetID = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                String host = intent.getExtras().getString("url");
                if (widgetID != 0){
                    requestServerStatusFromServiceByIp(context, widgetID, host);
                }
        }
    }

    private static void setWidgetsFunctionality(Context context){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, NoWolWidget.class));
        setWidgetsFunctionality(context, appWidgetIds);
    }

    private static void setWidgetsFunctionality(Context context, int[] appWidgetIds){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.no_wol_widget);

        for (int appWidgetId : appWidgetIds){
            int widgetBgColor = WidgetConfigurator.loadBgColorPref(context, appWidgetId);

            remoteViews.setInt(R.id.widgetBodyLayout, "setBackgroundColor", widgetBgColor);
            //
            SharedPreferences sharedSettings = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
            String hostIP = sharedSettings.getString("Host", context.getResources().getString(R.string.hostNameDefault));

            remoteViews.setOnClickPendingIntent(R.id.widgetBntRefresh,
                    createPendingIntent(context, appWidgetId, hostIP, appWidgetId)); // Refresh
            remoteViews.setOnClickPendingIntent(R.id.widgetBtnReset,
                    createPendingIntent(context, appWidgetId, hostIP + "/?RESET=on", appWidgetId+"RESET".hashCode()));
            remoteViews.setOnClickPendingIntent(R.id.widgetBtnPwr,
                    createPendingIntent(context, appWidgetId, hostIP + "/?POWER0=on", appWidgetId+"POWER0".hashCode()));
            remoteViews.setOnClickPendingIntent(R.id.widgetBtnPwr5,
                    createPendingIntent(context, appWidgetId, hostIP + "/?POWER1=on", appWidgetId+"POWER1".hashCode()));

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

            requestServerStatusFromService(context, appWidgetId);
        }
    }
    private static PendingIntent createPendingIntent(Context context, int widgetId, String linkAddress, int code){
        Intent intent = new Intent(context, NoWolWidget.class);
        intent.setAction(ACTION_APPWIDGET_REQUEST);
        intent.putExtra("url", linkAddress);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        return PendingIntent.getBroadcast(context, code, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            WidgetConfigurator.deleteBgColorPref(context, appWidgetId);
        }
    }
}
