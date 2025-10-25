package com.example.plant_trial.models;

public class HistoryItem {
    private String title;
    private String date;
    private int imageResource;

    public HistoryItem(String title, String date, int imageResource) {
        this.title = title;
        this.date = date;
        this.imageResource = imageResource;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public int getImageResource() {
        return imageResource;
    }
}
