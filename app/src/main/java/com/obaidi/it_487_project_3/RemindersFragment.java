package com.obaidi.it_487_project_3;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;


public class RemindersFragment extends Fragment {


    private FloatingActionButton fabAddReminder;
    private RecyclerView recyclerView;
    private ReminderListAdapter adapter;
    private ReminderViewModel reminderViewModel;
    private LinearLayout emptyView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminders, container, false);

        recyclerView = view.findViewById(R.id.reminders_recyclerview);
        emptyView = view.findViewById(R.id.reminders_empty_view);
        fabAddReminder = view.findViewById(R.id.fab_add_reminder); // Find FAB

        // Setup RecyclerView
        adapter = new ReminderListAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Setup ViewModel
        reminderViewModel = new ViewModelProvider(this).get(ReminderViewModel.class);

        // Observe LiveData
        reminderViewModel.getAllReminders().observe(getViewLifecycleOwner(), reminders -> {
            adapter.submitList(reminders);
            // Toggle empty view
            if (reminders == null || reminders.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        // Setup FAB Listener
        fabAddReminder.setOnClickListener(v -> {
            AddReminderDialogFragment dialogFragment = new AddReminderDialogFragment();
            dialogFragment.show(getParentFragmentManager(), "AddReminderDialog");
        });

        // Setup Swipe-to-Delete
        setupSwipeToDelete();

        return view;
    }

    private void setupSwipeToDelete() {
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Reminder reminderToDelete = adapter.getReminderAt(position);

                // 1. Delete from ViewModel (which deletes from DB)
                reminderViewModel.delete(reminderToDelete);

                // 2. Cancel the associated AlarmManager alarm
                cancelAlarm(getContext(), reminderToDelete.getId());

                // 3. Show Snackbar with Undo
                Snackbar.make(recyclerView, R.string.snackbar_reminder_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_undo, v -> {
                            // Undo: Re-insert and Reschedule
                            long newId = reminderViewModel.insert(reminderToDelete); // Re-insert (might get a new ID if deleted fully)
                            if (newId != -1) {
                                // Reschedule alarm with potentially new ID if insertion happened again
                                // Or better, update the original reminder object with the new ID if possible
                                scheduleAlarm(getContext(), (int) newId, reminderToDelete.getTitle(), reminderToDelete.getDescription(), reminderToDelete.getTriggerTimeMillis());
                            } else {
                                // Handle re-insertion error if needed
                            }
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView);
    }

    // --- Static Alarm Scheduling/Cancelling Methods ---
    // Made static for potential use elsewhere, but Context is needed
    public static void scheduleAlarm(Context context, int reminderId, String title, String description, long triggerTimeMillis) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderBroadcast.class);
        intent.putExtra("reminderTitle", title); // Pass data needed by broadcast
        intent.putExtra("reminderDescription", description);
        intent.putExtra("reminderId", reminderId); // Pass ID if needed

        // Use reminderId as the request code for uniqueness
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reminderId, // UNIQUE request code per reminder
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT); // Update if exists

        if (alarmManager != null) {
            try {
                // Use setExactAndAllowWhileIdle for more reliability on newer Android versions
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                } else {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent);
                }
            } catch (SecurityException se) {
                // Handle potential lack of SCHEDULE_EXACT_ALARM permission on Android 12+
                // Maybe fall back to setWindow or inform the user
                Toast.makeText(context, "Permission needed to set exact alarms.", Toast.LENGTH_LONG).show();
                // Consider using AlarmManagerCompat.setExactAndAllowWhileIdle() from androidx.core
            }
        }
    }

    public static void cancelAlarm(Context context, int reminderId) {
        if (context == null) return;
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, ReminderBroadcast.class); // MUST be the same intent used for scheduling

        // Recreate the EXACT same PendingIntent used for scheduling
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                reminderId, // SAME UNIQUE request code
                intent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_NO_CREATE); // Use NO_CREATE to check existence

        if (alarmManager != null && pendingIntent != null) {
            alarmManager.cancel(pendingIntent);
            pendingIntent.cancel(); // Also cancel the PendingIntent itself
            // Log.d("RemindersFragment", "Cancelled alarm for ID: " + reminderId);
        } else {
            // Log.w("RemindersFragment", "Alarm cancellation failed for ID: " + reminderId + " - PendingIntent might not exist.");
        }
    }
}