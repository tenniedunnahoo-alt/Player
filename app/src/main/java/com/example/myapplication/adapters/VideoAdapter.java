package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.VideoModel;

import java.util.ArrayList;
import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<VideoModel> videos;
    private OnVideoClickListener listener;

    public interface OnVideoClickListener {
        void onVideoClick(VideoModel video, int position);
        void onMoreClick(VideoModel video, View anchor);
    }

    public VideoAdapter(OnVideoClickListener listener) {
        this.videos = new ArrayList<>();
        this.listener = listener;
    }

    public void setVideos(List<VideoModel> videos) {
        this.videos = videos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_video, parent, false);
        return new VideoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        VideoModel video = videos.get(position);
        holder.tvTitle.setText(video.getTitle());
        holder.tvDuration.setText(video.getFormattedDuration());

        Glide.with(holder.itemView.getContext())
            .load(video.getThumbnail())
            .placeholder(R.drawable.ic_folder)
            .centerCrop()
            .into(holder.imgThumbnail);

        final int pos = position;
        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onVideoClick(video, pos);
        });

        holder.btnMore.setOnClickListener(v -> {
            if (listener != null) listener.onMoreClick(video, v);
        });
    }

    @Override
    public int getItemCount() {
        return videos.size();
    }

    static class VideoViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgThumbnail;
        TextView tvTitle, tvDuration;
        ImageButton btnMore;

        VideoViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_recent);
            imgThumbnail = itemView.findViewById(R.id.img_thumbnail);
            tvTitle = itemView.findViewById(R.id.tv_video_title);
            tvDuration = itemView.findViewById(R.id.tv_duration);
            btnMore = itemView.findViewById(R.id.btn_more);
        }
    }
}
