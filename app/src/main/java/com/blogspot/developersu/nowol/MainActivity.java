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

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.JobIntentService;
import androidx.core.content.ContextCompat;
import com.blogspot.developersu.nowol.widget.NoWolWidget;
import com.google.android.material.snackbar.Snackbar;

import static com.blogspot.developersu.nowol.SendRequestService.JOB_ID;
import static com.blogspot.developersu.nowol.ServerReplies.*;

public class MainActivity extends AppCompatActivity {
    private TextView hostAddress,
                     statusLbl;

    private Intent SendRequestIntent;

    private int status = -2;

    private SharedPreferences settings;

    private class MyResultReciever extends ResultReceiver{

        MyResultReciever(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            status = resultCode;
            updateServerStatusText(resultCode);
            showSnackBarNotice(resultCode);
        }
    }

    private void doRequest(String url) {
        SendRequestIntent.putExtra("url", url);
        JobIntentService.enqueueWork(getApplicationContext(), SendRequestService.class, JOB_ID, SendRequestIntent);
    }

    private void inform(String textToShow){
        Snackbar.make(findViewById(android.R.id.content), textToShow, Snackbar.LENGTH_SHORT).show();
    }

    // provide toolbar options
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater infMenu = getMenuInflater();
        infMenu.inflate(R.menu.toolbar_main, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("STATE", status);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyResultReciever myReciever = new MyResultReciever(null);
        SendRequestIntent = new Intent(this, SendRequestService.class);
        SendRequestIntent.putExtra("receiver", myReciever);

        // toolbar setup start
        Toolbar toolBar = findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        getSupportActionBar();
        // toolbar setup end

        hostAddress = findViewById(R.id.hostNameStaticMain);
        statusLbl = findViewById(R.id.statusTxtMainAct);

        if (savedInstanceState != null){
            status = savedInstanceState.getInt("STATE");
            updateServerStatusText(status);
        }

        Button powerBtn = findViewById(R.id.pwrBntMainAct);
        Button power5Btn = findViewById(R.id.pwr5BntMainAct);
        Button resetBtn = findViewById(R.id.resBntMainAct);
        // Set request queue for Volley

        settings = getSharedPreferences("NoWolPreferences", MODE_PRIVATE);
        hostAddress.setText(settings.getString("Host", getResources().getString(R.string.hostNameDefault)));

        final Button.OnClickListener ActionButtonsListener = event -> {
            switch (event.getId()) {
                case R.id.pwrBntMainAct:
                    doRequest(hostAddress.getText().toString() + "/?POWER0=on");
                    break;
                case R.id.pwr5BntMainAct:
                    doRequest(hostAddress.getText().toString() + "/?POWER1=on");
                    break;
                case R.id.resBntMainAct:
                    doRequest(hostAddress.getText().toString() + "/?RESET=on");
            }
        };
        //    RequestQueue
        powerBtn.setOnClickListener(ActionButtonsListener);
        power5Btn.setOnClickListener(ActionButtonsListener);
        resetBtn.setOnClickListener(ActionButtonsListener);
    }

    private void updateServerStatusText(int status){
        switch (status) {
            case STATE_ON:
                statusLbl.setText(getResources().getString(R.string.statusOnline));
                statusLbl.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                break;
            case STATE_OFF:
                statusLbl.setText(getResources().getString(R.string.statusOffline));
                statusLbl.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
                break;
            case STATE_NO_REPLY:
                statusLbl.setText(getResources().getString(R.string.noResponse));
                statusLbl.setTextColor(hostAddress.getTextColors());
                break;
        }
    }
    private void showSnackBarNotice(int status) {
        switch (status) {
            case STATE_ON:
                inform(getResources().getString(R.string.statusOnline));
                break;
            case STATE_OFF:
                inform(getResources().getString(R.string.statusOffline));
                break;
            case STATE_NO_REPLY:
                inform(getResources().getString(R.string.noResponse) + hostAddress.getText().toString());
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()){
            case R.id.refreshMenu:          /* Button requests status */
                doRequest(hostAddress.getText().toString());
                break;
            case R.id.changeHostMenu:       /* Button requests pop-up window */
                showSettingsDialog();
        }
        return true;
    }

    private void showSettingsDialog(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.settings_popup);
        dialog.setTitle(getString(R.string.popupTitle));
        EditText input = (EditText) dialog.findViewById(R.id.input);
        input.setText(hostAddress.getText());
        Button confirmButton = (Button) dialog.findViewById(R.id.confirm);
        confirmButton.setOnClickListener(event -> {
            updateAllUsingNewIp(input.getText());
            dialog.dismiss();
        });
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        dialog.show();
    }
    private void updateAllUsingNewIp(Editable newIpAddress){
        hostAddress.setText(newIpAddress);
        // TODO:fix
        inform(getResources().getString(R.string.hostLblMain) + newIpAddress);
        settings.edit().putString("Host", newIpAddress.toString()).apply();

        /* Update widgets by sending broadcast intent */
        Intent updateWidgetIntent = new Intent(this, NoWolWidget.class);
        updateWidgetIntent.setAction(NoWolWidget.ACTION_APPWIDGET_REBUILD);
        sendBroadcast(updateWidgetIntent);
    }
}
