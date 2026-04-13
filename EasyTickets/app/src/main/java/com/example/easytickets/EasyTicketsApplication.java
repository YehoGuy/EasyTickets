package com.example.easytickets;

import android.app.Application;

import com.example.easytickets.di.AppContainer;

/**
 * Application entry point that creates the app's composition root once per process.
 * The class owns a single {@link AppContainer} instance and exposes it to the UI layer.
 */
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
