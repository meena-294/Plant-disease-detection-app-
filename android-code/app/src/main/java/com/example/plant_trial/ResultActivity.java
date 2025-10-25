package com.example.plant_trial;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_result);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Toolbar toolbar = findViewById(R.id.toolbar_result);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("Analysis Results");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        ImageView analyzedImage = findViewById(R.id.analyzed_image_view);
        TextView diseaseName = findViewById(R.id.disease_name);

        // Get the data from the intent
        String imageUriString = getIntent().getStringExtra("imageUri");
        String disease = getIntent().getStringExtra("disease");

        // Display the data
        if (imageUriString != null) {
            analyzedImage.setImageURI(Uri.parse(imageUriString));
        }
        diseaseName.setText("Disease: " + disease);

        Button treatmentButton = findViewById(R.id.treatment_button);
        Button suggestionsButton = findViewById(R.id.suggestions_button);
        String disease_name = getIntent().getStringExtra("DISEASE_NAME"); // Assuming you get this from the intent

// This button launches the existing TreatmentsActivity
        treatmentButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, TreatmentsActivity.class);
            intent.putExtra("DISEASE_NAME", disease_name);
            startActivity(intent);
        });

// This button launches our new SuggestionsActivity
        suggestionsButton.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, SuggestionsActivity.class);
            intent.putExtra("DISEASE_NAME", disease_name);
            startActivity(intent);
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}