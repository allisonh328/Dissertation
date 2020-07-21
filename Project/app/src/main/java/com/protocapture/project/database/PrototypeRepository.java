package com.protocapture.project.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

    LiveData<Prototype> getPrototype(String prototypeName) { return mPrototypeDao.getPrototype(prototypeName); }

    LiveData<PrototypeWithComponents> getPrototypeWithComponents(Integer prototypeID) {
        return mPrototypeDao.getPrototypeWithComponents(prototypeID);
    }

    void setPrototypeBitmap(String bitmap, String prototypeName) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPrototypeDao.setPrototypeBitmap(bitmap, prototypeName);
        });
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Prototype prototype) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPrototypeDao.insert(prototype);
        });
    }

    void delete() {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPrototypeDao.deleteAll();
        });
    }

    void deletePrototype(Integer prototypeID) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mPrototypeDao.deletePrototype(prototypeID);
        });
    }
}
