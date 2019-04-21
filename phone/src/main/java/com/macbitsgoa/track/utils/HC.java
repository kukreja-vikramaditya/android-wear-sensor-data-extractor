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

public class HC {
    public static final String DEFAULT_SP = "phone";

    public static final String WEAR_PATH = "/wear";

    public static final String PHONE_PATH = "/phone";

    public static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
    };

    public static SharedPreferences getSharedPreferences(@NonNull final Context context) {
        return context.getSharedPreferences(DEFAULT_SP, Context.MODE_PRIVATE);
    }

    public static void deleteFromSp(@NonNull final Context context,
                                    @NonNull final String activity) {
        final PutDataMapRequest pdmr = PutDataMapRequest.create(WEAR_PATH);
        pdmr.getDataMap().putString(Keys.Activity.DELETE, activity);

        final PutDataRequest pdr = pdmr.asPutDataRequest();
        Wearable.getDataClient(context).putDataItem(pdr);
    }
}
