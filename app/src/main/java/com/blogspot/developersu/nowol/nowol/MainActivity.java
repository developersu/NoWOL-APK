package com.blogspot.developersu.nowol.nowol;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.ResultReceiver;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import com.blogspot.developersu.nowol.nowol.com.blogspot.developersu.nowol.nowol.widget.NoWolWidget;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity implements popUp.pupUpRetuningValueListener {
    private TextView hostAdress;
    private TextView statusLbl;
    private Button powerBtn;
    private Button power5Btn;
    private Button resetBtn;

    Intent SendRequestIntent;

    private SharedPreferences settings;
    SharedPreferences.Editor settingsEditor;
    // define reciever for the data we got from service
    private class MyResultReciever extends ResultReceiver{

        public MyResultReciever(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            super.onReceiveResult(resultCode, resultData);
            switch (resultCode) {
                case 1:
                    inform(getResources().getString(R.string.statusOnline));
                    statusLbl.setText(getResources().getString(R.string.statusOnline));
                    statusLbl.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimaryDark));
                    break;
                case 0:
                    inform(getResources().getString(R.string.statusOffline));
                    statusLbl.setText(getResources().getString(R.string.statusOffline));
                    statusLbl.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.colorRed));
                    break;
                case -1:
                    inform(getResources().getString(R.string.noResponse) + hostAdress.getText().toString());
                    statusLbl.setText(getResources().getString(R.string.noResponse));
                    statusLbl.setTextColor(hostAdress.getTextColors());
                    break;
                default: break;
            }
        }
    }
    //reciever end
    private void doRequest(String url) {
        SendRequestIntent.putExtra("url", url);
        startService(SendRequestIntent);
    }

    private void inform(String textToShow){
        //Toast.makeText(this, textToShow, Toast.LENGTH_SHORT).show();
        // .setGravity(Gravity.TOP|Gravity.BOTTOM,0,0)
        Snackbar.make(findViewById(android.R.id.content), textToShow, Snackbar.LENGTH_SHORT).show();
        // SOME GOODS APPEARS SUDDENLY android.R.id.content gives you the root element of a view, without having to know its actual name/type/ID.
    }

    // provide toolbar options
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        MenuInflater infMenu = getMenuInflater();
        infMenu.inflate(R.menu.toolbar_main, menu);
        return true;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyResultReciever myReciever = new MyResultReciever(null);
        SendRequestIntent = new Intent(this, SendRequestService.class);
        SendRequestIntent.putExtra("reciever", myReciever);

        // toolbar setup start
        Toolbar toolBar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolBar);
        final ActionBar ActBar = getSupportActionBar();
        // toolbar setup end

        hostAdress = (TextView)findViewById(R.id.hostNameStaticMain);
        statusLbl = (TextView)findViewById(R.id.statusTxtMainAct);
        powerBtn = (Button)findViewById(R.id.pwrBntMainAct);
        power5Btn = (Button)findViewById(R.id.pwr5BntMainAct);
        resetBtn = (Button)findViewById(R.id.resBntMainAct);
        // Set request queue for Volley


        settings = getSharedPreferences("NoWolPreferences", MODE_PRIVATE);
        hostAdress.setText(settings.getString("Host", getResources().getString(R.string.hostNameDefault)));
        settingsEditor = settings.edit();

        final Button.OnClickListener ActionButtonsListener = new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.pwrBntMainAct:
                        doRequest(hostAdress.getText().toString() + "/?POWER0=on");
                        break;
                    case R.id.pwr5BntMainAct:
                        doRequest(hostAdress.getText().toString() + "/?POWER1=on");
                        break;
                    case R.id.resBntMainAct:
                        doRequest(hostAdress.getText().toString() + "/?RESET=on");
                        break;
                    default:
                        break;
                }
            }
        };
        //    RequestQueue
        powerBtn.setOnClickListener(ActionButtonsListener);
        power5Btn.setOnClickListener(ActionButtonsListener);
        resetBtn.setOnClickListener(ActionButtonsListener);
        }

        @Override
        public void onFinishEdit (String hostNameReSet){
            hostAdress.setText(hostNameReSet);
            inform(getResources().getString(R.string.hostLblMain) + hostNameReSet);
            settingsEditor.putString("Host", hostNameReSet);
            settingsEditor.commit();
            /*
            Update widgets by sending broadcast intent
             */
            Intent updateWidgetIntent = new Intent(this, NoWolWidget.class);
            updateWidgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

            AppWidgetManager awm = AppWidgetManager.getInstance(this);
            int[] IDs = awm.getAppWidgetIds(new ComponentName(this, NoWolWidget.class));
            updateWidgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, IDs);
            getApplicationContext().sendBroadcast(updateWidgetIntent);
            /*
            broadcase end
             */
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item){
            switch (item.getItemId()){
                case R.id.refreshMenu:          /* Button requests status */
                    doRequest(hostAdress.getText().toString());
                    break;
                case R.id.changeHostMenu:       /* Button requests pop-up window */
                    popUp N = popUp.newInstance(hostAdress.getText());
                    N.show(this.getSupportFragmentManager(), "tst");
                    break;
                default:
                    //onOptionsItemSelected(item);
                    break;
            }
            return true;
        }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(SendRequestIntent); // just in case
    }
}
