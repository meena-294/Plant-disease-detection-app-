package com.example.plant_trial.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plant_trial.R;
import com.example.plant_trial.models.Treatment;

import java.util.List;

public class TreatmentAdapter extends RecyclerView.Adapter<TreatmentAdapter.TreatmentViewHolder> {

    private List<Treatment> treatmentList;

    public TreatmentAdapter(List<Treatment> treatmentList) {
        this.treatmentList = treatmentList;
    }

    @NonNull
    @Override
    public TreatmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_treatment, parent, false);
        return new TreatmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TreatmentViewHolder holder, int position) {
        Treatment treatment = treatmentList.get(position);
        holder.nameTextView.setText(treatment.getName());
        holder.descriptionTextView.setText(treatment.getDescription());
        holder.imageView.setImageResource(treatment.getImageResource());
    }

    @Override
    public int getItemCount() {
        return treatmentList.size();
    }

    public static class TreatmentViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public TextView nameTextView;
        public TextView descriptionTextView;

        public TreatmentViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.treatment_image);
            nameTextView = itemView.findViewById(R.id.treatment_name);
            descriptionTextView = itemView.findViewById(R.id.treatment_description);
        }
    }
}
