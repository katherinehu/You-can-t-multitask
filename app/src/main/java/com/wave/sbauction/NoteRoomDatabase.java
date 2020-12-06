//package com.wave.sbauction;
//
//import android.content.Context;
//
//import androidx.room.Database;
//import androidx.room.Room;
//import androidx.room.RoomDatabase;
//
//@Database(entities = Note.class, version = 1)
//public abstract class NoteRoomDatabase extends RoomDatabase {
//
//    public abstract NoteDao noteDao();
//
//
//    private static volatile NoteRoomDatabase noteRoomInstance;
//
//    //Check that there isn't already a database
//    static NoteRoomDatabase getDatabase(final Context context) {
//        if (noteRoomInstance == null) {
//            synchronized (NoteRoomDatabase.class) {
//                if (noteRoomInstance == null) {
//                    noteRoomInstance = Room.databaseBuilder(context.getApplicationContext(),
//                            NoteRoomDatabase.class, "note_database")
//                            .build();
//                }
//            }
//        }
//        return noteRoomInstance;
//    }
//
//}
