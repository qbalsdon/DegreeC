package com.berry.degreec;

import android.app.Application;


public class DegreeCApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceUtil.init(this);
    }
}
