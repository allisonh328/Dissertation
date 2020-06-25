package com.example.project.database;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.List;

public class JointViewModel extends AndroidViewModel {

    private JointRepository mRepository;

    private LiveData<List<Joint>> mAllJoints;

    public JointViewModel(Application application) {
        super(application);
        mRepository = new JointRepository(application);
    }

    public void setAllJoints(Integer prototypeID) {
        mAllJoints = mRepository.getAllPrototypeJoints(prototypeID);
    }

    public LiveData<List<Joint>> getAllJoints() { return mAllJoints; }

    public LiveData<List<Joint>> getAllProtoJoints(Integer prototypeID) {
        return mRepository.getAllPrototypeJoints(prototypeID);
    }

    public void insert(Joint joint) { mRepository.insert(joint); }

    public void deleteJoint(Integer jointID) { mRepository.deleteJoint(jointID); }
}
