package com.macbitsgoa.track.services;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.Tasks;
import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.macbitsgoa.track.utils.HC;
import com.macbitsgoa.track.utils.Keys;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class WLS extends WearableListenerService {

    private static final String TAG = WLS.class.getSimpleName();

    @Override
    public void onDataChanged(DataEventBuffer dataEventBuffer) {
        super.onDataChanged(dataEventBuffer);
        //check if permissions to write are granted
        //NOTE: This does not initiate a request. Open main app to do that.
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Permissions denied! Open app to allow access!",
                    Toast.LENGTH_SHORT).show();
            return;
        }
        for (DataEvent event : dataEventBuffer) {
            if (event.getType() == DataEvent.TYPE_DELETED) {
                broadcast("DataItem deleted: " + event.getDataItem().getUri());
            } else if (event.getType() == DataEvent.TYPE_CHANGED) {
                Log.e(TAG, "Getting data event - type changed");
                Log.e(TAG, event.getDataItem().toString());
                DataMap dataMap = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                if (dataMap.containsKey(Keys.Csv.CSV)) {
                    resolveCsv(dataMap);
                    Log.e(TAG, "Receiving csv data from wear");
                } else if (dataMap.containsKey(Keys.Activity.ACTIVITIES)) {
                    resolveActivities(dataMap);
                    Log.e(TAG, "Receiving activities from wear");
                } else {
                    Log.e(TAG, "What path? " + event.getDataItem().toString());
                }

            } else {
                broadcast("Unknown data event");
            }
        }
    }

    private void resolveActivities(@NonNull final DataMap dataMap) {
        final SharedPreferences sp = HC.getSharedPreferences(this);
        final SharedPreferences.Editor editor = sp.edit();

        final Set<String> activities = new HashSet<>(dataMap.getStringArrayList(Keys.Activity.ACTIVITIES));
        editor.putStringSet(Keys.Activity.ACTIVITIES, activities);
        editor.commit();
    }

    private void resolveCsv(@NonNull final DataMap dataMap) {
        byte[] csvData = getCsvFromAsset(dataMap.getAsset(Keys.Csv.CSV));
        if (csvData == null) {
            broadcast("CSV data was null or parsing error occurred. Try again!");
            return;
        }
        broadcast("Received csv data.\nSaving it to local storage");
        String fileName = dataMap.getString(Keys.Csv.FILE_NAME);
        try {
            final File directory =
                    new File(Environment.getExternalStorageDirectory()
                            + "/track/");
            if (!directory.exists()) {
                if (!directory.mkdirs()) {
                    throw new FileNotFoundException("Could not create requested directory");
                }
            }
            final File outputfile = new File(directory, fileName);
            if (!outputfile.exists()) {
                if (!outputfile.createNewFile()) {
                    throw new FileNotFoundException("Could not create new file");
                }
            }
            final FileOutputStream fos = new FileOutputStream(outputfile);
            fos.write(csvData);
            fos.flush();
            broadcast("Saved successfully to " + outputfile.getAbsolutePath() + " !");
        } catch (IOException e) {
            e.printStackTrace();
            broadcast(e.getMessage());
        }
    }//TODO add notification

    @Nullable
    public byte[] getCsvFromAsset(@Nullable final Asset asset) {
        if (asset == null) return null;
        byte[] csvData;
        try {
            final InputStream assetInputStream = Tasks
                    .await(Wearable.getDataClient(this).getFdForAsset(asset))
                    .getInputStream();
            if (assetInputStream == null) {
                return null;
            }
            csvData = IOUtils.toByteArray(assetInputStream);
            assetInputStream.close();
        } catch (ExecutionException | InterruptedException | IOException e) {
            e.printStackTrace();
            csvData = null;
        }
        return csvData;
    }

    void broadcast(@NonNull final String message) {
        Log.e(TAG, "Status: " + message);
    }
}
