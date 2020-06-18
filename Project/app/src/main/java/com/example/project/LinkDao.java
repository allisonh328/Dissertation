package com.example.project;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Dao
public interface LinkDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(Prototype prototype);

    @Query("DELETE FROM Prototype")
    void deleteAll();

    @Query("SELECT * FROM Prototype")
    LiveData<List<Prototype>> getAllLinks();
}
