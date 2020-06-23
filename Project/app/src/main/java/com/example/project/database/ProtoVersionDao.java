package com.example.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface ProtoVersionDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(ProtoVersion protoVersion);

    @Query("DELETE FROM version_table")
    void deleteAll();

    @Query("SELECT * FROM version_table where proto_parent_id = :prototypeID")
    LiveData<List<ProtoVersion>> getAllProtoVersions(Integer prototypeID);

    @Query("SELECT * FROM version_table")
    LiveData<List<ProtoVersion>> getAllVersions();

    @Transaction
    @Query("SELECT * FROM version_table")
    LiveData<List<VersionWithLinks>> getVersionWithLinks();
}
