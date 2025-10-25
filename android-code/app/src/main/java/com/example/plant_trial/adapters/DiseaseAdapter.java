package com.example.plant_trial.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.plant_trial.DiseaseDetailActivity;
import com.example.plant_trial.R;
import com.example.plant_trial.models.Disease;

import java.util.List;

public class DiseaseAdapter extends RecyclerView.Adapter<DiseaseAdapter.DiseaseViewHolder> {


    private final List<Disease> diseaseList;
    private Context context;

    public DiseaseAdapter(Context context, List<Disease> diseaseList) {

        this.context = context;

        this.diseaseList = diseaseList;
    }

    @NonNull
    @Override
    public DiseaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_disease, parent, false);
        return new DiseaseViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiseaseViewHolder holder, int position) {
        Disease disease = diseaseList.get(position);
        holder.diseaseName.setText(disease.getName());
        holder.diseaseDescription.setText(disease.getDescription());
        holder.diseaseImage.setImageResource(disease.getImageResource());

        holder.viewDetailsButton.setOnClickListener(v -> {
            Context context = v.getContext();
            Intent intent = new Intent(context, DiseaseDetailActivity.class);
            intent.putExtra("DISEASE_NAME", disease.getName());
            intent.putExtra("DISEASE_DESCRIPTION", disease.getDescription());
            intent.putExtra("DISEASE_SYMPTOMS", disease.getSymptoms());
            intent.putExtra("DISEASE_PREVENTION", disease.getPrevention());
            intent.putExtra("DISEASE_FRIENDLY", disease.getFriendlyTreatments());
            intent.putExtra("DISEASE_CHEMICAL", disease.getChemicalTreatments());


            intent.putExtra("DISEASE_IMAGE_RES", disease.getImageResource());
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return diseaseList.size();
    }

    public static class DiseaseViewHolder extends RecyclerView.ViewHolder {
        public ImageView diseaseImage;
        public TextView diseaseName;
        public TextView diseaseDescription;
        public Button viewDetailsButton;

        public DiseaseViewHolder(@NonNull View itemView) {
            super(itemView);
            diseaseImage = itemView.findViewById(R.id.disease_image);
            diseaseName = itemView.findViewById(R.id.disease_name);
            diseaseDescription = itemView.findViewById(R.id.disease_description);
            viewDetailsButton = itemView.findViewById(R.id.view_details_button);
        }
    }

}