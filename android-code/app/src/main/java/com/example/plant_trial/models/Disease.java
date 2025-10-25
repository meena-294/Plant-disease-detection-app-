package com.example.plant_trial.models;


public class Disease {
    private String name;
    private String description;
    private String friendlyTreatments;
    private String chemicalTreatments;
    private String Symptoms;
    private String prevention;
    private int imageResource;

    public Disease(String name, String description,String Symptoms,String prevention,String friendlyTreatments,String chemicalTreatments, int imageResource) {
        this.name = name;
        this.description = description;
        this.Symptoms=Symptoms;
        this.prevention=prevention;
        this.friendlyTreatments=friendlyTreatments;
        this.chemicalTreatments=chemicalTreatments;

        this.imageResource = imageResource;


    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }



    public int getImageResource() {
        return imageResource;
    }

    public String getFriendlyTreatments() {
        return friendlyTreatments;
    }

    public String getChemicalTreatments() {
        return chemicalTreatments;
    }

    public String getSymptoms() {
        return Symptoms;
    }

    public String getPrevention() {
        return prevention;
    }
}