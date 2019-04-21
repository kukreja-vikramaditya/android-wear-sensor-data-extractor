package com.macbitsgoa.track.utils;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.DataItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class HC {

    public static final String ID = "com.macbitsgoa.track";

    public static final String DEFAULT_SP = "wear";

    public static final String WEAR_PATH = "/wear";

    public static final String PHONE_PATH = "/phone";

    public static final String ACTION_UPDATE_NEW_ACTIVITY_ADDED = ID + ".actions.new_activity";

    public static final String ACTION_DELETE_ACTIVITY = ID + ".actions.delete_activity";

    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.BODY_SENSORS,
    };

    public static SharedPreferences getSharedPreferences(@NonNull final Context context) {
        return context.getSharedPreferences(DEFAULT_SP, Context.MODE_PRIVATE);
    }

    public static boolean deleteDirectory(@NonNull final String directoryPath) {
        final File directory = new File(directoryPath);
        if (!directory.exists()) {
            return true;
        }
        final String[] children = directory.list();
        boolean delete = true;
        for (String childFile : children) {
            delete &= new File(directory, childFile).delete();
        }
        return delete;
    }

    public static Task<DataItem> shareActivitiesList(@NonNull final Context context) {
        final SharedPreferences sp = getSharedPreferences(context);
        final Set<String> activities = sp.getStringSet(Keys.Activity.ACTIVITIES, new HashSet<String>());

        final PutDataMapRequest pdmr = PutDataMapRequest.create(PHONE_PATH);
        assert activities != null;
        pdmr.getDataMap().putStringArrayList(Keys.Activity.ACTIVITIES, new ArrayList<>(activities));
        return sendDataMap(context, pdmr);
    }

    public static Task<DataItem> sendDataMap(@NonNull final Context context, @NonNull final PutDataMapRequest pdmr) {
        final PutDataRequest pdr = pdmr.asPutDataRequest();
        return Wearable.getDataClient(context).putDataItem(pdr);
    }
}
