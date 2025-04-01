package com.obaidi.it_487_project_3;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TimePicker;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.textfield.TextInputEditText;
import java.text.DateFormat;
import java.util.Calendar;

public class AddReminderDialogFragment extends DialogFragment {

    private TextInputEditText editTextTitle;
    private TextInputEditText editTextDescription;
    private TextInputEditText editTextDate;
    private ReminderViewModel reminderViewModel;
    private Calendar selectedDateTime;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_reminder, null);

        editTextTitle = dialogView.findViewById(R.id.dialog_edit_text_reminder_title);
        editTextDescription = dialogView.findViewById(R.id.dialog_edit_text_reminder_description);
        editTextDate = dialogView.findViewById(R.id.dialog_edit_text_reminder_date);

        selectedDateTime = Calendar.getInstance(); // Initialize calendar

        // Get ViewModel scoped to the parent Fragment/Activity
        reminderViewModel = new ViewModelProvider(requireActivity()).get(ReminderViewModel.class);

        editTextDate.setOnClickListener(v -> showDateTimePicker());

        builder.setView(dialogView)
                .setTitle(R.string.dialog_title_add_reminder) // Add string resource
                .setPositiveButton(R.string.dialog_add, (dialog, id) -> {
                    saveReminder();
                    // Dialog dismisses automatically
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, id) -> {
                    AddReminderDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }

    private void showDateTimePicker() {
        final Calendar currentDate = Calendar.getInstance();
        // Prevent selecting past dates
        DatePickerDialog datePickerDialog = new DatePickerDialog(requireContext(),
                (view, year, month, dayOfMonth) -> {
                    selectedDateTime.set(year, month, dayOfMonth);
                    showTimePicker(); // Chain the time picker
                }, currentDate.get(Calendar.YEAR), currentDate.get(Calendar.MONTH), currentDate.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.getDatePicker().setMinDate(System.currentTimeMillis() - 1000); // Set min date to now
        datePickerDialog.show();
    }

    private void showTimePicker() {
        final Calendar currentTime = Calendar.getInstance();
        new TimePickerDialog(requireContext(),
                (view, hourOfDay, minute) -> {
                    selectedDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    selectedDateTime.set(Calendar.MINUTE, minute);
                    selectedDateTime.set(Calendar.SECOND, 0);
                    selectedDateTime.set(Calendar.MILLISECOND, 0);
                    updateReminderDateTextField();
                }, currentTime.get(Calendar.HOUR_OF_DAY), currentTime.get(Calendar.MINUTE), false).show();
    }

    private void updateReminderDateTextField() {
        String dateTimeString = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                .format(selectedDateTime.getTime());
        editTextDate.setText(dateTimeString);
    }

    private void saveReminder() {
        String title = editTextTitle.getText().toString().trim();
        String description = editTextDescription.getText().toString().trim();
        String dateText = editTextDate.getText().toString();
        long triggerTime = selectedDateTime.getTimeInMillis();

        // Validation
        if (title.isEmpty()) {
            Toast.makeText(getContext(), R.string.toast_enter_reminder_title, Toast.LENGTH_SHORT).show();
            // How to prevent closing? More complex, requires overriding button listener
            return;
        }
        if (dateText.isEmpty()) {
            Toast.makeText(getContext(), R.string.toast_select_date_time, Toast.LENGTH_SHORT).show();
            return;
        }
        if (triggerTime <= System.currentTimeMillis()) {
            Toast.makeText(getContext(), R.string.toast_select_future_time, Toast.LENGTH_SHORT).show();
            return;
        }

        Reminder newReminder = new Reminder(title, description, triggerTime);
        long reminderId = reminderViewModel.insert(newReminder);

        if (reminderId != -1) {
            RemindersFragment.scheduleAlarm(getContext(), (int) reminderId, title, description, triggerTime); // Call static method
            Toast.makeText(getContext(), R.string.toast_reminder_set, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), R.string.error_saving_reminder, Toast.LENGTH_SHORT).show();
        }
    }
}
