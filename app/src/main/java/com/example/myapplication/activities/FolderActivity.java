package com.example.myapplication.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.VideoAdapter;
import com.example.myapplication.models.VideoModel;
import com.example.myapplication.utils.VideoFetcher;

import java.util.ArrayList;
import java.util.List;

public class FolderActivity extends AppCompatActivity implements VideoAdapter.OnVideoClickListener {

    private ImageButton btnBack;
    private TextView tvFolderTitle;
    private RecyclerView recyclerVideos;
    private VideoAdapter videoAdapter;
    private VideoFetcher videoFetcher;
    private List<VideoModel> videos;
    private String folderPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder);

        folderPath = getIntent().getStringExtra("folder_path");
        String folderName = getIntent().getStringExtra("folder_name");

        btnBack = findViewById(R.id.btn_back);
        tvFolderTitle = findViewById(R.id.tv_folder_title);
        recyclerVideos = findViewById(R.id.recycler_videos);

        tvFolderTitle.setText(folderName);
        btnBack.setOnClickListener(v -> finish());

        videoFetcher = new VideoFetcher(this);
        videos = new ArrayList<>();

        setupRecyclerView();
        loadVideos();
    }

    private void setupRecyclerView() {
        videoAdapter = new VideoAdapter(this);
        recyclerVideos.setLayoutManager(new GridLayoutManager(this, 2));
        recyclerVideos.setAdapter(videoAdapter);
        recyclerVideos.setHasFixedSize(true);
    }

    private void loadVideos() {
        new Thread(() -> {
            videoFetcher.fetchAllVideos(); // Initialize folders
            videos = videoFetcher.getVideosInFolder(folderPath);
            runOnUiThread(() -> videoAdapter.setVideos(videos));
        }).start();
    }

    @Override
    public void onVideoClick(VideoModel video, int position) {
        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("video_path", video.getPath());
        intent.putExtra("video_title", video.getTitle());
        intent.putExtra("video_id", video.getId());
        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    @Override
    public void onMoreClick(VideoModel video, View anchor) {
        PopupMenu popup = new PopupMenu(this, anchor);
        popup.getMenuInflater().inflate(R.menu.menu_video_options, popup.getMenu());
        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.action_play) {
                onVideoClick(video, 0);
                return true;
            } else if (itemId == R.id.action_share) {
                shareVideo(video);
                return true;
            } else if (itemId == R.id.action_details) {
                showDetails(video);
                return true;
            }
            return false;
        });
        popup.show();
    }

    private void shareVideo(VideoModel video) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("video/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, android.net.Uri.parse(video.getPath()));
        startActivity(Intent.createChooser(shareIntent, "Share Video"));
    }

    private void showDetails(VideoModel video) {
        String details = "Title: " + video.getTitle() + "
" +
                "Duration: " + video.getFormattedDuration() + "
" +
                "Size: " + video.getFormattedSize() + "
" +
                "Path: " + video.getPath();
        new androidx.appcompat.app.AlertDialog.Builder(this, R.style.Theme_PremiumVideoPlayer)
            .setTitle("Video Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show();
    }
}
