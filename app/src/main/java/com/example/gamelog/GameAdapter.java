package com.example.gamelog;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private ArrayList<GameItem> gameList;
    private OnItemClickListener listener;
    private OnDeleteClickListener deleteListener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnDeleteClickListener {
        void onDeleteClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnDeleteClickListener(OnDeleteClickListener deleteListener) {
        this.deleteListener = deleteListener;
    }

    public GameAdapter(ArrayList<GameItem> gameList) {
        this.gameList = gameList;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game, parent, false);
        return new GameViewHolder(view, listener, deleteListener);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        GameItem currentItem = gameList.get(position);
        holder.title.setText(currentItem.getTitle());
        holder.notes.setText(currentItem.getNotes());
        
        // Show thumbnail if image exists
        if (currentItem.getImageUri() != null && !currentItem.getImageUri().isEmpty()) {
            holder.thumbnail.setImageURI(Uri.parse(currentItem.getImageUri()));
            holder.thumbnail.setColorFilter(null); // Remove tint for actual images
        } else {
            holder.thumbnail.setImageResource(R.drawable.ic_games);
            holder.thumbnail.setColorFilter(holder.itemView.getContext().getResources().getColor(R.color.text_disabled));
        }
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        TextView notes;
        ImageView thumbnail;
        ImageView deleteBtn;

        GameViewHolder(@NonNull View itemView, final OnItemClickListener listener, final OnDeleteClickListener deleteListener) {
            super(itemView);
            title = itemView.findViewById(R.id.game_title);
            notes = itemView.findViewById(R.id.game_notes);
            thumbnail = itemView.findViewById(R.id.game_thumbnail);
            deleteBtn = itemView.findViewById(R.id.delete_button);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onItemClick(position);
                    }
                }
            });

            deleteBtn.setOnClickListener(v -> {
                if (deleteListener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        deleteListener.onDeleteClick(position);
                    }
                }
            });
        }
    }
}
