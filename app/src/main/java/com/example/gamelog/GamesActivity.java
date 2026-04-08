package com.example.gamelog;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class GamesActivity extends AppCompatActivity {

    private ArrayList<GameItem> gameList = new ArrayList<>();
    private GameAdapter adapter;
    private TextView totalGames;
    private LinearLayout emptyState;

    // Location & Image state for Dialog
    private FusedLocationProviderClient fusedLocationClient;
    private String currentFetchedLocation = "";
    private Uri selectedImageUri;
    private ImageView dialogImagePreview;
    private TextView dialogLocationText;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private static final int PICK_IMAGE_REQUEST = 101;
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_games);

        totalGames = findViewById(R.id.total_games);
        emptyState = findViewById(R.id.empty_state);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        RecyclerView recyclerView = findViewById(R.id.games_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new GameAdapter(gameList);
        recyclerView.setAdapter(adapter);

        FloatingActionButton fab = findViewById(R.id.add_game_fab);
        fab.setOnClickListener(v -> showAddGameDialog());

        // Interaction: Tap to view details
        adapter.setOnItemClickListener(this::showDetailDialog);

        // Interaction: Delete with confirmation
        adapter.setOnDeleteClickListener(this::showDeleteConfirmation);

        updateUI();
    }

    private void showAddGameDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_game, null);
        builder.setView(view);

        EditText titleInput = view.findViewById(R.id.dialog_game_title_input);
        EditText notesInput = view.findViewById(R.id.dialog_game_notes_input);
        dialogLocationText = view.findViewById(R.id.dialog_location_text);
        dialogImagePreview = view.findViewById(R.id.dialog_image_preview);
        Button locationBtn = view.findViewById(R.id.dialog_get_location_button);
        Button imageBtn = view.findViewById(R.id.dialog_choose_image_button);
        Button saveBtn = view.findViewById(R.id.dialog_save_button);
        Button cancelBtn = view.findViewById(R.id.dialog_cancel_button);

        MaterialButtonToggleGroup modeToggle = view.findViewById(R.id.dialog_mode_toggle_group);
        LinearLayout reminderOptionsContainer = view.findViewById(R.id.reminder_options_container);
        ChipGroup frequencyChips = view.findViewById(R.id.reminder_frequency_chips);

        AlertDialog dialog = builder.create();

        // Toggle between Note and Reminder modes
        modeToggle.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                if (checkedId == R.id.btn_mode_reminder) {
                    reminderOptionsContainer.setVisibility(View.VISIBLE);
                } else {
                    reminderOptionsContainer.setVisibility(View.GONE);
                }
            }
        });

        locationBtn.setOnClickListener(v -> checkLocationPermissions());
        imageBtn.setOnClickListener(v -> openGallery());

        cancelBtn.setOnClickListener(v -> {
            resetDialogState();
            dialog.dismiss();
        });

        saveBtn.setOnClickListener(v -> {
            int checkedModeId = modeToggle.getCheckedButtonId();

            if (checkedModeId == R.id.btn_mode_reminder) {
                // Reminder mode
                int checkedChipId = frequencyChips.getCheckedChipId();
                if (checkedChipId == View.NO_ID) {
                    Toast.makeText(this, "Please select a reminder frequency", Toast.LENGTH_SHORT).show();
                    return;
                }
                long intervalMinutes = getIntervalMinutes(checkedChipId);
                scheduleReminder(intervalMinutes);
                resetDialogState();
                dialog.dismiss();
                Toast.makeText(this, "Reminder set!", Toast.LENGTH_SHORT).show();
            } else {
                // Note mode — existing behaviour
                String title = titleInput.getText().toString();
                if (title.isEmpty()) {
                    Toast.makeText(this, "Title is required", Toast.LENGTH_SHORT).show();
                    return;
                }
                String notes = notesInput.getText().toString();
                String imgUri = (selectedImageUri != null) ? selectedImageUri.toString() : null;
                gameList.add(new GameItem(title, notes, "Tracked", imgUri, currentFetchedLocation));
                adapter.notifyItemInserted(gameList.size() - 1);
                updateUI();
                resetDialogState();
                dialog.dismiss();
                Toast.makeText(this, "Game added to library", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private long getIntervalMinutes(int chipId) {
        if (chipId == R.id.chip_5min)  return 5;
        if (chipId == R.id.chip_20min) return 20;
        if (chipId == R.id.chip_1hour) return 60;
        if (chipId == R.id.chip_6hour) return 360;
        if (chipId == R.id.chip_1day)  return 1440;
        return 60; // fallback
    }

    private void scheduleReminder(long intervalMinutes) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.POST_NOTIFICATIONS},
                        NOTIFICATION_PERMISSION_REQUEST_CODE);
                // Permission may be granted async; schedule anyway — WorkManager will fire when ready
            }
        }

        PeriodicWorkRequest reminderRequest = new PeriodicWorkRequest.Builder(
                ReminderWorker.class, intervalMinutes, TimeUnit.MINUTES)
                .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "game_reminder",
                ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest
        );
    }

    private void showDetailDialog(int position) {
        GameItem item = gameList.get(position);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        
        // Dynamic detail view
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_add_game, null); // Reusing layout for simplicity or use a dedicated one
        builder.setView(view);

        // Modify dialog for "Detail Mode"
        EditText title = view.findViewById(R.id.dialog_game_title_input);
        EditText notes = view.findViewById(R.id.dialog_game_notes_input);
        TextView locText = view.findViewById(R.id.dialog_location_text);
        ImageView imgPreview = view.findViewById(R.id.dialog_image_preview);
        
        title.setText(item.getTitle());
        title.setEnabled(false);
        notes.setText(item.getNotes());
        notes.setEnabled(false);
        
        locText.setText("Location: " + (item.getLocation().isEmpty() ? "Not set" : item.getLocation()));
        
        if (item.getImageUri() != null) {
            imgPreview.setImageURI(Uri.parse(item.getImageUri()));
            imgPreview.setVisibility(View.VISIBLE);
        }

        view.findViewById(R.id.dialog_get_location_button).setVisibility(View.GONE);
        view.findViewById(R.id.dialog_choose_image_button).setVisibility(View.GONE);
        view.findViewById(R.id.dialog_save_button).setVisibility(View.GONE);
        view.findViewById(R.id.dialog_mode_toggle_group).setVisibility(View.GONE);
        view.findViewById(R.id.reminder_options_container).setVisibility(View.GONE);
        ((Button)view.findViewById(R.id.dialog_cancel_button)).setText("CLOSE");
        
        AlertDialog dialog = builder.create();
        view.findViewById(R.id.dialog_cancel_button).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private void showDeleteConfirmation(int position) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Game")
                .setMessage("Are you sure you want to remove this from your library?")
                .setPositiveButton("Remove", (dialog, which) -> {
                    gameList.remove(position);
                    adapter.notifyItemRemoved(position);
                    updateUI();
                    Toast.makeText(this, "Game removed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetDialogState() {
        selectedImageUri = null;
        currentFetchedLocation = "";
    }

    private void updateUI() {
        totalGames.setText(gameList.size() + " Titles Tracked");
        emptyState.setVisibility(gameList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    // Permission and Gallery logic (Keep same as before but update dialog UI)
    private void checkLocationPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLastLocation();
        }
    }

    private void getLastLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, location -> {
                if (location != null) {
                    currentFetchedLocation = location.getLatitude() + ", " + location.getLongitude();
                    if (dialogLocationText != null) dialogLocationText.setText("Location: Set");
                    Toast.makeText(this, "Location detected", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) { e.printStackTrace(); }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (dialogImagePreview != null) {
                dialogImagePreview.setImageURI(selectedImageUri);
                dialogImagePreview.setVisibility(View.VISIBLE);
            }
        }
    }
}
