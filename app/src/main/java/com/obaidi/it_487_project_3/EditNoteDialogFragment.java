package com.obaidi.it_487_project_3;

import android.app.Dialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;

public class EditNoteDialogFragment extends DialogFragment {

    private static final String ARG_NOTE_ID = "note_id";
    private static final String ARG_NOTE_TEXT = "note_text";
    private static final String ARG_NOTE_TIMESTAMP = "note_timestamp";

    private TextInputEditText editTextNote;
    private NotesViewModel notesViewModel;
    private int noteId;
    private long noteTimestamp; // Store original timestamp if needed for comparison later

    // Factory method to create instance with arguments
    public static EditNoteDialogFragment newInstance(Note note) {
        EditNoteDialogFragment fragment = new EditNoteDialogFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_NOTE_ID, note.getId());
        args.putString(ARG_NOTE_TEXT, note.getNoteText());
        args.putLong(ARG_NOTE_TIMESTAMP, note.getTimestamp());
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_note, null);

        editTextNote = dialogView.findViewById(R.id.edit_note_edit_text);

        // Get ViewModel (scoped to the parent Fragment/Activity)
        notesViewModel = new ViewModelProvider(requireActivity()).get(NotesViewModel.class);

        // Retrieve arguments
        if (getArguments() != null) {
            noteId = getArguments().getInt(ARG_NOTE_ID, -1); // Use -1 as default invalid ID
            String currentText = getArguments().getString(ARG_NOTE_TEXT, "");
            noteTimestamp = getArguments().getLong(ARG_NOTE_TIMESTAMP, 0);
            editTextNote.setText(currentText);
            editTextNote.setSelection(currentText.length()); // Move cursor to end
        } else {
            // Handle error: arguments not provided
            Toast.makeText(getContext(), R.string.error_loading_note, Toast.LENGTH_SHORT).show();
            dismiss();
        }

        builder.setView(dialogView)
                .setTitle(R.string.dialog_title_edit_note)
                .setPositiveButton(R.string.dialog_save, (dialog, id) -> {
                    // Save button clicked
                    String updatedText = editTextNote.getText().toString().trim();
                    if (noteId != -1 && !TextUtils.isEmpty(updatedText)) {
                        // Create a Note object for update
                        // It's okay to create a new one, Room uses the PrimaryKey (id)
                        Note updatedNote = new Note(updatedText);
                        updatedNote.setId(noteId);
                        // Timestamp is updated automatically in the Repository's update method
                        notesViewModel.update(updatedNote);
                        Toast.makeText(getContext(), R.string.toast_note_updated, Toast.LENGTH_SHORT).show();
                    } else if (TextUtils.isEmpty(updatedText)){
                        Toast.makeText(getContext(), R.string.toast_note_cannot_be_empty, Toast.LENGTH_SHORT).show();
                        // Optionally prevent dialog from closing if empty
                    } else {
                        Toast.makeText(getContext(), R.string.error_updating_note, Toast.LENGTH_SHORT).show();
                    }
                    // Dialog dismisses automatically on positive button click
                })
                .setNegativeButton(R.string.dialog_cancel, (dialog, id) -> {
                    // Cancel button clicked - dismisses automatically
                    EditNoteDialogFragment.this.getDialog().cancel();
                });

        return builder.create();
    }
}