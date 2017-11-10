package com.mathieuclement.android.garage_opener.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.mathieuclement.android.garage_opener.app.gps.GpsTrigger;

public class MainActivity extends Activity {

    //private AutoUpdateApk autoUpdateApk;

    private Button openButton;
    private Button closeButton;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        initButtons();
    }

    private void initButtons() {
        openButton = (Button) findViewById(R.id.open_button);
        closeButton = (Button) findViewById(R.id.close_button);

        // Set background colors
        setButtonsBackgroundColor();

        // Progress views
        final TextView countDownView = (TextView) findViewById(R.id.wait_countdown_textview);
        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressbar);
        progressBar.setMax(GarageButtonListener.WAIT_TIME);

        // Assign listener to buttons
        GarageButtonListener garageButtonListener = new GarageButtonListener(this, openButton, closeButton) {
            @Override
            protected void updateCountdown(int seconds) {
                if (seconds == 0) {
                    countDownView.setText("");
                    progressBar.setVisibility(View.INVISIBLE);
                    setButtonsBackgroundColor();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    countDownView.setText(getString(R.string.wait_text, seconds));
                    progressBar.setProgress(GarageButtonListener.WAIT_TIME - seconds);
                }
            }
        };
        openButton.setOnClickListener(garageButtonListener);
        closeButton.setOnClickListener(garageButtonListener);
    }

    private void setButtonsBackgroundColor() {
        openButton.setBackgroundColor(Color.parseColor("#00dd00"));
        closeButton.setBackgroundColor(Color.parseColor("#dd0000"));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);


        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (GpsTrigger.isArmed()) {
            menu.getItem(0).setIcon(android.R.drawable.ic_menu_close_clear_cancel);
        } else {
            menu.getItem(0).setIcon(android.R.drawable.ic_menu_mylocation);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean handled;

        switch (item.getItemId()) {
            /*
            case R.id.menu_item_webcam:
                Intent intent = new Intent(this, WebcamActivity.class);
                startActivity(intent);
                handled = true;
                break;
            */
            case R.id.menu_item_arm_gps_trigger:
                if (GpsTrigger.isArmed()) {
                    GpsTrigger.unarm();
                    item.setIcon(android.R.drawable.ic_menu_mylocation);
                } else {
                    GpsTrigger.armEpendes(this);
                    item.setIcon(android.R.drawable.ic_menu_close_clear_cancel);

                    // Disable WiFi
                    WifiManager wifiManager = (WifiManager) MyApplication.getContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(false);
                }

                handled = true;
                break;
            default:
                handled = super.onOptionsItemSelected(item);
                break;
        }

        return handled;
    }
}
