package com.kodelabs.boilerplate;

import android.app.Application;

import timber.log.Timber;
import timber.log.Timber.DebugTree;

public class AndroidApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // initiate Timber
        Timber.plant(new DebugTree());
    }
}
