package com.obaidi.it_487_project_3;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ReminderDao {

    // Use Long for insert return type to get the generated ID
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(Reminder reminder); // Returns the row ID of the inserted item

    @Update
    void update(Reminder reminder);

    @Delete
    void delete(Reminder reminder);

    @Query("SELECT * FROM reminders ORDER BY trigger_time_millis ASC") // Show earliest first
    LiveData<List<Reminder>> getAllReminders();

    @Query("SELECT * FROM reminders WHERE id = :id")
    Reminder getReminderById(int id); // Needed for potential background tasks
}