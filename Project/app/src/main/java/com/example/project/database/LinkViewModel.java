package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.project.database.Prototype;
import com.example.project.database.PrototypeRepository;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
public class LinkViewModel extends AndroidViewModel {

    private LinkRepository mRepository;

    private LiveData<List<Link>> mAllLinks;

    public LinkViewModel (Application application, Integer versionID) {
        super(application);
        mRepository = new LinkRepository(application);
        mAllLinks = mRepository.getAllVersionLinks(versionID);
    }

    LiveData<List<Link>> getAllLinks() { return mAllLinks; }

    public void insert(Link link) { mRepository.insert(link); }
}
