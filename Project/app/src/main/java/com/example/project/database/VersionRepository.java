package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class VersionRepository {
    private ProtoVersionDao mVersionDao;
    private LiveData<List<ProtoVersion>> mAllVersions;

    VersionRepository(Application application) {
        PrototypeRoomDatabase db = PrototypeRoomDatabase.getDatabase(application);
        mVersionDao = db.versionDao();
        mAllVersions = mVersionDao.getAllVersions();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<ProtoVersion>> getAllVersions() {
        return mAllVersions;
    }

    LiveData<List<ProtoVersion>> getAllProtoLinks(Integer prototypeID) { return mVersionDao.getAllProtoVersions(prototypeID); }

    LiveData<List<VersionWithLinks>> getVersionsWithLinks() { return mVersionDao.getVersionWithLinks(); }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(ProtoVersion version) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mVersionDao.insert(version);
        });
    }

    void delete() {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mVersionDao.deleteAll();
        });
    }
}
