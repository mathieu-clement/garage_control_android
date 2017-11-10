package com.mathieuclement.android.garage_opener.app;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import com.mathieuclement.android.garage_opener.app.webrequest.GarageControl;

/**
 * @author Mathieu Clément
 * @since 25.07.2013
 */
abstract class GarageButtonListener implements View.OnClickListener, GarageControl.Listener {

    private final GarageControl garageControl;

    public final static int WAIT_TIME = 20; // seconds to wait after command has been received
    private final View[] views;
    private final Activity activity;

    public GarageButtonListener(Activity activity, View... viewsToDisableDuringWaitTime) {
        this.activity = activity;
        this.garageControl = new GarageControl();
        this.views = viewsToDisableDuringWaitTime;

        this.garageControl.addListener(this);
    }

    @Override
    public void onClick(final View v) {
        int title;
        int message;

        switch (v.getId()) {
            case R.id.open_button:
                title = R.string.confirm_dialog_open_title;
                message = R.string.confirm_dialog_open_message;
                break;

            case R.id.close_button:
                title = R.string.confirm_dialog_close_title;
                message = R.string.confirm_dialog_close_message;
                break;

            default:
                throw new Error("Unknown source of event: " + v);
        }

        ConfirmDialogFragment dialogFrag = new ConfirmDialogFragment(title, message) {
            @Override
            protected void onPositiveButtonClicked() {
                onClickConfirmed(v);
            }

            @Override
            protected void onNegativeButtonClicked() {
            }
        };
        dialogFrag.show(activity.getFragmentManager(), "confirm_dialog");


    }

    public void onClickConfirmed(final View v) {
        v.setClickable(false);
        Drawable defaultBackground = v.getResources().getDrawable(android.R.drawable.btn_default);

        for (View view : views) {
            view.setEnabled(false);
            if (!view.equals(v)) {
                view.setBackground(defaultBackground);
            }
        }

        switch (v.getId()) {
            case R.id.open_button:
                try {
                    garageControl.open();
                    onOpenCommandSent();
                } catch (Exception e) {
                    // nothing to do.
                }
                break;

            case R.id.close_button:
                try {
                    garageControl.close();
                    onCloseCommandSent();
                } catch (Exception e) {
                    // nothing to do.
                }
                break;
        }
    }

    private static boolean isNetworkConnected(Context context) {
        NetworkInfo ni = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE))
                .getActiveNetworkInfo();
        if (ni == null) return false;

        int netType = ni.getType();
        int netSubtype = ni.getSubtype();

        // Check if connected to data network
        return (netType == ConnectivityManager.TYPE_MOBILE
                && (netSubtype == TelephonyManager.NETWORK_TYPE_UMTS ||
                netSubtype == TelephonyManager.NETWORK_TYPE_HSDPA ||
                netSubtype == TelephonyManager.NETWORK_TYPE_HSPA ||
                netSubtype == TelephonyManager.NETWORK_TYPE_HSPAP ||
                netSubtype == TelephonyManager.NETWORK_TYPE_HSUPA)) && ni.isConnected();
    }

    public static void waitNetworkConnected(Context context) {
        int i = 0;
        while (!isNetworkConnected(context) && i++ < 50) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    protected abstract void updateCountdown(int seconds);

    private class ProgressTask extends AsyncTask<View[], Integer, Void> {

        public View[] views;

        @Override
        protected Void doInBackground(View[]... params) {
            this.views = params[0];

            try {
                for (int i = WAIT_TIME; i > 0; i--) {
                    publishProgress(i);
                    Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Log.e(GarageButtonListener.class.getName(), "Thread.sleep()", e);
            } finally {
                publishProgress(0);
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            updateCountdown(values[0]);
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            for (View view : views) {
                view.setEnabled(true);
                view.setClickable(true);
            }
        }
    }

    @Override
    public void onOpenCommandSent() {
        GarageControl.alertStatus(activity.getString(R.string.processing_request));
    }

    @Override
    public void onCloseCommandSent() {
        GarageControl.alertStatus(activity.getString(R.string.processing_request));
    }

    @Override
    public void onCommandSuccess(String cmd) {
        new ProgressTask().execute(this.views);
    }

    @Override
    public void onCommandFailure(String cmd) {
        // Reset view, allow the user to try again.
        updateCountdown(0);
        for (View view : views) {
            view.setEnabled(true);
            view.setClickable(true);
        }
    }
}
