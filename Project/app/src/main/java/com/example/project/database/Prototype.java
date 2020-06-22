package com.example.project.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "prototype_table")
public class Prototype {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "prototype_id")
    private Integer prototypeId = 0;

    @NonNull
    @ColumnInfo(name="prototype_name")
    private String prototypeName;

    public Prototype() {}

    public void setPrototypeId(@NonNull Integer prototypeId) { this.prototypeId = prototypeId; }

    public void setPrototypeName(@NonNull String prototypeName) { this.prototypeName = prototypeName; }

    public Integer getPrototypeId() { return this.prototypeId; }

    public String getPrototypeName() { return this.prototypeName; }
}
