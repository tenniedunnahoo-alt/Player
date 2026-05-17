package com.example.myapplication.activities;

import android.content.ContentResolver;
import android.content.Context;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.ui.PlayerView;

import com.example.myapplication.R;
import com.example.myapplication.utils.PreferenceManager;

public class PlayerActivity extends AppCompatActivity {

    private PlayerView playerView;
    private ExoPlayer player;
    private PreferenceManager prefManager;

    // Controls
    private FrameLayout controlsOverlay, lockOverlay;
    private ImageButton btnBack, btnLock, btnPlayPause, btnReplay, btnForward;
    private ImageButton btnSubtitle, btnSpeed;
    private SeekBar seekBar;
    private TextView tvTitle, tvCurrentTime, tvTotalTime;
    private ProgressBar volumeIndicator, brightnessIndicator;

    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable hideControlsRunnable;
    private boolean isControlsVisible = true;
    private boolean isLocked = false;

    private AudioManager audioManager;
    private int maxVolume;
    private float startBrightness;
    private int startVolume;
    private float startY;
    private static final int SWIPE_THRESHOLD = 50;

    private String videoPath, videoTitle, videoId;
    private long lastPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        videoPath = getIntent().getStringExtra("video_path");
        videoTitle = getIntent().getStringExtra("video_title");
        videoId = getIntent().getStringExtra("video_id");

