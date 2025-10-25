package com.example.plant_trial;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.plant_trial.models.DiseaseManagementGuide;
import com.example.plant_trial.utils.DummyData;

import java.util.stream.Collectors;

public class SuggestionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_suggestions);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Toolbar toolbar = findViewById(R.id.toolbar_suggestions);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Management Suggestions");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        String diseaseName = getIntent().getStringExtra("DISEASE_NAME");
        DiseaseManagementGuide guide = DummyData.getManagementGuideForDisease(diseaseName);

        if (guide != null) {
            // Find all TextViews from the layout
            TextView tempTv = findViewById(R.id.suggestion_temperature);
            TextView waterTv = findViewById(R.id.suggestion_water);
            TextView humidityTv = findViewById(R.id.suggestion_humidity);
            TextView pesticidesTv = findViewById(R.id.suggestion_pesticides);
            TextView fertilizersTv = findViewById(R.id.suggestion_fertilizers);

            // Set the text from the guide object
            tempTv.setText(guide.getCriticalTemperature());
            waterTv.setText(guide.getWateringInfo());
            humidityTv.setText(guide.getHumidityInfo());

            // Format the lists with bullet points
            String pesticides = guide.getOrganicPesticides().stream().map(p -> "• " + p).collect(Collectors.joining("\n"));
            pesticidesTv.setText(pesticides);

            String fertilizers = guide.getOrganicFertilizers().stream().map(f -> "• " + f).collect(Collectors.joining("\n"));
            fertilizersTv.setText(fertilizers);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Handle the back arrow in the toolbar
        return true;
    }
}
