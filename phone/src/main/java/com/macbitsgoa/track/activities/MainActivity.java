package com.macbitsgoa.track.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.TextView;

import com.macbitsgoa.track.R;

import java.io.File;

public class MainActivity extends BaseActivity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView status;
    private FloatingActionButton fab;

    private File lastCsv = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = findViewById(R.id.tv);
        fab = findViewById(R.id.fab);

        fab.hide();
        status.setText("Listener activated ✔️. Press START on wear to be begin recording. Keep this screen open to receive file");

        checkPermissions();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (lastCsv == null) fab.hide();
        else fab.show();
    }

    @Override
    public void onClick(final View v) {
        if (v.getId() == R.id.fab) {
            if (lastCsv == null) fab.hide();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(lastCsv), "text/plain");
            startActivity(intent);
        } else if (v.getId() == R.id.btn_main_manage) {
            startActivity(new Intent(this, ManageActivity.class));
        }
    }
}
