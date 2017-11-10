package com.mathieuclement.android.garage_opener.app;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * @author Mathieu Clément
 * @since 25.07.2013
 */
public class MyApplication extends Application {
    private static MyApplication instance;

    public MyApplication() {
        instance = this;
    }

    public static Context getContext() {
        return instance;
    }

    public String getVersion() {
        try {
            return getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(getClass().getName(), e.getMessage(), e);
        }
        return null;
    }
}
