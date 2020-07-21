package com.protocapture.project.database;

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

    public LiveData<Joint> getJoint(String jointName) { return mRepository.getJoint(jointName); }

    public LiveData<Joint> getJointById(int jointID) { return mRepository.getJointById(jointID); }

    public LiveData<Prototype> getParentPrototype(int jointID) {return mRepository.getParentPrototype(jointID); }

    public LiveData<Link> getParentLink1(int jointID) {return mRepository.getParentLink1(jointID); }

    public LiveData<Link> getParentLink2(int jointID) {return mRepository.getParentLink2(jointID); }

    public void insert(Joint joint) { mRepository.insert(joint); }

    public void delete() { mRepository.deleteJoints(); }

    public void deleteJoint(Integer jointID) { mRepository.deleteJoint(jointID); }
}
