package com.obaidi.it_487_project_3;

import android.app.Application;
import androidx.lifecycle.LiveData;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NoteRepository {

    private NoteDao noteDao;
    private LiveData<List<Note>> allNotes;
    private ExecutorService executorService;

    NoteRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        noteDao = db.noteDao();
        allNotes = noteDao.getAllNotes();
        // Use a single thread executor for database writes/deletes to ensure order if needed
        executorService = Executors.newSingleThreadExecutor();
    }

    LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    void insert(Note note) {
        // Automatically sets timestamp in Note constructor now
        executorService.execute(() -> noteDao.insert(note));
    }

    // Add delete method
    void delete(Note note) {
        executorService.execute(() -> noteDao.delete(note));
    }

    void deleteAll() {
        executorService.execute(noteDao::deleteAll); // Use method reference
    }

    // Add update method
    void update(Note note) {
        // Update the timestamp when the note is updated
        note.setTimestamp(System.currentTimeMillis());
        executorService.execute(() -> noteDao.update(note));
    }

}