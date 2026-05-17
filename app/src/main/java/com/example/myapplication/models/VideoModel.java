package com.example.myapplication.models;

public class VideoModel {
    private String id;
    private String title;
    private String path;
    private String folderName;
    private String folderPath;
    private long duration;
    private long size;
    private long dateAdded;
    private String thumbnail;
    private long lastPosition;

    public VideoModel() {}

    public VideoModel(String id, String title, String path, String folderName, 
                      String folderPath, long duration, long size, long dateAdded) {
        this.id = id;
        this.title = title;
        this.path = path;
        this.folderName = folderName;
        this.folderPath = folderPath;
        this.duration = duration;
        this.size = size;
        this.dateAdded = dateAdded;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getFolderName() { return folderName; }
    public void setFolderName(String folderName) { this.folderName = folderName; }
    public String getFolderPath() { return folderPath; }
    public void setFolderPath(String folderPath) { this.folderPath = folderPath; }
    public long getDuration() { return duration; }
    public void setDuration(long duration) { this.duration = duration; }
    public long getSize() { return size; }
    public void setSize(long size) { this.size = size; }
    public long getDateAdded() { return dateAdded; }
    public void setDateAdded(long dateAdded) { this.dateAdded = dateAdded; }
    public String getThumbnail() { return thumbnail; }
    public void setThumbnail(String thumbnail) { this.thumbnail = thumbnail; }
    public long getLastPosition() { return lastPosition; }
    public void setLastPosition(long lastPosition) { this.lastPosition = lastPosition; }

    public String getFormattedDuration() {
        long seconds = duration / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getFormattedSize() {
        if (size < 1024) return size + " B";
        int exp = (int) (Math.log(size) / Math.log(1024));
        char unit = "KMGTPE".charAt(exp - 1);
        return String.format("%.1f %sB", size / Math.pow(1024, exp), unit);
    }
}