        prefManager = new PreferenceManager(this);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        initViews();
        setupPlayer();
        setupControls();
        setupGestures();
        startHideControlsTimer();
    }

    private void initViews() {
        playerView = findViewById(R.id.player_view);
        controlsOverlay = findViewById(R.id.controls_overlay);
        lockOverlay = findViewById(R.id.lock_overlay);
        btnBack = findViewById(R.id.btn_player_back);
        btnLock = findViewById(R.id.btn_lock);
        btnPlayPause = findViewById(R.id.btn_play_pause);
        btnReplay = findViewById(R.id.btn_replay);
        btnForward = findViewById(R.id.btn_forward);
        btnSubtitle = findViewById(R.id.btn_subtitle);
        btnSpeed = findViewById(R.id.btn_speed);
        seekBar = findViewById(R.id.seekbar);
        tvTitle = findViewById(R.id.tv_player_title);
        tvCurrentTime = findViewById(R.id.tv_current_time);
        tvTotalTime = findViewById(R.id.tv_total_time);
        volumeIndicator = findViewById(R.id.volume_indicator);
        brightnessIndicator = findViewById(R.id.brightness_indicator);

        tvTitle.setText(videoTitle);
    }

    private void setupPlayer() {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        Uri uri = Uri.parse(videoPath);
        MediaItem mediaItem = MediaItem.fromUri(uri);
        player.setMediaItem(mediaItem);

        if (prefManager.isResumePlayback() && videoId != null) {
            lastPosition = prefManager.getLastPosition(videoId);
            if (lastPosition > 0) {
                player.seekTo(lastPosition);
            }
        }

        player.prepare();
        player.play();

        player.addListener(new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int state) {
                if (state == Player.STATE_READY) {
                    updateProgress();
                    tvTotalTime.setText(formatTime(player.getDuration()));
                    seekBar.setMax((int) player.getDuration());
                }
            }

            @Override
            public void onIsPlayingChanged(boolean isPlaying) {
                btnPlayPause.setImageResource(isPlaying ? R.drawable.ic_pause : R.drawable.ic_play);
            }
        });
    }

    private void setupControls() {
        btnBack.setOnClickListener(v -> finish());

        btnLock.setOnClickListener(v -> {
            isLocked = true;
            controlsOverlay.setVisibility(View.GONE);
            lockOverlay.setVisibility(View.VISIBLE);
            isControlsVisible = false;
        });

        lockOverlay.setOnClickListener(v -> {
            isLocked = false;
            lockOverlay.setVisibility(View.GONE);
            showControls();
        });

        btnPlayPause.setOnClickListener(v -> {
            if (player.isPlaying()) {
                player.pause();
            } else {
                player.play();
            }
        });

        btnReplay.setOnClickListener(v -> {
            long pos = player.getCurrentPosition() - 10000;
            player.seekTo(Math.max(0, pos));
        });

        btnForward.setOnClickListener(v -> {
            long pos = player.getCurrentPosition() + 10000;
            player.seekTo(Math.min(player.getDuration(), pos));
        });

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    player.seekTo(progress);
                    tvCurrentTime.setText(formatTime(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnSpeed.setOnClickListener(v -> showSpeedDialog());
        btnSubtitle.setOnClickListener(v -> Toast.makeText(this, "Subtitle feature coming soon", Toast.LENGTH_SHORT).show());
    }

    private void setupGestures() {
        GestureDetector gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (!prefManager.isDoubleTapSeek() || isLocked) return false;

                float x = e.getX();
                float width = getResources().getDisplayMetrics().widthPixels;

                if (x < width / 3) {
                    // Double tap left - rewind
                    long pos = player.getCurrentPosition() - 10000;
                    player.seekTo(Math.max(0, pos));
                    Toast.makeText(PlayerActivity.this, "-10s", Toast.LENGTH_SHORT).show();
                } else if (x > 2 * width / 3) {
                    // Double tap right - forward
                    long pos = player.getCurrentPosition() + 10000;
                    player.seekTo(Math.min(player.getDuration(), pos));
                    Toast.makeText(PlayerActivity.this, "+10s", Toast.LENGTH_SHORT).show();
                } else {
                    // Center - play/pause
                    if (player.isPlaying()) player.pause();
                    else player.play();
                }
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isLocked) {
                    lockOverlay.setVisibility(View.VISIBLE);
                    return true;
                }
                toggleControls();
                return true;
            }
        });

        playerView.setOnTouchListener((v, event) -> {
            if (!prefManager.isGesturesEnabled() || isLocked) {
                return gestureDetector.onTouchEvent(event);
            }

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    startY = event.getY();
                    startVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    try {
                        startBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
                    } catch (Settings.SettingNotFoundException e) {
                        startBrightness = 128;
                    }
                    break;

                case MotionEvent.ACTION_MOVE:
                    float deltaY = startY - event.getY();
                    float width = getResources().getDisplayMetrics().widthPixels;

                    if (Math.abs(deltaY) > SWIPE_THRESHOLD) {
                        if (event.getX() < width / 2) {
                            // Left side - brightness
                            float brightnessDelta = (deltaY / getResources().getDisplayMetrics().heightPixels) * 255;
                            float newBrightness = Math.max(0, Math.min(255, startBrightness + brightnessDelta));
                            WindowManager.LayoutParams lp = getWindow().getAttributes();
                            lp.screenBrightness = newBrightness / 255f;
                            getWindow().setAttributes(lp);
                            brightnessIndicator.setVisibility(View.VISIBLE);
                            brightnessIndicator.setProgress((int) newBrightness);
                        } else {
                            // Right side - volume
                            float volumeDelta = (deltaY / getResources().getDisplayMetrics().heightPixels) * maxVolume;
                            int newVolume = Math.max(0, Math.min(maxVolume, startVolume + (int) volumeDelta));
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0);
                            volumeIndicator.setVisibility(View.VISIBLE);
                            volumeIndicator.setProgress((int) ((newVolume / (float) maxVolume) * 100));
                        }
                    }
                    break;

                case MotionEvent.ACTION_UP:
                    volumeIndicator.setVisibility(View.GONE);
                    brightnessIndicator.setVisibility(View.GONE);
                    break;
            }

            return gestureDetector.onTouchEvent(event);
        });
    }

    private void toggleControls() {
        if (isControlsVisible) {
            hideControls();
        } else {
            showControls();
        }
    }

    private void showControls() {
        controlsOverlay.setVisibility(View.VISIBLE);
        isControlsVisible = true;
        startHideControlsTimer();
    }

    private void hideControls() {
        controlsOverlay.setVisibility(View.GONE);
        isControlsVisible = false;
        handler.removeCallbacks(hideControlsRunnable);
    }

    private void startHideControlsTimer() {
        handler.removeCallbacks(hideControlsRunnable);
        hideControlsRunnable = () -> {
            if (isControlsVisible && !isLocked) {
                hideControls();
            }
        };
        handler.postDelayed(hideControlsRunnable, 3000);
    }

    private void updateProgress() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (player != null && player.isPlaying()) {
                    long current = player.getCurrentPosition();
                    seekBar.setProgress((int) current);
                    tvCurrentTime.setText(formatTime(current));
                    handler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    private String formatTime(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;
        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds);
        }
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void showSpeedDialog() {
        String[] speeds = {"0.5x", "0.75x", "1.0x", "1.25x", "1.5x", "2.0x"};
        float[] speedValues = {0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f};

        new AlertDialog.Builder(this, R.style.Theme_PremiumVideoPlayer)
            .setTitle("Playback Speed")
            .setItems(speeds, (dialog, which) -> {
                player.setPlaybackSpeed(speedValues[which]);
                Toast.makeText(this, "Speed: " + speeds[which], Toast.LENGTH_SHORT).show();
            })
            .show();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            if (videoId != null) {
                prefManager.saveLastPosition(videoId, player.getCurrentPosition());
            }
            player.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null) {
            player.play();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        if (player != null) {
            if (videoId != null) {
                prefManager.saveLastPosition(videoId, player.getCurrentPosition());
            }
            player.release();
            player = null;
        }
    }

    @Override
    public void onBackPressed() {
        if (isLocked) {
            lockOverlay.setVisibility(View.VISIBLE);
            return;
        }
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
}
