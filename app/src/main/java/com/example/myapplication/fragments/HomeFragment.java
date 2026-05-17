package com.example.myapplication.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.activities.FolderActivity;
import com.example.myapplication.activities.PlayerActivity;
import com.example.myapplication.activities.SettingsActivity;
import com.example.myapplication.adapters.FolderAdapter;
import com.example.myapplication.models.FolderModel;
import com.example.myapplication.models.VideoModel;
import com.example.myapplication.utils.PreferenceManager;
import com.example.myapplication.utils.VideoFetcher;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements FolderAdapter.OnFolderClickListener {

    private static final int PERMISSION_REQUEST = 100;

    private RecyclerView recyclerFolders;
    private FolderAdapter folderAdapter;
    private VideoFetcher videoFetcher;
    private PreferenceManager prefManager;
    private List<VideoModel> allVideos;
    private List<FolderModel> allFolders;

    private CardView cardRecent;
    private ImageView imgRecentThumb;
    private TextView tvRecentTitle, tvRecentDuration;
    private ProgressBar progressRecent;
    private ImageButton btnPlayRecent, btnClearRecent, btnSearch, btnSettingsTop;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        prefManager = new PreferenceManager(requireContext());
        videoFetcher = new VideoFetcher(requireContext());
        allVideos = new ArrayList<>();
        allFolders = new ArrayList<>();

        initViews(view);
        setupRecyclerView();
        checkPermissionAndLoad();
    }

    private void initViews(View view) {
        recyclerFolders = view.findViewById(R.id.recycler_folders);
        cardRecent = view.findViewById(R.id.card_recent);
        imgRecentThumb = view.findViewById(R.id.img_recent_thumbnail);
        tvRecentTitle = view.findViewById(R.id.tv_recent_title);
        tvRecentDuration = view.findViewById(R.id.tv_recent_duration);
        progressRecent = view.findViewById(R.id.progress_recent);
        btnPlayRecent = view.findViewById(R.id.btn_play_recent);
        btnClearRecent = view.findViewById(R.id.btn_clear_recent);
        btnSearch = view.findViewById(R.id.btn_search);
        btnSettingsTop = view.findViewById(R.id.btn_settings_top);

        btnClearRecent.setOnClickListener(v -> showClearDialog());
        btnSearch.setOnClickListener(v -> Toast.makeText(requireContext(), "Search coming soon", Toast.LENGTH_SHORT).show());
        btnSettingsTop.setOnClickListener(v -> startActivity(new Intent(requireContext(), SettingsActivity.class)));
    }

    private void setupRecyclerView() {
        folderAdapter = new FolderAdapter(this);
        recyclerFolders.setLayoutManager(new GridLayoutManager(requireContext(), 2));
        recyclerFolders.setAdapter(folderAdapter);
        recyclerFolders.setHasFixedSize(true);
        recyclerFolders.setNestedScrollingEnabled(false);
    }

    private void checkPermissionAndLoad() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU 
            ? Manifest.permission.READ_MEDIA_VIDEO 
            : Manifest.permission.READ_EXTERNAL_STORAGE;

        if (ContextCompat.checkSelfPermission(requireContext(), permission) == PackageManager.PERMISSION_GRANTED) {
            loadVideos();
        } else {
            requestPermissions(new String[]{permission}, PERMISSION_REQUEST);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadVideos();
            } else {
                Toast.makeText(requireContext(), "Permission required to access videos", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void loadVideos() {
        new Thread(() -> {
            allVideos = videoFetcher.fetchAllVideos();
            allFolders = videoFetcher.getFolders();

            new Handler(Looper.getMainLooper()).post(() -> {
                folderAdapter.setFolders(allFolders);
                loadRecentVideo();
            });
        }).start();
    }

    private void loadRecentVideo() {
        String[] recentPaths = prefManager.getRecentVideos();
        if (recentPaths.length > 0) {
            String recentPath = recentPaths[0];
            for (VideoModel video : allVideos) {
                if (video.getPath().equals(recentPath)) {
                    cardRecent.setVisibility(View.VISIBLE);
                    tvRecentTitle.setText(video.getTitle());
                    tvRecentDuration.setText(video.getFormattedDuration());

                    long pos = prefManager.getLastPosition(video.getId());
                    if (video.getDuration() > 0) {
                        progressRecent.setProgress((int) ((pos * 100) / video.getDuration()));
                    }

                    Glide.with(requireContext())
                        .load(video.getThumbnail())
                        .placeholder(R.drawable.ic_folder)
                        .into(imgRecentThumb);

                    final VideoModel finalVideo = video;
                    btnPlayRecent.setOnClickListener(v -> playVideo(finalVideo));
                    cardRecent.setOnClickListener(v -> playVideo(finalVideo));
                    return;
                }
            }
        }
        cardRecent.setVisibility(View.GONE);
    }

    private void playVideo(VideoModel video) {
        prefManager.addToRecent(video.getPath());
        Intent intent = new Intent(requireContext(), PlayerActivity.class);
        intent.putExtra("video_path", video.getPath());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("video_id", video.getId());
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void showClearDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext(), R.style.Theme_PremiumVideoPlayer);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clear, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btn_cancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btn_clear).setOnClickListener(v -> {
            prefManager.clearRecent();
            cardRecent.setVisibility(View.GONE);
            dialog.dismiss();
            Toast.makeText(requireContext(), "Recently played cleared", Toast.LENGTH_SHORT).show();
        });

        dialog.show();
    }

    @Override
    public void onFolderClick(FolderModel folder) {
        Intent intent = new Intent(requireContext(), FolderActivity.class);
        intent.putExtra("folder_name", folder.getName());
        intent.putExtra("folder_path", folder.getPath());
        startActivity(intent);
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }
}
