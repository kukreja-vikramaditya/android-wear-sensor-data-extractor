package com.macbitsgoa.track.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.macbitsgoa.track.R;
import com.macbitsgoa.track.adapters.ActivitiesAdapter;
import com.macbitsgoa.track.utils.HC;
import com.macbitsgoa.track.utils.Keys;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class ManageActivity extends BaseActivity implements View.OnClickListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = ManageActivity.class.getSimpleName();

    private EditText et;
    private ActivitiesAdapter adapter;
    private List<String> activities;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage);

        et = findViewById(R.id.et_manage);
        RecyclerView rv = findViewById(R.id.rv_manage);

        activities = new ArrayList<>();
        adapter = new ActivitiesAdapter(activities);
        rv.setAdapter(adapter);
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setHasFixedSize(true);

        HC.getSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
        onSharedPreferenceChanged(HC.getSharedPreferences(this), Keys.Activity.ACTIVITIES);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HC.getSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.btn_manage_add) {
            String activity = et.getText().toString().trim();
            if (activity.length() == 0) {
                return;
            }
            final PutDataMapRequest pdmr = PutDataMapRequest.create(HC.WEAR_PATH);
            pdmr.getDataMap().putString(Keys.Activity.NEW_ACTIVITY, activity);

            final PutDataRequest pdr = pdmr.asPutDataRequest();
            Wearable.getDataClient(this).putDataItem(pdr);
            Log.e(TAG, "Sending data to wear");
            et.getText().clear();
            hideSoftKeyboard(et);
        } else if(id == R.id.btn_manage_clear) {
            et.getText().clear();
        }
    }

    @Override
    public void onSharedPreferenceChanged(@NonNull final SharedPreferences sharedPreferences,
                                          @NonNull final String key) {
        if (key.equals(Keys.Activity.ACTIVITIES)) {
            //activities got updated. display updated content
            activities.clear();
            activities.addAll(sharedPreferences.getStringSet(key, new HashSet<String>()));
            adapter.notifyDataSetChanged();
        }
    }
}
