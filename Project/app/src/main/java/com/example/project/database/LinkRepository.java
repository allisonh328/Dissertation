package com.example.project.database;

import android.app.Application;
import android.os.AsyncTask;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.RoomDatabase;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
public class LinkRepository {
    private LinkDao mLinkDao;
    private LiveData<List<Link>> mAllLinks;

    LinkRepository(Application application) {
        PrototypeRoomDatabase db = PrototypeRoomDatabase.getDatabase(application);
        mLinkDao = db.linkDao();
        mAllLinks = mLinkDao.getAllLinks();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    LiveData<List<Link>> getAllLinks() {
        return mAllLinks;
    }

    LiveData<List<Link>> getAllPrototypeLinks(Integer prototypeID) {
        return mLinkDao.getAllPrototypeLinks(prototypeID);
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    void insert(Link link) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mLinkDao.insert(link);
        });
    }

    void deleteLinks() {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mLinkDao.deleteAll();
        });
    }

    void deleteLink(Integer linkID) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mLinkDao.deleteLink(linkID);
        });
    }

}
