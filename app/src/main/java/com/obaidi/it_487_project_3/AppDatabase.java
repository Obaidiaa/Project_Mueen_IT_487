package com.obaidi.it_487_project_3;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

// Increment version number if schema changes
@Database(entities = {Note.class, Reminder.class}, version = 2, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    public abstract NoteDao noteDao();
    public abstract ReminderDao reminderDao(); // Add ReminderDao getter

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "IT487_database") // Renamed DB for clarity
                            // Add migrations here for production
                            .fallbackToDestructiveMigration() // Use only during development!
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}