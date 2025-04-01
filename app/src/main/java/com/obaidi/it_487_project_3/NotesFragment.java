package com.obaidi.it_487_project_3;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout; // Import LinearLayout
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.ItemTouchHelper; // Import ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar; // Import Snackbar
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import java.util.List;
import androidx.fragment.app.DialogFragment; // Import DialogFragment

public class NotesFragment extends Fragment implements NoteListAdapter.OnItemClickListener { // Implement listener

    private NotesViewModel notesViewModel;
    private TextInputEditText editTextNotes;
    private MaterialButton buttonSaveNotes;
    private RecyclerView recyclerView;
    private NoteListAdapter adapter;
    private LinearLayout emptyView; // Reference to empty state layout

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notes, container, false);

        editTextNotes = view.findViewById(R.id.edit_text_notes);
        buttonSaveNotes = view.findViewById(R.id.button_save_notes);
        recyclerView = view.findViewById(R.id.recyclerview);
        emptyView = view.findViewById(R.id.empty_view); // Get empty view reference

        adapter = new NoteListAdapter(new NoteListAdapter.NoteDiff());
        adapter.setOnItemClickListener(this); // Set the fragment as the listener
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // ViewModel Setup
        notesViewModel = new ViewModelProvider(this).get(NotesViewModel.class);

        // Observe Notes List
        notesViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            // Update the cached copy of the notes in the adapter.
            adapter.submitList(notes);
            // Show/Hide Empty View
            if (notes == null || notes.isEmpty()) {
                recyclerView.setVisibility(View.GONE);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                recyclerView.setVisibility(View.VISIBLE);
                emptyView.setVisibility(View.GONE);
            }
        });

        // Save Button Logic
        buttonSaveNotes.setOnClickListener(v -> {
            String noteText = editTextNotes.getText().toString().trim(); // Trim whitespace
            if (!noteText.isEmpty()) {
                Note note = new Note(noteText); // Timestamp added in constructor
                notesViewModel.insert(note);
                editTextNotes.setText(""); // Clear the input
                Toast.makeText(getContext(), R.string.toast_note_saved, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), R.string.toast_enter_note, Toast.LENGTH_SHORT).show();
            }
        });

        // --- Swipe-to-Delete Implementation ---
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,
                ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) { // Enable left and right swipe
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // We don't want move functionality
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                // 1. Get the note to delete
                int position = viewHolder.getAdapterPosition();
                Note noteToDelete = adapter.getNoteAt(position);

                // 2. Delete the note via ViewModel
                notesViewModel.delete(noteToDelete);

                // 3. Show Snackbar with Undo option
                Snackbar.make(recyclerView, R.string.snackbar_note_deleted, Snackbar.LENGTH_LONG)
                        .setAction(R.string.snackbar_undo, v -> {
                            // Undo action: Re-insert the note
                            notesViewModel.insert(noteToDelete);
                        })
                        .show();
            }
        }).attachToRecyclerView(recyclerView); // Attach the helper to RecyclerView
        // --- End Swipe-to-Delete ---

        return view;
    }

    // --- Implementation of OnItemClickListener ---
    @Override
    public void onItemClick(Note note) {
        // Create and show the EditNoteDialogFragment
        EditNoteDialogFragment dialogFragment = EditNoteDialogFragment.newInstance(note);
        // Use getChildFragmentManager() if showing from within a Fragment is preferred
        // Use getParentFragmentManager() or requireActivity().getSupportFragmentManager() otherwise
        dialogFragment.show(getParentFragmentManager(), "EditNoteDialog");
    }
}
