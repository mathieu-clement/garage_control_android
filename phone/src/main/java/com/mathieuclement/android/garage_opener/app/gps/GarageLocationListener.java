package com.mathieuclement.android.garage_opener.app.gps;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

/**
 * @author Mathieu Clément
 * @since 20.03.2014
 */
/* package */ class GarageLocationListener implements LocationListener {

    private final double triggerLatitude;
    private final double triggerLongitude;
    private final int triggerRadius;
    private final GpsCallback gpsCallback;
    private boolean wasInside = false;
    private boolean wasOutsideOnce = false;

    GarageLocationListener(double triggerLatitude, double triggerLongitude, int triggerRadius,
                           GpsCallback gpsCallback) {
        this.triggerLatitude = triggerLatitude;
        this.triggerLongitude = triggerLongitude;
        this.triggerRadius = triggerRadius;
        this.gpsCallback = gpsCallback;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Check if it's necessary to temporize calls to the callback

        if (isInsideRadius(location)) {
            if(!wasInside && wasOutsideOnce) {
                Log.d(GarageLocationListener.class.getName(), "Radius entered");
                gpsCallback.onRadiusEntered();
            } else {
                // Already inside, do nothing
                Log.d(GarageLocationListener.class.getName(), "Location change but already inside zone");
            }
            wasInside = true;
        } else {
            if(wasInside) {
                Log.d(GarageLocationListener.class.getName(), "Radius exited");
                gpsCallback.onRadiusExited();
                wasInside = false;
            }
            wasOutsideOnce = true;
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        if (status == LocationProvider.AVAILABLE)
            gpsCallback.onGpsFixAcquired();
        else if (status == LocationProvider.TEMPORARILY_UNAVAILABLE)
            gpsCallback.onGpsFixLost();
        else if (status == LocationProvider.OUT_OF_SERVICE)
            gpsCallback.onGpsError();
        else
            throw new RuntimeException("Unknown status " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    /**
     * Returns true if the location is inside the defined radius.
     * @param location Location to be tested, probably the current location
     * @return true if the location is inside the defined radius.
     */
    public boolean isInsideRadius(Location location) {
        return distance(location.getLatitude(), location.getLongitude(),
                triggerLatitude, triggerLongitude) < triggerRadius;
    }

    /**
     * Distance between two points (in meters) accounting that the Earth is not flat.
     *
     * @param lat1 Latitude of 1st point
     * @param lng1 Longitude of 1st point
     * @param lat2 Latitude of 2nd point
     * @param lng2 Longitude of 2nd point
     * @return distance between two points (in meters)
     */
    private static double distance(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng / 2) * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        double meterConversion = 1609.;

        return dist * meterConversion;
    }
}
