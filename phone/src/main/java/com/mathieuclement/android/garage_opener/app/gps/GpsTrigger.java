package com.mathieuclement.android.garage_opener.app.gps;

import android.app.Activity;
import android.content.Context;
import android.location.LocationManager;
import android.widget.Toast;
import com.mathieuclement.android.garage_opener.app.MyApplication;
import com.mathieuclement.android.garage_opener.app.R;

/**
 * @author Mathieu Clément
 * @since 11.03.2014
 */
public class GpsTrigger {

    private static LocationManager LOCATION_MANAGER_INSTANCE;

    private static boolean isArmed = false;
    private static GarageLocationListener listener;
    private static OpenGarageGpsCallback gpsCallback;

    /**
     * Arm the trigger, so that it fires when approaching the specified location.
     * Users should only arm one location at a time.
     *
     * @param latitude   Latitude (46.* in Switzerland)
     * @param longitude  Longitude (7.* in Switzerland)
     * @param proximity  Proximity radius in meters. > 5 meters
     * @param updateTime Minimum update time (in ms)
     * @param updateDist Minimum update distance (in meters)
     * @param activity   Activity for OpenGarageGpsCallback.
     */
    public static void arm(double latitude, double longitude, int proximity,
                           long updateTime, long updateDist, Activity activity) {
        isArmed = true;

        if (listener == null) {
            gpsCallback = new OpenGarageGpsCallback(activity);
            listener = new GarageLocationListener(
                    latitude, longitude, proximity, gpsCallback);
        }

        getLocationManager().requestLocationUpdates(LocationManager.GPS_PROVIDER,
                updateTime, updateDist, listener);

        Toast.makeText(MyApplication.getContext(), R.string.gps_trigger_armed, Toast.LENGTH_SHORT).show();
    }

    public static void armEpendes(Activity activity) {
        arm(46.712345, 7.123456, 300, 5000, 0, activity);
    }

    /**
     * Stop watching (all armed locations)
     */
    public static void unarm() {
        if (isArmed) {
            getLocationManager().removeUpdates(listener);
            gpsCallback.onGpsUnarmed();
        }
        isArmed = false;
        listener = null;
        gpsCallback = null;
        Toast.makeText(MyApplication.getContext(), R.string.gps_trigger_disabled, Toast.LENGTH_SHORT).show();
    }

    /**
     * Returns true if GPS trigger is armed
     * @return true if GPS trigger is armed
     */
    public static boolean isArmed() {
        return isArmed;
    }

    private static LocationManager getLocationManager() {
        if (LOCATION_MANAGER_INSTANCE == null) {
            LOCATION_MANAGER_INSTANCE = (LocationManager) MyApplication.getContext().getSystemService(Context.LOCATION_SERVICE);
        }
        return LOCATION_MANAGER_INSTANCE;
    }

}
