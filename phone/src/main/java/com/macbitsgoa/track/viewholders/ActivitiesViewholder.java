package com.macbitsgoa.track.viewholders;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.macbitsgoa.track.R;
import com.macbitsgoa.track.utils.HC;

public class ActivitiesViewholder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private TextView activityTv;
    private ImageButton deleteBtn;
    private Context context;

    public ActivitiesViewholder(@NonNull View itemView, @NonNull final Context context) {
        super(itemView);
        this.context = context;
        activityTv = itemView.findViewById(R.id.tv_vh_activities);
        deleteBtn = itemView.findViewById(R.id.imgbtn_vh_activities);
        deleteBtn.setOnClickListener(this);
    }

    public void setText(@NonNull final String text) {
        activityTv.setText(text);
    }

    @Override
    public void onClick(View v) {
        final int id = v.getId();
        if (id == R.id.imgbtn_vh_activities) {
            HC.deleteFromSp(context, activityTv.getText().toString());
        }
    }
}
