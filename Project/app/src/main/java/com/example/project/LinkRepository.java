package com.example.project;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
public class LinkRepository {
    private LinkDao mLinkDao;
    private LiveData<List<Prototype>> mAllLinks;

    // Note that in order to unit test the LinkRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    LinkRepository(Application application) {
        LinkRoomDatabase db = LinkRoomDatabase.getDatabase(application);
        mLinkDao = db.linkDao();
        mAllLinks = mLinkDao.getAllLinks();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Prototype>> getAllLinks() {
        return mAllLinks;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Prototype prototype) {
        LinkRoomDatabase.databaseWriteExecutor.execute(() -> {
            mLinkDao.insert(prototype);
        });
    }
}
