package com.obaidi.it_487_project_3;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "notes")
public class Note {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "note_text")
    public String noteText;

    @ColumnInfo(name = "timestamp") // New column for timestamp
    public long timestamp;         // Store as milliseconds

    // Update constructor to initialize timestamp
    public Note(String noteText) {
        this.noteText = noteText;
        this.timestamp = System.currentTimeMillis(); // Set timestamp on creation
    }

    // Getters and Setters (keep existing ones, add for timestamp)
    public String getNoteText() { return noteText;}
    public int getId() { return id;}
    public long getTimestamp() { return timestamp; } // Getter for timestamp

    public void setNoteText(String noteText) { this.noteText = noteText;}
    public void setId(int id) {this.id = id;}
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; } // Setter for timestamp
}