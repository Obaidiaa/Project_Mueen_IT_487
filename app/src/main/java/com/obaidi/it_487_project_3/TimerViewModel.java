package com.obaidi.it_487_project_3;

import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.StringRes;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class TimerViewModel extends ViewModel {

    // Pomodoro States and Durations (Copied from Fragment)
    public enum PomodoroPhase { // Make public or provide getter
        WORKING,
        SHORT_BREAK,
        LONG_BREAK
    }

//    private static final long WORK_DURATION_MS = TimeUnit.MINUTES.toMillis(1); // 1 min FOR TESTING
//    private static final long SHORT_BREAK_DURATION_MS = TimeUnit.SECONDS.toMillis(10); // 10 sec FOR TESTING
//    private static final long LONG_BREAK_DURATION_MS = TimeUnit.SECONDS.toMillis(20); // 20 sec FOR TESTING
     private static final long WORK_DURATION_MS = TimeUnit.MINUTES.toMillis(25);
     private static final long SHORT_BREAK_DURATION_MS = TimeUnit.MINUTES.toMillis(5);
     private static final long LONG_BREAK_DURATION_MS = TimeUnit.MINUTES.toMillis(15);
    private static final int LONG_BREAK_INTERVAL = 4;

    // LiveData for UI State
    private final MutableLiveData<Long> _timeLeftInMillis = new MutableLiveData<>();
    public LiveData<Long> timeLeftInMillis = _timeLeftInMillis;

    private final MutableLiveData<Boolean> _timerRunning = new MutableLiveData<>(false);
    public LiveData<Boolean> timerRunning = _timerRunning;

    private final MutableLiveData<PomodoroPhase> _currentPhase = new MutableLiveData<>(PomodoroPhase.WORKING);
    public LiveData<PomodoroPhase> currentPhase = _currentPhase;

    private final MutableLiveData<Integer> _pomodoroCount = new MutableLiveData<>(0);
    public LiveData<Integer> pomodoroCount = _pomodoroCount; // Optional: If UI needs count

    private final MutableLiveData<Long> _totalTimeForPhase = new MutableLiveData<>();
    public LiveData<Long> totalTimeForPhase = _totalTimeForPhase;

    private final MutableLiveData<String> _phaseText = new MutableLiveData<>();
    public LiveData<String> phaseText = _phaseText;

    private final MutableLiveData<String> _timeLeftFormatted = new MutableLiveData<>();
    public LiveData<String> timeLeftFormatted = _timeLeftFormatted;

    private final MutableLiveData<Integer> _phaseTextResId = new MutableLiveData<>();
    public LiveData<Integer> phaseTextResId = _phaseTextResId; // Expose Integer LiveData
    private CountDownTimer countDownTimer;
    private final MutableLiveData<Event<NotificationInfo>> _notificationEvent = new MutableLiveData<>();
    public LiveData<Event<NotificationInfo>> notificationEvent = _notificationEvent;
    public TimerViewModel() {
        // Initial setup when ViewModel is first created
        resetTimerInternal();
    }

    public void toggleTimer() {
        if (Boolean.TRUE.equals(_timerRunning.getValue())) {
            pauseTimerInternal();
        } else {
            startTimerInternal();
        }
    }

    public void resetTimer() {
        resetTimerInternal();
    }

    public void skipPhase() {
        skipPhaseInternal();
    }


    // Helper class to hold notification info (Resource IDs)
    public static class NotificationInfo {
        @StringRes public final int formatResId;
        @StringRes public final int completedPhaseResId;
        @StringRes public final int nextPhaseResId;

        public NotificationInfo(@StringRes int formatResId, @StringRes int completedPhaseResId, @StringRes int nextPhaseResId) {
            this.formatResId = formatResId;
            this.completedPhaseResId = completedPhaseResId;
            this.nextPhaseResId = nextPhaseResId;
        }
    }

    private void startTimerInternal() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        Long timeLeft = _timeLeftInMillis.getValue();
        if (timeLeft == null || timeLeft <= 0) {
            // If time is up or invalid, reset to current phase duration
            timeLeft = getCurrentPhaseDuration();
            _timeLeftInMillis.setValue(timeLeft);
        }
        _totalTimeForPhase.setValue(getCurrentPhaseDuration()); // Ensure total time is set
        _timerRunning.setValue(true);

        countDownTimer = new CountDownTimer(timeLeft, 50) {
            @Override
            public void onTick(long millisUntilFinished) {
                _timeLeftInMillis.setValue(millisUntilFinished);
                updateFormattedTime(millisUntilFinished);
            }

            @Override
            public void onFinish() {
                _timerRunning.setValue(false);
                // Use Handler to ensure phase change happens on main thread if needed,
                // and slightly delays to avoid race conditions with UI updates
                new Handler(Looper.getMainLooper()).postDelayed(() -> handlePhaseCompletionInternal(), 100);
            }
        }.start();
    }

    private void pauseTimerInternal() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        _timerRunning.setValue(false);
    }

    private void resetTimerInternal() {
        pauseTimerInternal(); // Stop any running timer
        _pomodoroCount.setValue(0);
        _currentPhase.setValue(PomodoroPhase.WORKING);
        long duration = getCurrentPhaseDuration();
        _totalTimeForPhase.setValue(duration);
        _timeLeftInMillis.setValue(duration);
        updatePhaseTextResId();
        updateFormattedTime(duration);
    }

    private void skipPhaseInternal() {
        pauseTimerInternal(); // Stop current timer
        postNotificationEvent(_currentPhase.getValue(), true); // Indicate skip
        handlePhaseCompletionInternal(); // Move to next phase
    }

    private void handlePhaseCompletionInternal() {
        PomodoroPhase previousPhase = _currentPhase.getValue();
        PomodoroPhase current = _currentPhase.getValue();
        int count = _pomodoroCount.getValue() != null ? _pomodoroCount.getValue() : 0;
        PomodoroPhase nextPhase;

        if (current == PomodoroPhase.WORKING) {
            count++;
            _pomodoroCount.setValue(count);
            if (count % LONG_BREAK_INTERVAL == 0 && count > 0) {
                nextPhase = PomodoroPhase.LONG_BREAK;
            } else {
                nextPhase = PomodoroPhase.SHORT_BREAK;
            }
        } else {
            nextPhase = PomodoroPhase.WORKING;
        }

        _currentPhase.setValue(nextPhase);
        postNotificationEvent(previousPhase, false); // Post notification based on phase change

        long duration = getCurrentPhaseDuration();
        _totalTimeForPhase.setValue(duration);
        _timeLeftInMillis.setValue(duration);
        updatePhaseTextResId(); // Update the resource ID
        updateFormattedTime(duration);
    }

    // Notification message generation needs context, ideally done in Fragment,
    // but since it's simple text, generating here is acceptable for now.
    // If it needed plural strings etc., it would *have* to move to Fragment.
    @StringRes
    private int getPhaseNameResId(PomodoroPhase phase) {
        if (phase == null) return R.string.timer_phase_focus; // Default safety
        switch (phase) {
            case WORKING: return R.string.timer_phase_focus;
            case SHORT_BREAK: return R.string.timer_phase_short_break;
            case LONG_BREAK: return R.string.timer_phase_long_break;
            default: return R.string.timer_phase_focus;
        }
    }

    // Rewritten method posts NotificationInfo object
    private void postNotificationEvent(PomodoroPhase completedPhase, boolean skipped) {
        PomodoroPhase nextPhase = _currentPhase.getValue();
        if (completedPhase == null || nextPhase == null) return;

        // Get Resource IDs for the phase names
        @StringRes int completedPhaseNameResId = getPhaseNameResId(completedPhase);
        @StringRes int nextPhaseNameResId = getPhaseNameResId(nextPhase);

        // Determine which format string Resource ID to use
        @StringRes int formatStringResId = skipped ?
                R.string.notification_timer_phase_skipped :
                R.string.notification_timer_phase_finished;

        // Create the info object
        NotificationInfo info = new NotificationInfo(formatStringResId, completedPhaseNameResId, nextPhaseNameResId);

        // Post the event
        _notificationEvent.setValue(new Event<>(info));
    }

    private long getCurrentPhaseDuration() {
        PomodoroPhase phase = _currentPhase.getValue();
        if (phase == null) phase = PomodoroPhase.WORKING; // Default
        switch (phase) {
            case WORKING: return WORK_DURATION_MS;
            case SHORT_BREAK: return SHORT_BREAK_DURATION_MS;
            case LONG_BREAK: return LONG_BREAK_DURATION_MS;
            default: return WORK_DURATION_MS;
        }
    }

    private void updatePhaseTextResId() {
        PomodoroPhase phase = _currentPhase.getValue();
        if (phase == null) phase = PomodoroPhase.WORKING;
        @StringRes int resId; // Use annotation for clarity
        switch (phase) {
            case WORKING:
                resId = R.string.timer_phase_focus;
                break;
            case SHORT_BREAK:
                resId = R.string.timer_phase_short_break;
                break;
            case LONG_BREAK:
                resId = R.string.timer_phase_long_break;
                break;
            default:
                resId = R.string.timer_phase_focus; // Default
        }
        _phaseTextResId.setValue(resId); // Set the resource ID
    }

    private void updateFormattedTime(long millis) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);
        String timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        _timeLeftFormatted.setValue(timeLeftFormatted);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        // Cancel the timer when the ViewModel is destroyed
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
    }
}