package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.project.database.Prototype;
import com.example.project.database.PrototypeDao;
import com.example.project.database.PrototypeRoomDatabase;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
public class PrototypeRepository {
    private PrototypeDao mPrototypeDao;
    private LiveData<List<Prototype>> mAllPrototypes;

    // Note that in order to unit test the PrototypeRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    PrototypeRepository(Application application) {
        PrototypeRoomDatabase db = PrototypeRoomDatabase.getDatabase(application);
        mPrototypeDao = db.prototypeDao();
        mAllPrototypes = mPrototypeDao.getAllPrototypes();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Prototype>> getAllPrototypes() {
        return mAllPrototypes;
    }

    LiveData<List<PrototypeWithVersions>> getPrototypesWithVersions() { return mPrototypeDao.getPrototypeWithVersions(); }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Prototype prototype) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPrototypeDao.insert(prototype);
        });
    }

    void delete() { mPrototypeDao.deleteAll(); }
}
