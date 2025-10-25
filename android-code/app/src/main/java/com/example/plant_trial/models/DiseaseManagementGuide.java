package com.example.plant_trial.models;

import java.util.List;

public class DiseaseManagementGuide {

    private String criticalTemperature;
    private String wateringInfo;
    private String humidityInfo;
    private List<String> organicPesticides;
    private List<String> organicFertilizers;

    // --- Constructor ---
    public DiseaseManagementGuide(String criticalTemperature, String wateringInfo, String humidityInfo, List<String> organicPesticides, List<String> organicFertilizers) {
        this.criticalTemperature = criticalTemperature;
        this.wateringInfo = wateringInfo;
        this.humidityInfo = humidityInfo;
        this.organicPesticides = organicPesticides;
        this.organicFertilizers = organicFertilizers;
    }

    // --- Getters for all fields ---
    public String getCriticalTemperature() { return criticalTemperature; }
    public String getWateringInfo() { return wateringInfo; }
    public String getHumidityInfo() { return humidityInfo; }
    public List<String> getOrganicPesticides() { return organicPesticides; }
    public List<String> getOrganicFertilizers() { return organicFertilizers; }

}
