package com.example.easytickets;

import android.app.Application;

import com.example.easytickets.di.AppContainer;

public class EasyTicketsApplication extends Application {

    private AppContainer appContainer;

    @Override
    public void onCreate() {
        super.onCreate();
        appContainer = new AppContainer(this);
    }

    public AppContainer getAppContainer() {
        return appContainer;
    }
}
