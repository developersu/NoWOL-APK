package com.blogspot.developersu.nowol.nowol;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.widget.RemoteViews;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;


public class SendRequestService extends IntentService {

    private static final int STATE_ON = 1;
    private static final int STATE_OFF = 0;
    private static final int STATE_UNKNOWN = -1;

    private ResultReceiver resReceiver;

    public SendRequestService() {
        super("MyIntentService");
    }

    private void sendData(int state, int awID){
        // MainActivity requested
       if (awID == 0) {
           //  Log.d("qwerty1212", "MainActivity case. Status = " + Integer.toString(state) + " awID = " + Integer.toString(awID));
           resReceiver.send(state, null);
       }

       RemoteViews rv = new RemoteViews(getPackageName(), R.layout.no_wol_widget);
       AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);

       switch (state){
           case STATE_ON:
               rv.setTextViewText(R.id.widgetStatusText, getResources().getString(R.string.statusOnline));
               rv.setInt(R.id.widgetHeaderLayout, "setBackgroundColor", ContextCompat.getColor(this, R.color.colorPrimary));
             //  Log.d("qwerty1212", "case 1 widget");
               break;
           case STATE_OFF:
               rv.setTextViewText(R.id.widgetStatusText, getResources().getString(R.string.statusOffline));
               rv.setInt(R.id.widgetHeaderLayout, "setBackgroundColor", ContextCompat.getColor(this, R.color.colorRed));
             //  Log.d("qwerty1212", "case 0 widget");
               break;
           case STATE_UNKNOWN:
               rv.setTextViewText(R.id.widgetStatusText, getResources().getString(R.string.noResponse));
               rv.setInt(R.id.widgetHeaderLayout, "setBackgroundColor", ContextCompat.getColor(this, R.color.colorOrange));
             //  Log.d("qwerty1212", "case -1 widget");
               break;
       }
       appWidgetManager.updateAppWidget(awID, rv);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null)
            return;

        // Log.d("service", "Got intent");
        Bundle bndle = intent.getExtras();
        String url = bndle.getString("url");
        resReceiver = bndle.getParcelable("reciever");
        final int awID = bndle.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);

        // Log.d("qwerty1212", "got from " + Integer.toString(awID) +" "+ url);

        RequestQueue queueStd = Volley.newRequestQueue(this);
        StringRequest strRequest = new StringRequest(Request.Method.GET, url, //will be 4 requests
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.contains("00c600")){
                            sendData(STATE_ON, awID);
                        } else {
                            sendData(STATE_OFF, awID);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                sendData(STATE_UNKNOWN, awID);
            }
        });
        queueStd.add(strRequest);
    }
}
