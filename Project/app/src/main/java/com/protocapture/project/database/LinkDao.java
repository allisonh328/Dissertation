package com.protocapture.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(Link link);

    @Update
    void updateLink(Link link);

    @Query("DELETE FROM link_table")
    void deleteAll();

    @Query("DELETE FROM link_table WHERE link_id = :linkID")
    void deleteLink(Integer linkID);

    @Query("SELECT * FROM link_table where proto_parent_id = :prototypeID")
    LiveData<List<Link>> getAllPrototypeLinks(Integer prototypeID);

    @Query("SELECT * FROM link_table")
    LiveData<List<Link>> getAllLinks();

    @Query("SELECT * FROM link_table WHERE link_name = :linkName")
    LiveData<Link> getLink(String linkName);

    @Query("SELECT * FROM link_table WHERE link_id = :linkID")
    LiveData<Link> getLinkById(int linkID);

    @Query("SELECT * FROM prototype_table " +
            " JOIN link_table ON prototype_id = proto_parent_id " +
            " WHERE link_id = :linkID")
    LiveData<Prototype> getParentPrototype(int linkID);

    @Query("SELECT * FROM joint_table " +
            " JOIN link_table ON joint_id = joint1_id " +
            " WHERE link_id = :linkID")
    LiveData<Joint> getEndpoint1(int linkID);

    @Query("SELECT * FROM joint_table " +
            " JOIN link_table ON joint_id = joint2_id " +
            " WHERE link_id = :linkID")
    LiveData<Joint> getEndpoint2(int linkID);
}
