package com.obaidi.it_487_project_3;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "reminders")
public class Reminder {
    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "trigger_time_millis")
    public long triggerTimeMillis; // Time when the alarm should fire

    // Constructor
    public Reminder(String title, String description, long triggerTimeMillis) {
        this.title = title;
        this.description = description;
        this.triggerTimeMillis = triggerTimeMillis;
    }

    // Getters (needed for DiffUtil and potentially other logic)
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public long getTriggerTimeMillis() { return triggerTimeMillis; }

    // Setters (optional, but useful)
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setTriggerTimeMillis(long triggerTimeMillis) { this.triggerTimeMillis = triggerTimeMillis; }
}