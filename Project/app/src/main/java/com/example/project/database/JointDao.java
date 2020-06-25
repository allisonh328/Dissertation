package com.example.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface JointDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Joint joint);

    @Query("DELETE FROM joint_table")
    void deleteAll();

    @Query("DELETE FROM joint_table WHERE joint_id = :jointID")
    void deleteJoint(Integer jointID);

    @Query("SELECT * FROM joint_table where proto_parent_id = :prototypeID")
    LiveData<List<Joint>> getAllProtoJoints(Integer prototypeID);

    @Query("SELECT * FROM joint_table")
    LiveData<List<Joint>> getAllJoints();

}
