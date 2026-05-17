package com.example.myapplication.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

import com.example.myapplication.models.FolderModel;
import com.example.myapplication.models.VideoModel;
import com.example.myapplication.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VideoFetcher {
    private Context context;
    private Map<String, FolderModel> folderMap;

    public VideoFetcher(Context context) {
        this.context = context;
        this.folderMap = new HashMap<>();
    }

    public List<VideoModel> fetchAllVideos() {
        List<VideoModel> videos = new ArrayList<>();
        folderMap.clear();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
            MediaStore.Video.Media._ID,
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATE_ADDED,
            MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Video.Media.BUCKET_ID
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        try (Cursor cursor = context.getContentResolver().query(uri, projection, null, null, sortOrder)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int pathIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE);
                int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);
                int bucketIndex = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME);

                do {
                    String id = cursor.getString(idIndex);
                    String title = cursor.getString(titleIndex);
                    String path = cursor.getString(pathIndex);
                    long duration = cursor.getLong(durationIndex);
                    long size = cursor.getLong(sizeIndex);
                    long dateAdded = cursor.getLong(dateIndex);
                    String folderName = cursor.getString(bucketIndex);

                    if (folderName == null || folderName.isEmpty()) {
                        folderName = new File(path).getParentFile().getName();
                    }

                    String folderPath = new File(path).getParent();

                    VideoModel video = new VideoModel(id, title, path, folderName, folderPath, duration, size, dateAdded);
                    video.setThumbnail(path);
                    videos.add(video);

                    // Add to folder
                    if (!folderMap.containsKey(folderPath)) {
                        int iconRes = getFolderIcon(folderName);
                        folderMap.put(folderPath, new FolderModel(folderName, folderPath, iconRes));
                    }
                    folderMap.get(folderPath).addVideo(video);

                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return videos;
    }

    public List<FolderModel> getFolders() {
        List<FolderModel> folders = new ArrayList<>();
        for (FolderModel folder : folderMap.values()) {
            folders.add(folder);
        }
        return folders;
    }

    private int getFolderIcon(String folderName) {
        String lower = folderName.toLowerCase();
        if (lower.contains("download")) return R.drawable.ic_download;
        if (lower.contains("whatsapp")) return R.drawable.ic_whatsapp;
        if (lower.contains("telegram")) return R.drawable.ic_telegram;
        if (lower.contains("movie") || lower.contains("film")) return R.drawable.ic_movie;
        if (lower.contains("camera") || lower.contains("dcim")) return R.drawable.ic_camera;
        if (lower.contains("screen") || lower.contains("record")) return R.drawable.ic_screen_record;
        return R.drawable.ic_folder;
    }

    public List<VideoModel> getVideosInFolder(String folderPath) {
        if (folderMap.containsKey(folderPath)) {
            return folderMap.get(folderPath).getVideos();
        }
        return new ArrayList<>();
    }
}
