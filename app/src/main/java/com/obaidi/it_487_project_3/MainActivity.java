package com.obaidi.it_487_project_3;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build; // Import Build
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    // Define Channel IDs consistently
    public static final String REMINDER_CHANNEL_ID = "notifyIT487Reminder"; // Make sure this matches EXACTLY
    public static final String TIMER_CHANNEL_ID = "notifyIT487Timer"; // Keep the timer one too

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // --- Create Notification Channels ---
        createNotificationChannels();
        // --- End Channel Creation ---

        bottomNavigationView = findViewById(R.id.bottomNavigationView);

        // Load default fragment (make sure this happens AFTER channel creation)
        if (savedInstanceState == null) { // Only load default on first creation
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TimerFragment())
                    .commit();
        }


        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Fragment selectedFragment = null;
                int itemId = item.getItemId();

                // Use your actual menu item IDs here
                if (itemId == R.id.navigation_timer) {
                    selectedFragment = new TimerFragment();
                } else if (itemId == R.id.navigation_notes) {
                    selectedFragment = new NotesFragment();
                } else if (itemId == R.id.navigation_reminders) {
                    selectedFragment = new RemindersFragment();
                } else if (itemId == R.id.navigation_quotes) {
                    selectedFragment = new QuotesFragment();
                }
                // ... add other fragments if needed

                if (selectedFragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }
                return false;
            }
        });
    }

    private void createNotificationChannels() {
        // Channels are only needed on API 26+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // --- Reminder Channel ---
            CharSequence reminderChannelName = getString(R.string.channel_name_reminders); // Use string resource
            String reminderChannelDesc = getString(R.string.channel_description_reminders); // Use string resource
            int reminderImportance = NotificationManager.IMPORTANCE_HIGH; // Reminders should be high importance
            NotificationChannel reminderChannel = new NotificationChannel(REMINDER_CHANNEL_ID, reminderChannelName, reminderImportance);
            reminderChannel.setDescription(reminderChannelDesc);

            // --- Timer Channel --- (If you haven't created it elsewhere)
            CharSequence timerChannelName = getString(R.string.channel_name_timer); // Use string resource
            String timerChannelDesc = getString(R.string.channel_description_timer); // Use string resource
            int timerImportance = NotificationManager.IMPORTANCE_HIGH; // Timer alerts are also important
            NotificationChannel timerChannel = new NotificationChannel(TIMER_CHANNEL_ID, timerChannelName, timerImportance);
            timerChannel.setDescription(timerChannelDesc);


            // --- Register the channels with the system ---
            // Don't create a new NotificationManager instance every time. Get the system service.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(reminderChannel);
                notificationManager.createNotificationChannel(timerChannel); // Create timer channel too
            }
        }
    }
}