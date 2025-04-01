package com.obaidi.it_487_project_3;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import java.text.DateFormat;
import java.util.Date;

public class ReminderListAdapter extends ListAdapter<Reminder, ReminderListAdapter.ReminderViewHolder> {

    protected ReminderListAdapter() {
        super(DIFF_CALLBACK);
    }

    @NonNull
    @Override
    public ReminderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_reminder_item, parent, false);
        return new ReminderViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ReminderViewHolder holder, int position) {
        Reminder currentReminder = getItem(position);
        holder.bind(currentReminder);
    }

    public Reminder getReminderAt(int position) {
        return getItem(position);
    }

    static class ReminderViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView descriptionView;
        private final TextView timeView;

        public ReminderViewHolder(@NonNull View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.textViewReminderTitle);
            descriptionView = itemView.findViewById(R.id.textViewReminderDescription);
            timeView = itemView.findViewById(R.id.textViewReminderTime);
        }

        public void bind(Reminder reminder) {
            titleView.setText(reminder.getTitle());
            descriptionView.setText(reminder.getDescription());
            String formattedTime = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(reminder.getTriggerTimeMillis()));
            timeView.setText(formattedTime);
        }
    }

    private static final DiffUtil.ItemCallback<Reminder> DIFF_CALLBACK = new DiffUtil.ItemCallback<Reminder>() {
        @Override
        public boolean areItemsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Reminder oldItem, @NonNull Reminder newItem) {
            return oldItem.getTitle().equals(newItem.getTitle()) &&
                    oldItem.getDescription().equals(newItem.getDescription()) &&
                    oldItem.getTriggerTimeMillis() == newItem.getTriggerTimeMillis();
        }
    };
}
