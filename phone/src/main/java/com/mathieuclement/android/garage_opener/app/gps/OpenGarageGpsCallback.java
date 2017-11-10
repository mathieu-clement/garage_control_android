package com.mathieuclement.android.garage_opener.app.gps;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import com.mathieuclement.android.garage_opener.app.MyApplication;
import com.mathieuclement.android.garage_opener.app.R;
import com.mathieuclement.android.garage_opener.app.webrequest.GarageControl;

/**
 * @author Mathieu Clément
 * @since 27.03.2014
 */
public class OpenGarageGpsCallback implements GpsCallback {
    private static final GarageControl garageControl = new GarageControl();
    private final NotificationManager notificationManager;
    private final Context context;
    private final Activity activity;
    private static final int GPS_NOTIFICATION_ID = 1;
    private static final int GARAGE_OPENING_NOTIFICATION_ID = 2;

    public OpenGarageGpsCallback(Activity activity) {
        this.activity = activity;
        context = MyApplication.getContext();
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

    }

    private void showNotification(CharSequence detailText, boolean ongoing) {
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(detailText);
        builder.setOngoing(ongoing);
        Notification notification = builder.build();

        notificationManager.notify(GPS_NOTIFICATION_ID, notification);
    }

    private void hideAllNotifications() {
        notificationManager.cancelAll();
    }

    @Override
    public void onGpsFixAcquired() {
        showNotification(context.getText(R.string.gps_trigger_fix_obtained), true);
    }

    @Override
    public void onGpsFixLost() {
        showNotification(context.getText(R.string.gps_trigger_fix_lost), true);
    }

    @Override
    public void onGpsError() {
        showNotification(context.getText(R.string.gps_trigger_error), false);
    }

    private final Uri soundUri = Uri.parse("android.resource://com.mathieuclement.android.garage_opener.app/raw/chimes");

    @Override
    public void onRadiusEntered() {
        garageControl.open();

        GpsTrigger.unarm();

        // Must be done after unarm, because GpsTrigger onUarmed() will cancel all notifications
        Notification.Builder builder = new Notification.Builder(context);
        builder.setSmallIcon(R.drawable.ic_launcher);
        builder.setContentTitle(context.getString(R.string.app_name));
        builder.setContentText(context.getString(R.string.gps_trigger_opening));
        builder.setSound(soundUri, AudioManager.STREAM_ALARM); // plays even in silent mode
        Notification openingNotif = builder.build();
        notificationManager.notify(GARAGE_OPENING_NOTIFICATION_ID, openingNotif);

        // Enable WiFi
        getWifiManager().setWifiEnabled(true);
    }

    private WifiManager getWifiManager() {
        return (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public void onRadiusExited() {

    }

    @Override
    public void onGpsUnarmed() {
        //hideAllNotifications();
        notificationManager.cancel(GPS_NOTIFICATION_ID);

        if(this.activity != null && !this.activity.isDestroyed() && !this.activity.isFinishing()) {
            this.activity.invalidateOptionsMenu();
        }
    }
}
