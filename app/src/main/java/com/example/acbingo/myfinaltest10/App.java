package com.example.acbingo.myfinaltest10;

import android.app.Application;
import android.content.Context;

import timber.log.Timber;

/**
 * Created by Bingo on 24/5/16.
 */
public class App extends Application{
    private static App app;

    @Override
    public void onCreate() {
        super.onCreate();
        //Fabric.with(this, new Crashlytics());
        app = this;
        Timber.plant(new Timber.DebugTree());


    }

    public static Context getAppContext() {
        return app;
    }
}
