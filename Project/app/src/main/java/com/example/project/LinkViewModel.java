package com.example.project;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
public class LinkViewModel extends AndroidViewModel {

    private LinkRepository mRepository;

    private LiveData<List<Prototype>> mAllLinks;

    public LinkViewModel (Application application) {
        super(application);
        mRepository = new LinkRepository(application);
        mAllLinks = mRepository.getAllLinks();
    }

    LiveData<List<Prototype>> getAllLinks() { return mAllLinks; }

    public void insert(Prototype prototype) { mRepository.insert(prototype); }
}
