package com.kodelabs.boilerplate;

import android.app.Application;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

/**
 * Created by dmilicic on 12/10/15.
 */
public class AndroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Timber.plant(new DebugTree());
    }

}
