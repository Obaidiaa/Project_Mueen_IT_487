package com.obaidi.it_487_project_3;


import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import java.util.List;

public class NotesViewModel extends AndroidViewModel {

    private NoteRepository repository;
    private final LiveData<List<Note>> allNotes;

    public NotesViewModel(Application application) {
        super(application);
        repository = new NoteRepository(application);
        allNotes = repository.getAllNotes();
    }

    public LiveData<List<Note>> getAllNotes() {
        return allNotes;
    }

    public void insert(Note note) {
        repository.insert(note);
    }

    // Add delete method
    public void delete(Note note) {
        repository.delete(note);
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    // Add update method
    public void update(Note note) {
        repository.update(note);
    }
}