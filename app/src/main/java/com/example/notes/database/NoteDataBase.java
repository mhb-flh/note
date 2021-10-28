package com.example.notes.database;

import android.content.Context;

import com.example.notes.dao.NoteDao;
import com.example.notes.entities.Note;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = Note.class, version = 1, exportSchema = false)
public abstract class NoteDataBase extends RoomDatabase {

    private static NoteDataBase noteDataBase;

    public static synchronized NoteDataBase getDataBase(Context context) {
        if (noteDataBase == null) {
            noteDataBase = Room.databaseBuilder(context, NoteDataBase.class, "notes_db").build();
        }
        return noteDataBase;
    }

    public abstract NoteDao noteDao();

}
