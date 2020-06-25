package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.security.Policy;
import java.util.List;

public class PrototypeViewModel extends AndroidViewModel {

    private PrototypeRepository mRepository;

    private LiveData<List<Prototype>> mAllPrototypes;

    public PrototypeViewModel (Application application) {
        super(application);
        mRepository = new PrototypeRepository(application);
        mAllPrototypes = mRepository.getAllPrototypes();
    }

    public LiveData<List<Prototype>> getAllPrototypes() { return mAllPrototypes; }

    public LiveData<Prototype> getPrototype(String prototypeName) { return mRepository.getPrototype(prototypeName); }

    public void insert(Prototype prototype) { mRepository.insert(prototype); }

    public void delete() { mRepository.delete(); }

    public void deletePrototype(Integer prototypeID) { mRepository.deletePrototype(prototypeID);}
}
