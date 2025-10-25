package com.example.plant_trial.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import androidx.core.content.ContextCompat;

// ADD ALL OF THESE IMPORTS FOR THE CHART
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;


import com.example.plant_trial.DiseaseLibraryActivity;
import com.example.plant_trial.HistoryActivity;
import com.example.plant_trial.MainActivity;
import com.example.plant_trial.MoreOptionsActivity;
import com.example.plant_trial.R;
import com.example.plant_trial.ScanPlantActivity;
import com.example.plant_trial.TreatmentsActivity;

import java.util.ArrayList;

public class DashboardFragment extends Fragment {

    private BarChart weeklyScanChart;
    private ImageView voiceSearchButton; // Declare the button
    private ActivityResultLauncher<Intent> voiceLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 2. Register the launcher. This code runs when the voice input is finished.
        voiceLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Intent data = result.getData();
                        // The spoken text is returned as a list of possible matches
                        ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                        if (results != null && !results.isEmpty()) {
                            // We take the first result as it's the most likely match
                            String spokenText = results.get(0);

                            // --- THIS IS WHERE YOU USE THE SPOKEN TEXT ---
                            // For example, show it in a Toast or perform a search
                            Toast.makeText(getContext(), "You said: " + spokenText, Toast.LENGTH_LONG).show();
                            // You can now use the 'spokenText' variable to search your app
                        }
                    }
                }
        );
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);

        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        weeklyScanChart = view.findViewById(R.id.weekly_scan_chart);
        setupWeeklyScanChart();

        voiceSearchButton = view.findViewById(R.id.voice_search_button);
        voiceSearchButton.setOnClickListener(v -> {
            startVoiceRecognition();
        });
    }

    private void startVoiceRecognition() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak now...");

        try {
            // Launch the activity using the launcher we registered
            voiceLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            // This happens if the device does not have a speech recognition app
            Toast.makeText(getContext(), "Sorry, your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }


    private void setupWeeklyScanChart() {
        // === 1. Create Sample Data ===
        ArrayList<BarEntry> healthyPlantsEntries = new ArrayList<>();
        healthyPlantsEntries.add(new BarEntry(0f, 2f)); // Corresponds to X-axis position 0
        healthyPlantsEntries.add(new BarEntry(1f, 1f)); // Corresponds to X-axis position 1
        healthyPlantsEntries.add(new BarEntry(2f, 3f));
        healthyPlantsEntries.add(new BarEntry(3f, 2f));
        healthyPlantsEntries.add(new BarEntry(4f, 4f));
        healthyPlantsEntries.add(new BarEntry(5f, 1f));
        healthyPlantsEntries.add(new BarEntry(6f, 2f));

        ArrayList<BarEntry> diseasedPlantsEntries = new ArrayList<>();
        diseasedPlantsEntries.add(new BarEntry(0f, 1f));
        diseasedPlantsEntries.add(new BarEntry(1f, 2f));
        diseasedPlantsEntries.add(new BarEntry(2f, 1f));
        diseasedPlantsEntries.add(new BarEntry(3f, 0f));
        diseasedPlantsEntries.add(new BarEntry(4f, 1f));
        diseasedPlantsEntries.add(new BarEntry(5f, 3f));
        diseasedPlantsEntries.add(new BarEntry(6f, 1f));

        // === 2. Create Data Sets ===
        BarDataSet healthyDataSet = new BarDataSet(healthyPlantsEntries, "Healthy Plants");
        healthyDataSet.setColor(ContextCompat.getColor(getContext(), R.color.healthy_green));

        BarDataSet diseasedDataSet = new BarDataSet(diseasedPlantsEntries, "Diseased Plants");
        diseasedDataSet.setColor(ContextCompat.getColor(getContext(), R.color.diseased_orange));

        BarData barData = new BarData(healthyDataSet, diseasedDataSet);
        barData.setValueFormatter(new IntegerValueFormatter());
        barData.setValueTextSize(10f);

        // === 3. Configure Spacing for Grouped Bars ===
        float groupSpace = 0.4f;
        float barSpace = 0.05f;
        float barWidth = 0.25f;
        // (0.25 + 0.05) * 2 + 0.4 = 1.0 -> Each group takes up 1 unit on the X-axis
        barData.setBarWidth(barWidth);

        // === 4. Configure Chart Appearance & Axes (NEW SIMPLIFIED LOGIC) ===
        weeklyScanChart.setData(barData);
        weeklyScanChart.getDescription().setEnabled(false);
        weeklyScanChart.setDrawGridBackground(false);
        weeklyScanChart.setTouchEnabled(false);

        Legend legend = weeklyScanChart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false);
        legend.setTextSize(12f);

        XAxis xAxis = weeklyScanChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f); // Ensure labels are for each integer index
        xAxis.setValueFormatter(new DateAxisValueFormatter());
        xAxis.setLabelRotationAngle(-45);
        xAxis.setTextSize(10f);


        // This is the key change: we are manually defining the space for each bar group
        xAxis.setAxisMinimum(-0.5f);
        xAxis.setAxisMaximum(6.5f);

        //weeklyScanChart.groupBars(-0.5f, groupSpace, barSpace);

        YAxis leftAxis = weeklyScanChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setGranularity(1f);
        leftAxis.setInverted(false);



        weeklyScanChart.getAxisRight().setEnabled(false);
        weeklyScanChart.groupBars(-0.5f,groupSpace,barSpace);
        //weeklyScanChart.getLegend().setEnabled(true);

        // Refresh the chart
        weeklyScanChart.invalidate();
    }


   
    private static class DateAxisValueFormatter extends ValueFormatter {
        private final SimpleDateFormat sdf = new SimpleDateFormat("MMM dd", Locale.ENGLISH);
        private final Calendar calendar = Calendar.getInstance();

        @Override
        public String getFormattedValue(float value) {
            calendar.setTimeInMillis(System.currentTimeMillis());
            calendar.add(Calendar.DAY_OF_YEAR, -6 + (int) value);
            return sdf.format(calendar.getTime());
        }
    }

    // This class formats the numbers on top of the bars as integers
    private static class IntegerValueFormatter extends ValueFormatter {
        @Override
        public String getFormattedValue(float value) {
            if (value == 0) {
                return ""; // Don't show a '0' label for empty bars
            }
            return String.valueOf((int) value);
        }
    }
}



