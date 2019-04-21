package com.macbitsgoa.track.services;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;
import com.macbitsgoa.track.utils.HC;
import com.macbitsgoa.track.utils.Keys;

import java.util.HashSet;
import java.util.Set;

/**
 * WLS class to receive data from phone. Currently supports only "add" activity command
 * at path <b>/add</b>. The activity is added to shared preference list.
 */
public class WLS extends WearableListenerService {

    private static final String TAG = WLS.class.getSimpleName();

    //TODO add old csv deleter
    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        //check if permissions to write are granted
        //NOTE: This does not initiate a request. Open main app to do that.
        /*if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions denied! Open app to allow access!",
                    Toast.LENGTH_SHORT).show();
            return;
        }*/
        Log.e(TAG, "Receiving data from phone");
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_CHANGED) {
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if (dataMap.containsKey(Keys.Activity.NEW_ACTIVITY)) {
                    Log.e(TAG, "Adding new activity to shared preferences");
                    resolveAdd(dataMap);
                } else if (dataMap.containsKey(Keys.Activity.DELETE)) {
                    resolveDeleteFromSp(dataMap);
                } else {
                    Log.e(TAG, "Could not recognise datamap " + dataMap.toString());
                }
            } else {
                Log.e(TAG, "Unknown data event");
            }
        }
    }

    private void resolveDeleteFromSp(@NonNull final DataMap dataMap) {
        final SharedPreferences sp = HC.getSharedPreferences(this);
        final SharedPreferences.Editor editor = sp.edit();
        final String activity = dataMap.getString(Keys.Activity.DELETE);
        Set<String> activities = sp.getStringSet(Keys.Activity.ACTIVITIES, new HashSet<String>());
        activities.remove(activity);
        editor.putStringSet(Keys.Activity.ACTIVITIES, activities);
        editor.commit();
        //update phone list now
        HC.shareActivitiesList(this);
        broadcast(HC.ACTION_DELETE_ACTIVITY);
    }

    /**
     * New addd activity will come under path <b>/add</b> and the activity will be under
     * key <b>activity</b>.
     *
     * @param dataMap
     */
    private void resolveAdd(@NonNull final DataMap dataMap) {
        final String activity = dataMap.getString(Keys.Activity.NEW_ACTIVITY);
        final SharedPreferences sp = HC.getSharedPreferences(this);
        final SharedPreferences.Editor editor = sp.edit();
        final Set<String> activities = sp.getStringSet(Keys.Activity.ACTIVITIES, new HashSet<String>());
        assert activities != null;
        activities.add(activity);
        editor.putStringSet(Keys.Activity.ACTIVITIES, activities);
        editor.commit();
        HC.shareActivitiesList(this);
        broadcast(HC.ACTION_UPDATE_NEW_ACTIVITY_ADDED);
    }

    private void broadcast(@NonNull final String action) {
        final Intent intent = new Intent();
        intent.setAction(action);
        sendBroadcast(intent);
    }
}
