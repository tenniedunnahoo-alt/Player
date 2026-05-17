package com.example.myapplication.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {
    private static final String PREF_NAME = "PremiumPlayerPrefs";
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    public void setDarkMode(boolean enabled) {
        editor.putBoolean("dark_mode", enabled).apply();
    }

    public boolean isDarkMode() {
        return prefs.getBoolean("dark_mode", true);
    }

    public void setAmoledBlack(boolean enabled) {
        editor.putBoolean("amoled_black", enabled).apply();
    }

    public boolean isAmoledBlack() {
        return prefs.getBoolean("amoled_black", true);
    }

    public void setResumePlayback(boolean enabled) {
        editor.putBoolean("resume_playback", enabled).apply();
    }

    public boolean isResumePlayback() {
        return prefs.getBoolean("resume_playback", true);
    }

    public void setAutoPlay(boolean enabled) {
        editor.putBoolean("auto_play", enabled).apply();
    }

    public boolean isAutoPlay() {
        return prefs.getBoolean("auto_play", true);
    }

    public void setDoubleTapSeek(boolean enabled) {
        editor.putBoolean("double_tap_seek", enabled).apply();
    }

    public boolean isDoubleTapSeek() {
        return prefs.getBoolean("double_tap_seek", true);
    }

    public void setGesturesEnabled(boolean enabled) {
        editor.putBoolean("gestures_enabled", enabled).apply();
    }

    public boolean isGesturesEnabled() {
        return prefs.getBoolean("gestures_enabled", true);
    }

    public void saveLastPosition(String videoId, long position) {
        editor.putLong("pos_" + videoId, position).apply();
    }

    public long getLastPosition(String videoId) {
        return prefs.getLong("pos_" + videoId, 0);
    }

    public void addToRecent(String videoPath) {
        String recent = prefs.getString("recent_videos", "");
        if (!recent.contains(videoPath)) {
            recent = videoPath + "," + recent;
            String[] arr = recent.split(",");
            if (arr.length > 20) {
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i < 20; i++) {
                    if (i > 0) sb.append(",");
                    sb.append(arr[i]);
                }
                recent = sb.toString();
            }
            editor.putString("recent_videos", recent).apply();
        }
    }

    public String[] getRecentVideos() {
        String recent = prefs.getString("recent_videos", "");
        if (recent.isEmpty()) return new String[0];
        return recent.split(",");
    }

    public void clearRecent() {
        editor.putString("recent_videos", "").apply();
    }
}
