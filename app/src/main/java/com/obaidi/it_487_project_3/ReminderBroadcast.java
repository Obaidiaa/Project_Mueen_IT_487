package com.obaidi.it_487_project_3;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

public class ReminderBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        String reminderTitle = intent.getStringExtra("reminderTitle");
        String reminderDescription = intent.getStringExtra("reminderDescription");
        int reminderId = intent.getIntExtra("reminderId", -1); // Get ID if needed

        // Use a unique notification ID based on the reminder ID
        // This allows updating/cancelling specific notifications if needed later
        int notificationId = reminderId != -1 ? reminderId : 200; // Use reminder ID or a default

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "notifyIT487Reminder") // Use correct channel ID
                .setSmallIcon(R.drawable.ic_baseline_notifications_active_24)
                .setContentTitle(reminderTitle)
                .setContentText(reminderDescription)
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Use HIGH for reminders
                .setAutoCancel(true);

        // Add style for longer descriptions if they exist
        if (reminderDescription != null && !reminderDescription.isEmpty()) {
            builder.setStyle(new NotificationCompat.BigTextStyle().bigText(reminderDescription));
        }

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(notificationId, builder.build());

        // Optional: Here you could decide if the reminder should be auto-deleted from DB
        // after firing. e.g., Trigger a background service or WorkManager job to delete
        // AppDatabase.getDatabase(context).reminderDao().deleteById(reminderId); (Needs DAO method + background thread)
    }
}