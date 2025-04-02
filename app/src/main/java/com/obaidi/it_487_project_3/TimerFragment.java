package com.obaidi.it_487_project_3;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.CircularProgressIndicator;

import java.util.ArrayList; // Import ArrayList
import java.util.List;     // Import List

public class TimerFragment extends Fragment {

    // UI Elements
    private TextView textViewCountdown;
    private TextView textViewPhase;
    private MaterialButton buttonStartPause;
    private MaterialButton buttonResetSkip;
    private CircularProgressIndicator progressCircular;

    // ViewModel
    private TimerViewModel timerViewModel;
    private List<View> dotIndicators; // List to hold dot views

    // Notification Constants
    private static final String CHANNEL_ID = "notifyIT487Timer"; // Specific channel for timer
    private static final int NOTIFICATION_ID = 201; // Unique ID for timer notifications
    private static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 124; // Unique code


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timer, container, false);

        // Initialize UI Elements
        textViewCountdown = view.findViewById(R.id.text_view_countdown);
        textViewPhase = view.findViewById(R.id.text_view_phase);
        buttonStartPause = view.findViewById(R.id.button_start_pause);
        buttonResetSkip = view.findViewById(R.id.button_reset_skip);
        progressCircular = view.findViewById(R.id.progress_circular);

        // Initialize Dot Indicators
        dotIndicators = new ArrayList<>();
        dotIndicators.add(view.findViewById(R.id.dot1));
        dotIndicators.add(view.findViewById(R.id.dot2));
        dotIndicators.add(view.findViewById(R.id.dot3));
        dotIndicators.add(view.findViewById(R.id.dot4));

        // Create Notification Channel (do this early)
        createTimerNotificationChannel();

        // --- Get the ViewModel ---
        // Scope to the Activity to survive fragment replacement
        timerViewModel = new ViewModelProvider(requireActivity()).get(TimerViewModel.class);


        // --- Observe LiveData ---
        timerViewModel.timeLeftFormatted.observe(getViewLifecycleOwner(), formattedTime -> {
            if (textViewCountdown != null) {
                textViewCountdown.setText(formattedTime);
            }
        });
        timerViewModel.phaseTextResId.observe(getViewLifecycleOwner(), resId -> {
            if (textViewPhase != null && resId != null && getContext() != null) {
                textViewPhase.setText(getString(resId)); // Get string using Fragment's context
            }
        });
        Observer<Object> dotUpdateObserver = ignored -> updateDotIndicators();

        // Observe Notification Events
        timerViewModel.notificationEvent.observe(getViewLifecycleOwner(), event -> {
            TimerViewModel.NotificationInfo info = event.getContentIfNotHandled(); // Consume the event
            if (info != null && getContext() != null) {
                // Construct the localized string HERE in the Fragment
                String completedPhaseName = getString(info.completedPhaseResId);
                String nextPhaseName = getString(info.nextPhaseResId);
                String message = getString(info.formatResId, completedPhaseName, nextPhaseName); // Format the string

                // Show notification with the constructed message
                checkAndShowNotification(message);
            }
        });

        timerViewModel.phaseText.observe(getViewLifecycleOwner(), phase -> {
            if (textViewPhase != null) {
                textViewPhase.setText(phase);
            }
        });

        timerViewModel.timerRunning.observe(getViewLifecycleOwner(), isRunning -> {
            updateButtons(isRunning); // Update buttons based on running state
        });

        timerViewModel.timeLeftInMillis.observe(getViewLifecycleOwner(), timeLeft -> {
            updateProgressBar(); // Update progress bar when time changes
        });

        timerViewModel.totalTimeForPhase.observe(getViewLifecycleOwner(), totalTime -> {
            updateProgressBar(); // Update progress bar max when total time changes
        });

        // Observe current phase for dot updates
        timerViewModel.currentPhase.observe(getViewLifecycleOwner(), dotUpdateObserver);

        // Observe Pomodoro Count for dot updates
        timerViewModel.pomodoroCount.observe(getViewLifecycleOwner(), dotUpdateObserver);


        // --- Setup Button Listeners ---
        buttonStartPause.setOnClickListener(v -> timerViewModel.toggleTimer());
        buttonResetSkip.setOnClickListener(v -> {
            // Check running state from ViewModel to decide action
            if (Boolean.TRUE.equals(timerViewModel.timerRunning.getValue())) {
                timerViewModel.skipPhase();
                Toast.makeText(getContext(), "Skipped", Toast.LENGTH_SHORT).show();
            } else {
                timerViewModel.resetTimer();
                Toast.makeText(getContext(), "Reset", Toast.LENGTH_SHORT).show();
            }
        });

        // Initial UI state setup based on ViewModel current values
        updateButtons(Boolean.TRUE.equals(timerViewModel.timerRunning.getValue()));
        updateProgressBar();
        updateDotIndicators(); // Initial dot state based on current ViewModel values

        return view;
    }


    // Updated method to handle phase-specific dot colors
    private void updateDotIndicators() {
        if (dotIndicators == null || getContext() == null || timerViewModel == null) return;

        TimerViewModel.PomodoroPhase phase = timerViewModel.currentPhase.getValue();
        Integer count = timerViewModel.pomodoroCount.getValue();

        int currentCount = (count != null) ? count : 0;
        if (phase == null) phase = TimerViewModel.PomodoroPhase.WORKING; // Default

        int activeDrawableId;
        int inactiveDrawableId = R.drawable.dot_indicator_inactive;
        boolean highlightAllDots = false; // Flag for long break

        // Determine the drawable for active dots based on the current phase
        switch (phase) {
            case WORKING:
                activeDrawableId = R.drawable.dot_indicator_active; // Primary color
                break;
            case SHORT_BREAK:
                activeDrawableId = R.drawable.dot_indicator_short_break; // Secondary color
                break;
            case LONG_BREAK:
                activeDrawableId = R.drawable.dot_indicator_long_break; // Tertiary color
                highlightAllDots = true; // Special case for long break: all dots use the active color
                break;
            default:
                activeDrawableId = R.drawable.dot_indicator_inactive; // Should not happen
        }

        // Number of dots to potentially activate based on completed work sessions
        // During breaks, this still reflects the work sessions *before* the break started
        int activeDotsToShow = Math.max(0, Math.min(currentCount, dotIndicators.size()));

        for (int i = 0; i < dotIndicators.size(); i++) {
            View dot = dotIndicators.get(i);
            if (highlightAllDots) {
                // Long break: All dots get the long break color
                dot.setBackground(ContextCompat.getDrawable(getContext(), activeDrawableId));
            } else if (i < activeDotsToShow) {
                // Work or Short Break: Activate dots up to the completed count
                dot.setBackground(ContextCompat.getDrawable(getContext(), activeDrawableId));
            } else {
                // Inactive dots
                dot.setBackground(ContextCompat.getDrawable(getContext(), inactiveDrawableId));
            }
        }
    }


    private void updateProgressBar() {
        Long timeLeft = timerViewModel.timeLeftInMillis.getValue();
        Long totalTime = timerViewModel.totalTimeForPhase.getValue();

        if (progressCircular != null && timeLeft != null && totalTime != null && totalTime > 0) {
            progressCircular.setMax(totalTime.intValue()); // Use milliseconds for max
            progressCircular.setProgress(timeLeft.intValue(), true); // Use milliseconds for progress
        } else if (progressCircular != null) {
            progressCircular.setMax(100); // Default max
            progressCircular.setProgress(0, true); // Default progress
        }
    }

    private void updateButtons(boolean isRunning) {
        if (buttonStartPause == null || buttonResetSkip == null || getContext() == null) return; // Null checks

        if (isRunning) {
            buttonStartPause.setText(R.string.button_pause);
            buttonStartPause.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_pause_24));
            buttonResetSkip.setText(R.string.button_skip);
            buttonResetSkip.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_skip_next_24));
        } else {
            buttonStartPause.setText(R.string.button_start);
            buttonStartPause.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_play_arrow_24));
            buttonResetSkip.setText(R.string.button_reset);
            buttonResetSkip.setIcon(ContextCompat.getDrawable(getContext(), R.drawable.ic_baseline_replay_24));
        }
    }

    // No need for most of the timer logic methods (startTimer, pauseTimer, etc.) here anymore
    // They are now in the ViewModel
    private void checkAndShowNotification(String message) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13+
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // Request permission
                ActivityCompat.requestPermissions(requireActivity(), new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
                // Optionally store the message to show after permission granted, or show a Toast for now
                Toast.makeText(getContext(), "Notification permission needed", Toast.LENGTH_SHORT).show();
            } else {
                // Permission already granted
                showNotification(message);
            }
        } else {
            // No runtime permission needed for older versions
            showNotification(message);
        }
    }

    // Handle permission request result (add this method to the Fragment)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == NOTIFICATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, you might want to reshow the notification if you stored the message
                Toast.makeText(getContext(), "Notification permission granted!", Toast.LENGTH_SHORT).show();
                // Example: showNotification(storedMessage); // if you stored it
            } else {
                Toast.makeText(getContext(), "Notification permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void createTimerNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Pomodoro Timer Notifications";
            String description = "Notifications for Pomodoro phase changes";
            int importance = NotificationManager.IMPORTANCE_HIGH; // Use High for timer alerts
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system
            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    private void showNotification(String message) {
        Context context = getContext();
        if (context == null) return; // Avoid issues if context is unavailable

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_baseline_timer_24) // Use a timer icon
                .setContentTitle(getString(R.string.app_name))
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // High priority for timers
                .setAutoCancel(true); // Dismiss notification when tapped (optional)

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    @Override
    public void onDestroyView() {
        // No need to cancel timer here, ViewModel handles it in onCleared()
        super.onDestroyView();
        dotIndicators = null; // Clear references to views

    }
}