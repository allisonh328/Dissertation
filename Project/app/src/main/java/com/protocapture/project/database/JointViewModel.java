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

    //public LiveData<PointTuple> getCoordinates(int jointID) { return mRepository.getCoordinates(jointID); }

    public void insert(Joint joint) { mRepository.insert(joint); }

    public void updateJoints(List<Joint> joints) { mRepository.updateJoints(joints); }

    public void updateJoint(Joint joint) { mRepository.updateJoint(joint); }

    public void delete() { mRepository.deleteJoints(); }

    public void deleteJoint(Integer jointID) { mRepository.deleteJoint(jointID); }

    public void setLink1Id(int linkID, int jointID) { mRepository.setLink1Id(linkID, jointID); }

    public void setLink2Id(int linkID, int jointID) { mRepository.setLink2Id(linkID, jointID); }
}
