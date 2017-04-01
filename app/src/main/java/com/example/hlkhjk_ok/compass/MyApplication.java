package com.example.hlkhjk_ok.compass;

import android.app.Application;
import android.content.Context;

/**
 * Created by hlkhjk_ok on 17/3/31.
 */

public class MyApplication extends Application {
    private static Context mContext;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = getApplicationContext();
    }

    public static Context getContext() {
        return mContext;
    }
}
