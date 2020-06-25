package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
public class LinkViewModel extends AndroidViewModel {

    private LinkRepository mRepository;

    private LiveData<List<Link>> mAllLinks;

    public LinkViewModel(Application application) {
        super(application);
        mRepository = new LinkRepository(application);
    }

    public void setAllLinks(Integer prototypeID) {
        mAllLinks = mRepository.getAllPrototypeLinks(prototypeID);
    }

    public LiveData<List<Link>> getAllLinks() { return mAllLinks; }

    public LiveData<List<Link>> getAllProtoLinks(Integer prototypeID) {
        return mRepository.getAllPrototypeLinks(prototypeID);
    }

    public LiveData<Link> getLink(String linkName) { return mRepository.getLink(linkName); }

    public void insert(Link link) { mRepository.insert(link); }

    public void deleteLink(Integer linkID) { mRepository.deleteLink(linkID); }
}
