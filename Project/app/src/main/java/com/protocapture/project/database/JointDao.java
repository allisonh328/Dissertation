package com.protocapture.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface JointDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Joint joint);

    @Update
    void updateJoints(List<Joint> joints);

    @Update
    void updateJoint(Joint joint);

    @Query("DELETE FROM joint_table")
    void deleteAll();

    @Query("DELETE FROM joint_table WHERE joint_id = :jointID")
    void deleteJoint(Integer jointID);

    @Query("SELECT * FROM joint_table where proto_parent_id = :prototypeID")
    LiveData<List<Joint>> getAllProtoJoints(Integer prototypeID);

    @Query("SELECT * FROM joint_table")
    LiveData<List<Joint>> getAllJoints();

    @Query("SELECT * FROM joint_table WHERE joint_name = :jointName")
    LiveData<Joint> getJoint(String jointName);

    @Query("SELECT * FROM joint_table WHERE joint_id = :jointID")
    LiveData<Joint> getJointById(int jointID);

    @Query("SELECT * FROM prototype_table " +
    " JOIN joint_table ON prototype_id = proto_parent_id " +
    " WHERE joint_id = :jointID")
    LiveData<Prototype> getParentPrototype(int jointID);

    @Query("SELECT * FROM link_table " +
            " JOIN joint_table ON link_id = link1_parent_id " +
            " WHERE joint_id = :jointID")
    LiveData<Link> getParentLink1(int jointID);

    @Query("SELECT * FROM link_table " +
            " JOIN joint_table ON link_id = link2_parent_id " +
            " WHERE joint_id = :jointID")
    LiveData<Link> getParentLink2(int jointID);

    @Query("UPDATE joint_table SET link1_parent_id = :linkID WHERE joint_id = :jointID")
    void setLink1Id(int linkID, int jointID);

    @Query("UPDATE joint_table SET link2_parent_id = :linkID WHERE joint_id = :jointID")
    void setLink2Id(int linkID, int jointID);

   // @Query("SELECT joint_x, joint_y FROM joint_table WHERE joint_id = :jointID")
   // LiveData<PointTuple> getCoordinates(int jointID);
}
