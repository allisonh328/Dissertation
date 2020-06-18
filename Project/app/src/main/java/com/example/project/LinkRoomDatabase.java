package com.example.project;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Database(entities = {Prototype.class}, version = 1, exportSchema = false)
public abstract class LinkRoomDatabase extends RoomDatabase {

    public abstract LinkDao linkDao();

    private static volatile LinkRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static LinkRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (LinkRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            LinkRoomDatabase.class, "link_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
