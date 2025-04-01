package com.obaidi.it_487_project_3;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete; // Import Delete
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update; // Import Update

import java.util.List;


@Dao
public interface NoteDao {

    @Query("SELECT * FROM notes ORDER BY timestamp DESC") // Order by newest first
    LiveData<List<Note>> getAllNotes();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Note note);

    @Delete // Add the Delete annotation
    void delete(Note note); // Method to delete a specific note

    @Query("DELETE FROM notes")
    void deleteAll();

    @Update // Add the Update annotation
    void update(Note note); // Method to update an existing note
}