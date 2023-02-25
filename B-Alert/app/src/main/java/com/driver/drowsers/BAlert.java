package com.driver.drowsers;

import android.app.Application;
import android.content.Context;

public class BAlert extends Application {
    private static BAlert instance;

    public static Context getContext() {
        return instance.getApplicationContext();
    }


    @Override
    public void onCreate() {
        instance = this;
        super.onCreate();
    }
}
