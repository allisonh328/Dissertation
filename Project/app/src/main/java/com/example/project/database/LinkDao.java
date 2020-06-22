package com.example.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Link link);

    @Query("DELETE FROM link_table")
    void deleteAll();

    @Query("SELECT * FROM link_table where parent_id = :prototypeID")
    LiveData<List<Link>> getAllProtoLinks(Integer prototypeID);

    @Query("SELECT * FROM link_table")
    LiveData<List<Link>> getAllLinks();
}
