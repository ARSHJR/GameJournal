package com.example.gamelog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * Adapter for displaying games from the API in a RecyclerView
 */
public class GameApiAdapter extends RecyclerView.Adapter<GameApiAdapter.GameViewHolder> {

    private List<GameApiItem> gamesList;

    public GameApiAdapter(List<GameApiItem> gamesList) {
        this.gamesList = gamesList;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_game_api, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        // Bind data to the views
        GameApiItem game = gamesList.get(position);
        holder.titleText.setText(game.getTitle());
        holder.genreText.setText("Genre: " + game.getGenre());
        holder.releaseYearText.setText("Released: " + game.getReleaseYear());
    }

    @Override
    public int getItemCount() {
        return gamesList != null ? gamesList.size() : 0;
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView titleText, genreText, releaseYearText;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            titleText = itemView.findViewById(R.id.text_title);
            genreText = itemView.findViewById(R.id.text_genre);
            releaseYearText = itemView.findViewById(R.id.text_release_year);
        }
    }
}
