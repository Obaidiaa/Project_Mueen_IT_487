package com.obaidi.it_487_project_3;


import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future; // To get result from insert

public class ReminderRepository {

    private ReminderDao reminderDao;
    private LiveData<List<Reminder>> allReminders;
    private ExecutorService executorService;

    ReminderRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        reminderDao = db.reminderDao();
        allReminders = reminderDao.getAllReminders();
        executorService = Executors.newSingleThreadExecutor();
    }

    LiveData<List<Reminder>> getAllReminders() {
        return allReminders;
    }

    // Return Future<Long> to get the ID after insertion
    Future<Long> insert(Reminder reminder) {
        return executorService.submit(() -> reminderDao.insert(reminder));
    }

    void update(Reminder reminder) {
        executorService.execute(() -> reminderDao.update(reminder));
    }

    void delete(Reminder reminder) {
        executorService.execute(() -> reminderDao.delete(reminder));
    }
}