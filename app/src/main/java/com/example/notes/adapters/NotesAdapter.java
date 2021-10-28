package com.example.notes.adapters;

import android.annotation.SuppressLint;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.notes.R;
import com.example.notes.entities.Note;
import com.example.notes.listeners.NotesListeners;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.NoteViewHolder> {

    private List<Note> notes;
    private NotesListeners notesListeners;
    private Timer timer;
    private List<Note> noteSource;

    public NotesAdapter(List<Note> notes, NotesListeners notesListeners) {
        this.notes = notes;
        this.notesListeners = notesListeners;
        noteSource = notes;
    }

    @NonNull
    @Override
    public NoteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new NoteViewHolder(LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_container_note, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NoteViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.setNote(notes.get(position));
        holder.layoutNote.setOnClickListener(view -> notesListeners.onNoteClicked(notes.get(position), position));
    }

    @Override
    public int getItemCount() {
        return notes.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class NoteViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle, txtDateTime;
        LinearLayout layoutNote;
        RoundedImageView imgNote;

        NoteViewHolder(@NonNull View itemView) {
            super(itemView);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDateTime = itemView.findViewById(R.id.txtDateTime);
            layoutNote = itemView.findViewById(R.id.layoutNote);
            imgNote = itemView.findViewById(R.id.imgNote);
        }

        void setNote(Note note) {
            txtTitle.setText(note.getTitle());
            txtDateTime.setText(note.getDateTime());
            layoutNote.setBackgroundColor(note.getColor());
            if (note.getImgPath() != null) {
                imgNote.setImageBitmap(BitmapFactory.decodeFile(note.getImgPath()));
                imgNote.setVisibility(View.VISIBLE);
            } else {
                imgNote.setVisibility(View.GONE);
            }


        }
    }

    public void searchNotes(final String searchKeyWord) {
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (searchKeyWord.trim().isEmpty()) {
                    notes = noteSource;
                } else {
                    ArrayList<Note> temp = new ArrayList<>();
                    for (Note note : noteSource) {
                        if (note.getTitle().toLowerCase().contains(searchKeyWord.toLowerCase())
                                || note.getNoteText().toLowerCase().contains(searchKeyWord.toLowerCase())) {
                            temp.add(note);
                        }
                    }
                    notes = temp;
                }
                new Handler(Looper.getMainLooper()).post((new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                    }
                }));
            }
        }, 500);
    }

    public void cancelTimer() {
        if (timer != null) {
            timer.cancel();
        }
    }
}
