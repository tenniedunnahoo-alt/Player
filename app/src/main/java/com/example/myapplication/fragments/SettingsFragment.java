package com.example.myapplication.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.myapplication.R;
import com.example.myapplication.utils.PreferenceManager;
import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    private SwitchMaterial switchDarkMode, switchAmoled, switchResume, switchAutoPlay, switchDoubleTap, switchGestures;
    private PreferenceManager prefManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PreferenceManager(requireContext());

        switchDarkMode = view.findViewById(R.id.switch_dark_mode);
        switchAmoled = view.findViewById(R.id.switch_amoled);
        switchResume = view.findViewById(R.id.switch_resume);
        switchAutoPlay = view.findViewById(R.id.switch_autoplay);
        switchDoubleTap = view.findViewById(R.id.switch_double_tap);
        switchGestures = view.findViewById(R.id.switch_gestures);

        // Load saved states
        switchDarkMode.setChecked(prefManager.isDarkMode());
        switchAmoled.setChecked(prefManager.isAmoledBlack());
        switchResume.setChecked(prefManager.isResumePlayback());
        switchAutoPlay.setChecked(prefManager.isAutoPlay());
        switchDoubleTap.setChecked(prefManager.isDoubleTapSeek());
        switchGestures.setChecked(prefManager.isGesturesEnabled());

        // Listeners
        switchDarkMode.setOnCheckedChangeListener((button, isChecked) -> prefManager.setDarkMode(isChecked));
        switchAmoled.setOnCheckedChangeListener((button, isChecked) -> prefManager.setAmoledBlack(isChecked));
        switchResume.setOnCheckedChangeListener((button, isChecked) -> prefManager.setResumePlayback(isChecked));
        switchAutoPlay.setOnCheckedChangeListener((button, isChecked) -> prefManager.setAutoPlay(isChecked));
        switchDoubleTap.setOnCheckedChangeListener((button, isChecked) -> prefManager.setDoubleTapSeek(isChecked));
        switchGestures.setOnCheckedChangeListener((button, isChecked) -> prefManager.setGesturesEnabled(isChecked));
    }
}
