package com.macbitsgoa.track;

import android.app.Application;

import com.macbitsgoa.track.utils.HC;

/**
 * Application class that sends the shared preferences list to the phone
 * at path <b>/activities</b> as an array list of strings.
 */
public class Tracker extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //send shared pref list of custom activities
        HC.shareActivitiesList(this);
    }
}
