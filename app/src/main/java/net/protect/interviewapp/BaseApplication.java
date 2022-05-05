package net.protect.interviewapp;

import android.app.Application;

import net.protect.interviewapp.api.Singletons;

public class BaseApplication extends Application {
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // Hide the API key away as you would in production
        Singletons.INSTANCE.setAPI_KEY(getApiKey());
    }
    
    static {
        System.loadLibrary("interviewapp");
    }
    
    public native String getApiKey();
}