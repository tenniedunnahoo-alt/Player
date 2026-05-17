package com.example.myapplication.models;

import java.util.ArrayList;
import java.util.List;

public class FolderModel {
    private String name;
    private String path;
    private int videoCount;
    private List<VideoModel> videos;
    private int iconRes;

    public FolderModel(String name, String path, int iconRes) {
        this.name = name;
        this.path = path;
        this.iconRes = iconRes;
        this.videos = new ArrayList<>();
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public int getVideoCount() { return videoCount; }
    public void setVideoCount(int count) { this.videoCount = count; }
    public List<VideoModel> getVideos() { return videos; }
    public void addVideo(VideoModel video) { 
        videos.add(video); 
        videoCount = videos.size();
    }
    public int getIconRes() { return iconRes; }
    public void setIconRes(int iconRes) { this.iconRes = iconRes; }
}
