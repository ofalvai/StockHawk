package com.udacity.stockhawk;

import android.app.Application;

import com.facebook.stetho.Stetho;

import timber.log.Timber;

public class StockHawkApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }

        Stetho.initializeWithDefaults(this);
    }
}
