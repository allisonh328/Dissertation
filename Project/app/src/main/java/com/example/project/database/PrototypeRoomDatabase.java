package com.example.project.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Database(entities = {Prototype.class, Link.class}, version = 1, exportSchema = false)
public abstract class PrototypeRoomDatabase extends RoomDatabase {

    public abstract PrototypeDao prototypeDao();
    public abstract LinkDao linkDao();

    private static volatile PrototypeRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static PrototypeRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PrototypeRoomDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            PrototypeRoomDatabase.class, "prototype_database")
                            .build();
                }
            }
        }
        return INSTANCE;
    }

}
