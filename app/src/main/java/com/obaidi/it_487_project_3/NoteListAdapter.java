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
public class NoteListAdapter extends ListAdapter<Note, NoteListAdapter.NoteViewHolder> {

    private OnItemClickListener listener; // Listener instance

    // Constructor remains the same
    public NoteListAdapter(@NonNull DiffUtil.ItemCallback<Note> diffCallback) {
        super(diffCallback);
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.recyclerview_item, parent, false);
        return new NoteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, int position) {
        Note current = getItem(position);
        holder.bind(current);
    }

    public Note getNoteAt(int position) {
        return getItem(position);
    }

    // --- Click Listener Setup ---
    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }
    // --- End Click Listener Setup ---


    class NoteViewHolder extends RecyclerView.ViewHolder { // Make inner class non-static
        private final TextView noteItemView;
        private final TextView timestampView;

        private NoteViewHolder(View itemView) {
            super(itemView);
            noteItemView = itemView.findViewById(R.id.textViewNoteText);
            timestampView = itemView.findViewById(R.id.textViewTimestamp);

            // Set the click listener on the item view itself
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                // Check if listener exists and position is valid
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    listener.onItemClick(getItem(position));
                }
            });
        }

        public void bind(Note note) {
            noteItemView.setText(note.getNoteText());
            String formattedDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
                    .format(new Date(note.getTimestamp()));
            timestampView.setText(formattedDate);
        }
    }

    // DiffUtil remains the same
    static class NoteDiff extends DiffUtil.ItemCallback<Note> {
        @Override
        public boolean areItemsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Note oldItem, @NonNull Note newItem) {
            return oldItem.getNoteText().equals(newItem.getNoteText()) &&
                    oldItem.getTimestamp() == newItem.getTimestamp();
        }
    }
}