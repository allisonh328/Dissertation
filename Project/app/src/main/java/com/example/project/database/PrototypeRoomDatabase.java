package com.example.project.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

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
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    private static RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            // If you want to keep data through app restarts,
            // comment out the following block
            databaseWriteExecutor.execute(() -> {
                // Populate the database in the background.
                // If you want to start with more words, just add them.
                PrototypeDao dao = INSTANCE.prototypeDao();
                dao.deleteAll();

                /*Word word = new Word("Hello");
                dao.insert(word);
                word = new Word("World");
                dao.insert(word);*/
            });
        }
    };

}
