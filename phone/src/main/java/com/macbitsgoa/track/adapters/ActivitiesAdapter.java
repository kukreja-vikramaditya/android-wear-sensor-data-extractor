package com.macbitsgoa.track.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.macbitsgoa.track.R;
import com.macbitsgoa.track.viewholders.ActivitiesViewholder;

import java.util.List;

public class ActivitiesAdapter extends RecyclerView.Adapter<ActivitiesViewholder> {

    private List<String> activities;

    public ActivitiesAdapter(List<String> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivitiesViewholder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        final View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.vh_activities, viewGroup, false);
        return new ActivitiesViewholder(v, viewGroup.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull ActivitiesViewholder holder, int position) {
        holder.setText(activities.get(position));
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }
}
