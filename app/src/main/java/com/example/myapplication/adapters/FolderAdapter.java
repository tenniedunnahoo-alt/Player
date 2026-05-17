package com.example.myapplication.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.myapplication.R;
import com.example.myapplication.models.FolderModel;

import java.util.ArrayList;
import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private List<FolderModel> folders;
    private OnFolderClickListener listener;

    public interface OnFolderClickListener {
        void onFolderClick(FolderModel folder);
    }

    public FolderAdapter(OnFolderClickListener listener) {
        this.folders = new ArrayList<>();
        this.listener = listener;
    }

    public void setFolders(List<FolderModel> folders) {
        this.folders = folders;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_folder, parent, false);
        return new FolderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        FolderModel folder = folders.get(position);
        holder.tvName.setText(folder.getName());
        holder.tvCount.setText(folder.getVideoCount() + " videos");
        holder.imgIcon.setImageResource(folder.getIconRes());

        if (!folder.getVideos().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                .load(folder.getVideos().get(0).getThumbnail())
                .placeholder(folder.getIconRes())
                .into(holder.imgIcon);
        }

        holder.cardView.setOnClickListener(v -> {
            if (listener != null) listener.onFolderClick(folder);
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        ImageView imgIcon;
        TextView tvName, tvCount;

        FolderViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.card_recent);
            imgIcon = itemView.findViewById(R.id.img_folder_icon);
            tvName = itemView.findViewById(R.id.tv_folder_name);
            tvCount = itemView.findViewById(R.id.tv_video_count);
        }
    }
}
