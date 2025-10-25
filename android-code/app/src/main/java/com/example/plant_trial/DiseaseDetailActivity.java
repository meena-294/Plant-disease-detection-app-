package com.example.plant_trial;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class DiseaseDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_disease_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar_disease_detail);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ImageView diseaseImage = findViewById(R.id.disease_detail_image);
        TextView diseaseDescription = findViewById(R.id.disease_detail_description);
        TextView diseaseSymptoms = findViewById(R.id.disease_detail_symptoms);
        TextView diseasePrevention = findViewById(R.id.disease_prevention_description);
        TextView diseaseFriendlyTreatment = findViewById(R.id.disease_friendly_description);
        TextView diseaseChemicalTreatment = findViewById(R.id.disease_chemical_description);


        String diseaseName = getIntent().getStringExtra("DISEASE_NAME");
        String description = getIntent().getStringExtra("DISEASE_DESCRIPTION");
        String symptoms = getIntent().getStringExtra("DISEASE_SYMPTOMS");
        String prevention = getIntent().getStringExtra("DISEASE_PREVENTION");
        String friendlyTreatment = getIntent().getStringExtra("DISEASE_FRIENDLY");
        String chemicalTreatment = getIntent().getStringExtra("DISEASE_CHEMICAL");


        int imageResId = getIntent().getIntExtra("DISEASE_IMAGE_RES", R.drawable.ic_image_placeholder);

        getSupportActionBar().setTitle(diseaseName);
        diseaseImage.setImageResource(imageResId);
        diseaseDescription.setText(description);
        diseaseSymptoms.setText(symptoms);
        diseasePrevention.setText(prevention);
        diseaseFriendlyTreatment.setText(friendlyTreatment);
        diseaseChemicalTreatment.setText(chemicalTreatment);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

}