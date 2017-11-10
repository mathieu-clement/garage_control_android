package com.mathieuclement.android.garage_opener.app.gps;

/**
* @author Mathieu Clément
* @since 20.03.2014
*/
/* package */ interface GpsCallback {
    void onGpsFixAcquired();

    void onGpsFixLost();

    void onGpsError();

    void onRadiusEntered();

    void onRadiusExited();

    void onGpsUnarmed();
}
