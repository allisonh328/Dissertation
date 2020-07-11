package com.protocapture.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    void insert(Link link);

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
}
