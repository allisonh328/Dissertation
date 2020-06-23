package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class VersionViewModel extends AndroidViewModel {

    private VersionRepository mRepository;

    private LiveData<List<ProtoVersion>> mAllVersions;

    public VersionViewModel (Application application, Integer prototypeID) {
        super(application);
        mRepository = new VersionRepository(application);
        mAllVersions = mRepository.getAllProtoLinks(prototypeID);
    }

    LiveData<List<ProtoVersion>> getmAllVersions() { return mAllVersions; }

    public void insert(ProtoVersion version) { mRepository.insert(version); }
}
