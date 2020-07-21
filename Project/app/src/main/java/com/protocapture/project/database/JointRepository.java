package com.protocapture.project.database;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.List;

public class JointRepository {

    private JointDao mJointDao;
    private LiveData<List<Joint>> mAllJoints;

    JointRepository(Application application) {
        PrototypeRoomDatabase db = PrototypeRoomDatabase.getDatabase(application);
        mJointDao = db.jointDao();
        mAllJoints = mJointDao.getAllJoints();
    }

    LiveData<List<Joint>> getAllJoints() { return mAllJoints; }

    LiveData<List<Joint>> getAllPrototypeJoints(Integer prototypeID) {
        return mJointDao.getAllProtoJoints(prototypeID);
    }

    LiveData<Joint> getJoint(String jointName) { return mJointDao.getJoint(jointName); }

    LiveData<Joint> getJointById(int jointID) { return mJointDao.getJointById(jointID); }

    LiveData<Prototype> getParentPrototype(int jointID) { return mJointDao.getParentPrototype(jointID); }

    LiveData<Link> getParentLink1(int jointID) { return mJointDao.getParentLink1(jointID); }

    LiveData<Link> getParentLink2(int jointID) { return mJointDao.getParentLink2(jointID); }

    void insert(Joint joint) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mJointDao.insert(joint);
        });
    }

    void deleteJoints() {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mJointDao.deleteAll();
        });
    }

    void deleteJoint(Integer jointID) {
        PrototypeRoomDatabase.databaseWriteExecutor.execute(() -> {
            mJointDao.deleteJoint(jointID);
        });
    }
}
