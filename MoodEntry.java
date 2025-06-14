package com.example.woofly;

public class MoodEntry {
    private String mood;
    private String tip;
    private String imagePath;
    private String timestamp;

    public MoodEntry(String mood, String tip, String imagePath, String timestamp) {
        this.mood = mood;
        this.tip = tip;
        this.imagePath = imagePath;
        this.timestamp = timestamp;
    }

    public String getMood() { return mood; }
    public String getTip() { return tip; }
    public String getImagePath() { return imagePath; }
    public String getTimestamp() { return timestamp; }
}
