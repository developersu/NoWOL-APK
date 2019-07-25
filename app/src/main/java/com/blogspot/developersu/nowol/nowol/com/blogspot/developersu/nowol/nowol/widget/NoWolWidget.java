package com.blogspot.developersu.nowol.nowol.com.blogspot.developersu.nowol.nowol.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.widget.RemoteViews;

import com.blogspot.developersu.nowol.nowol.R;
import com.blogspot.developersu.nowol.nowol.SendRequestService;


public class NoWolWidget extends AppWidgetProvider {

    private void setRequests(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIDs){
        SharedPreferences sharedSettings = context.getSharedPreferences("NoWolPreferences", Context.MODE_PRIVATE);
        String hostIP = sharedSettings.getString("Host", context.getResources().getString(R.string.hostNameDefault));
        int bgColor = sharedSettings.getInt("WidgetBgColor", Color.BLACK);

        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.no_wol_widget);

        rv.setInt(R.id.widgetBasicLayout, "setBackgroundColor", bgColor);

        for (int appWidgetId : appWidgetIDs) {
            //refresh
            Intent refreshIntent = new Intent(context, SendRequestService.class);
            refreshIntent.putExtra("url", hostIP);
            refreshIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent refreshPendingIntent = PendingIntent.getService(context, appWidgetId+1, refreshIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widgetBntRefresh, refreshPendingIntent);
            //reset
            Intent resetIntent = new Intent(context, SendRequestService.class);
            resetIntent.putExtra("url", hostIP + "/?RESET=on");
            resetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent resetPendingIntent = PendingIntent.getService(context, appWidgetId+2, resetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widgetBtnReset, resetPendingIntent);
            //power
            Intent powerIntent = new Intent(context, SendRequestService.class);
            powerIntent.putExtra("url", hostIP + "/?POWER0=on");
            powerIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent powerPendingIntent = PendingIntent.getService(context, appWidgetId+3, powerIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widgetBtnPwr, powerPendingIntent);
            //power5
            Intent power5Intent = new Intent(context, SendRequestService.class);
            power5Intent.putExtra("url", hostIP + "/?POWER1=on");
            power5Intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            PendingIntent power5PendingIntent = PendingIntent.getService(context, appWidgetId+4, power5Intent, PendingIntent.FLAG_UPDATE_CURRENT);
            rv.setOnClickPendingIntent(R.id.widgetBtnPwr5, power5PendingIntent);

            appWidgetManager.updateAppWidget(appWidgetId, rv);

            context.startService(refreshIntent);
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        setRequests(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, NoWolWidget.class));

        setRequests(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        onEnabled(context);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
    }
}
