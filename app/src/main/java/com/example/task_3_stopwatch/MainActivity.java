package com.example.task_3_stopwatch;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView timeTextView;
    private Button startButton;
    private Button lapButton;
    private Button resetButton;
    private RecyclerView lapsRecyclerView;
    private Handler handler;
    private long startTime;
    private long elapsedTime;
    private boolean isRunning;
    private List<Long> lapTimes;
    private LapAdapter lapAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // Initialize views
        timeTextView = findViewById(R.id.timeTextView);
        startButton = findViewById(R.id.startButton);
        lapButton = findViewById(R.id.lapButton);
        resetButton = findViewById(R.id.resetButton);
        lapsRecyclerView = findViewById(R.id.lapsRecyclerView);

        // Initialize variables
        handler = new Handler(Looper.getMainLooper());
        lapTimes = new ArrayList<>();
        lapAdapter = new LapAdapter(lapTimes);

        // Setup RecyclerView
        lapsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        lapsRecyclerView.setAdapter(lapAdapter);

        // Set up window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Set up button click listeners
        startButton.setOnClickListener(v -> toggleStopwatch());
        lapButton.setOnClickListener(v -> recordLap());
        resetButton.setOnClickListener(v -> resetStopwatch());

        // Initialize button states
        lapButton.setEnabled(false);
    }

    private void toggleStopwatch() {
        if (!isRunning) {
            startStopwatch();
            startButton.setText("Stop");
            lapButton.setEnabled(true);
        } else {
            stopStopwatch();
            startButton.setText("Start");
        }
    }

    private void startStopwatch() {
        if (!isRunning) {
            isRunning = true;
            startTime = System.currentTimeMillis() - elapsedTime;
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (isRunning) {
                        elapsedTime = System.currentTimeMillis() - startTime;
                        updateTimeDisplay();
                        handler.postDelayed(this, 10); // Update every 10ms
                    }
                }
            });
        }
    }

    private void stopStopwatch() {
        isRunning = false;
    }

    private void resetStopwatch() {
        isRunning = false;
        elapsedTime = 0;
        lapTimes.clear();
        lapAdapter.notifyDataSetChanged();
        updateTimeDisplay();
        startButton.setText("Start");
        lapButton.setEnabled(false);
    }

    private void recordLap() {
        if (isRunning) {
            lapTimes.add(0, elapsedTime); // Add to beginning of list
            lapAdapter.notifyItemInserted(0);
            lapsRecyclerView.scrollToPosition(0);
        }
    }

    private void updateTimeDisplay() {
        long hours = elapsedTime / (1000 * 60 * 60);
        long minutes = (elapsedTime % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (elapsedTime % (1000 * 60)) / 1000;
        long milliseconds = elapsedTime % 1000;

        String timeString = String.format("%02d:%02d:%02d.%03d",
                hours, minutes, seconds, milliseconds);
        timeTextView.setText(timeString);
    }

    private static class LapAdapter extends RecyclerView.Adapter<LapAdapter.LapViewHolder> {
        private final List<Long> lapTimes;

        public LapAdapter(List<Long> lapTimes) {
            this.lapTimes = lapTimes;
        }

        @NonNull
        @Override
        public LapViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new LapViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.lap_item, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull LapViewHolder holder, int position) {
            long lapTime = lapTimes.get(position);
            holder.lapNumberText.setText(String.format("Lap %d", lapTimes.size() - position));
            
            long hours = lapTime / (1000 * 60 * 60);
            long minutes = (lapTime % (1000 * 60 * 60)) / (1000 * 60);
            long seconds = (lapTime % (1000 * 60)) / 1000;
            long milliseconds = lapTime % 1000;

            String timeString = String.format("%02d:%02d:%02d.%03d",
                    hours, minutes, seconds, milliseconds);
            holder.lapTimeText.setText(timeString);
        }

        @Override
        public int getItemCount() {
            return lapTimes.size();
        }

        static class LapViewHolder extends RecyclerView.ViewHolder {
            TextView lapNumberText;
            TextView lapTimeText;

            LapViewHolder(@NonNull android.view.View itemView) {
                super(itemView);
                lapNumberText = itemView.findViewById(R.id.lapNumberText);
                lapTimeText = itemView.findViewById(R.id.lapTimeText);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}