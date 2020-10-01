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
package com.blogspot.developersu.nowol;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.RemoteViews;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import static com.blogspot.developersu.nowol.ServerReplies.*;

public class SendRequestService extends IntentService {

    private ResultReceiver resReceiver;

    public SendRequestService() {
        super("MyIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle bundle = intent.getExtras();

        if (bundle == null)
            return;

        String url = bundle.getString("url");
        resReceiver = bundle.getParcelable("receiver");
        final int appWidgetId = bundle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

        RequestQueue queueStd = Volley.newRequestQueue(this);

        StringRequest strRequest = new StringRequest(Request.Method.GET, url, //will be 4 requests
                response -> {
                    if (response.contains("00c600"))
                        sendData(STATE_ON, appWidgetId);
                    else
                        sendData(STATE_OFF, appWidgetId);
                }, error -> sendData(STATE_UNKNOWN, appWidgetId));

        queueStd.add(strRequest);
    }

    private void sendData(int state, int widgetId){
        final int greenColor = ContextCompat.getColor(this, R.color.colorPrimary);
        final int redColor = ContextCompat.getColor(this, R.color.colorRed);
        final int orangeColor = ContextCompat.getColor(this, R.color.colorOrange);

        // MainActivity requested
        if (widgetId == 0){
            resReceiver.send(state, null);
        }

        switch (state){
           case STATE_ON:
               setWidgetTextColorIndication(widgetId, getResources().getString(R.string.statusOnline), greenColor);
               break;
           case STATE_OFF:
               setWidgetTextColorIndication(widgetId, getResources().getString(R.string.statusOffline), redColor);
               break;
           case STATE_UNKNOWN:
               setWidgetTextColorIndication(widgetId, getResources().getString(R.string.noResponse), orangeColor);
               break;
        }
    }
    private void setWidgetTextColorIndication(int widgetId, String text, int color){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.no_wol_widget);

        remoteViews.setTextViewText(R.id.widgetStatusText, text);
        remoteViews.setInt(R.id.widgetHeaderLayout, "setBackgroundColor", color);

        appWidgetManager.updateAppWidget(widgetId, remoteViews);
    }
}
