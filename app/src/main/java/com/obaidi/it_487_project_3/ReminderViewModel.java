package com.obaidi.it_487_project_3;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class ReminderViewModel extends AndroidViewModel {

    private ReminderRepository repository;
    private final LiveData<List<Reminder>> allReminders;

    public ReminderViewModel(Application application) {
        super(application);
        repository = new ReminderRepository(application);
        allReminders = repository.getAllReminders();
    }

    public LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    // Insert and return the generated ID (blocks caller, use carefully or with callbacks)
    public long insert(Reminder reminder) {
        Future<Long> future = repository.insert(reminder);
        try {
            // This blocks the thread until the insertion is complete.
            // Consider using LiveData or Callbacks for a fully async approach if needed.
            return future.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace(); // Handle error appropriately
            return -1; // Indicate failure
        }
    }

    public void update(Reminder reminder) {
        repository.update(reminder);
    }

    public void delete(Reminder reminder) {
        repository.delete(reminder);
    }
}