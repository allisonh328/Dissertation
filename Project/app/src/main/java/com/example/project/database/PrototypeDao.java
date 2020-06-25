package com.example.project.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.example.project.database.Prototype;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Dao
public interface PrototypeDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Prototype prototype);

    @Query("DELETE FROM prototype_table")
    void deleteAll();

    @Query("DELETE FROM prototype_table WHERE prototype_id= :prototypeID")
    void deletePrototype(Integer prototypeID);

    @Query("SELECT * FROM prototype_table")
    LiveData<List<Prototype>> getAllPrototypes();

    @Query("SELECT * FROM prototype_table WHERE prototype_name = :prototypeName")
    LiveData<Prototype> getPrototype(String prototypeName);

    @Transaction
    @Query("SELECT * FROM prototype_table WHERE prototype_id= :prototypeID")
    LiveData<PrototypeWithComponents> getPrototypeWithComponents (Integer prototypeID);
}
