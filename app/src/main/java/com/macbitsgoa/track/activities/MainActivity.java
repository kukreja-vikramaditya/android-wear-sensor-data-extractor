package com.macbitsgoa.track.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.wearable.Asset;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.macbitsgoa.track.R;
import com.macbitsgoa.track.utils.HC;
import com.macbitsgoa.track.utils.Keys;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Set;

public class MainActivity extends BaseActivity implements SensorEventListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int TIME_PERIOD_US = SensorManager.SENSOR_DELAY_FASTEST;//1_000_000; 1sec

    private Button startBtn;
    private RadioGroup radioGroup;

    private SensorManager sensorManager;
    private Sensor acclSensor;
    private Sensor gyroSensor;
    private Sensor heartRateSensor;
    private Sensor heartBeatSensor;
    private Sensor lAcclSensor;
    private Sensor rotationSensor;
    private Sensor _6dofSensor;
    private Sensor magnetSensor;
    private Sensor ambientSensor;
    private Sensor motionDetectSensor;
    private Sensor pressureSensor;
    private Sensor proximitySensor;
    private Sensor stationSensor;
    private Sensor stepSensor;
    private Sensor stepCountSensor;
    private Sensor sigMotionSensor;

    private boolean recording = false;

    private FileWriter writer;
    private File csvFile;
    private String fileName;

    private BroadcastReceiver wlsReceiver;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();
        HC.shareActivitiesList(this);

        startBtn = findViewById(R.id.btn_main_start);
        radioGroup = findViewById(R.id.rg);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        acclSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        heartBeatSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_BEAT);
        lAcclSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        _6dofSensor = sensorManager.getDefaultSensor(Sensor.TYPE_POSE_6DOF);
        magnetSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        ambientSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        motionDetectSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MOTION_DETECT);
        pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        stationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STATIONARY_DETECT);
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sigMotionSensor = sensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);

        checkPermissions();
        setupRadioButtons();
        setupWlsReceiver();
    }

    private void setupWlsReceiver() {
        wlsReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() == null) return;
                if (intent.getAction().equals(HC.ACTION_UPDATE_NEW_ACTIVITY_ADDED)) {
                    Toast.makeText(context, "New activity added!", Toast.LENGTH_SHORT).show();
                } else if (intent.getAction().equals(HC.ACTION_DELETE_ACTIVITY)) {
                    Toast.makeText(context, "Deleted 1 activity", Toast.LENGTH_SHORT).show();
                }
                setupRadioButtons();
            }
        };
    }

    private void setupRadioButtons() {
        //Get custom radio buttons from shared preference list
        final SharedPreferences sp = getDefaultSharedPreferences();
        final Set<String> activities = sp.getStringSet(Keys.Activity.ACTIVITIES, new HashSet<String>());
        assert activities != null;
        Log.e(TAG, "Found " + activities.size() + " activities");
        radioGroup.removeAllViews();
        for (String activity : activities) {
            final RadioButton rb = new RadioButton(this);
            rb.setText(activity);
            radioGroup.addView(rb);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        HC.shareActivitiesList(this);
        IntentFilter intf = new IntentFilter();
        intf.addAction(HC.ACTION_UPDATE_NEW_ACTIVITY_ADDED);
        intf.addAction(HC.ACTION_DELETE_ACTIVITY);
        registerReceiver(wlsReceiver, intf);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wlsReceiver);
        if (recording) {
            startBtn.setText("START");
            recording = false;
            endSession();
            sensorManager.unregisterListener(this);
        }
    }

    //TODO select option clear start
    @Override
    public void onClick(final View v) {
        final int id = v.getId();
        if (id == R.id.btn_main_start) {
            if (!checkPermissions()) {
                return;
            }
            if (recording) {
                recording = false;
                sensorManager.unregisterListener(this);
                startBtn.setText("START");
                endSession();
                setupRadioButtons(); //TODO issue when deleting async but start btn is pressed
            } else {
                if (!startSession()) {
                    Toast.makeText(this, "Permit file storage!", Toast.LENGTH_SHORT).show();
                    return;
                }
                recording = true;
                sensorManager.registerListener(this, heartBeatSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, heartRateSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, acclSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, gyroSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, lAcclSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, rotationSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, _6dofSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, magnetSensor, TIME_PERIOD_US);

                sensorManager.registerListener(this, ambientSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, motionDetectSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, pressureSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, proximitySensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, stationSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, stepSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, stepCountSensor, TIME_PERIOD_US);
                sensorManager.registerListener(this, sigMotionSensor, TIME_PERIOD_US);
                startBtn.setText("STOP");
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (!recording) return;
        StringBuilder sb = new StringBuilder();
        sb.append(Calendar.getInstance().getTime().getTime());
        sb.append(',');
        sb.append(event.values.length);
        sb.append(',');
        for (int i = 0; i < event.values.length; i++) {
            sb.append(event.values[i]);
            sb.append(',');
        }
        sb.append(event.sensor.getName());
        sb.append('\n');
        try {
            writer.append(sb.toString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Writing value error. Writer issue? or csvFile missing?");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public boolean startSession() {
        fileName = getFileName();
        if (!checkPermissions()) return false;
        try {
            String filepath = Environment.getExternalStorageDirectory() + "/track/";
            File directory = new File(filepath);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            csvFile = new File(directory, fileName);
            writer = new FileWriter(csvFile);
            writer.flush();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private String getFileName() {
        final Calendar date = Calendar.getInstance();
        final StringBuilder sb = new StringBuilder();

        final int id = ((RadioGroup) findViewById(R.id.rg)).getCheckedRadioButtonId();
        String mode = "not_specified";
        if (id != -1)
            mode = ((RadioButton) findViewById(id)).getText().toString();

        final int year = date.get(Calendar.YEAR);
        final int month = 1 + date.get(Calendar.MONTH);
        final int day = date.get(Calendar.DAY_OF_MONTH);
        final int hour = date.get(Calendar.HOUR_OF_DAY);
        final int minute = date.get(Calendar.MINUTE);
        final int second = date.get(Calendar.SECOND);

        sb.append(year);
        sb.append('_');
        if (month < 10) sb.append(0);
        sb.append(month);
        sb.append('_');
        if (day < 10) sb.append(0);
        sb.append(day);
        sb.append('_');
        if (hour < 10) sb.append(0);
        sb.append(hour);
        sb.append('_');
        if (minute < 10) sb.append(0);
        sb.append(minute);
        sb.append('_');
        if (second < 10) sb.append(0);
        sb.append(second);
        sb.append('_');
        sb.append(mode);
        sb.append(".csv");

        return sb.toString();
    }

    public void endSession() {
        try {
            writer.flush();
            writer.close();

            final byte bytes[] = FileUtils.readFileToByteArray(csvFile);

            final PutDataMapRequest pdmr = PutDataMapRequest.create(HC.PHONE_PATH);
            pdmr.getDataMap().putAsset(Keys.Csv.CSV, Asset.createFromBytes(bytes));
            pdmr.getDataMap().putString(Keys.Csv.FILE_NAME, fileName);
            HC.sendDataMap(this, pdmr);
            Log.e(TAG, "Session over. Sending data");
            Toast.makeText(this, "Sending data to phone!", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG, "Session ending error. Writer issue? or csvFile missing?");
        }
    }
}
