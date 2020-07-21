package com.protocapture.project.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "prototype_table",
   indices = {@Index(value = {"prototype_name"}, unique = true)})
public class Prototype {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "prototype_id")
    private Integer prototypeId;

    @NonNull
    @ColumnInfo(name="prototype_name")
    private String prototypeName;

    @ColumnInfo(name="prototype_bitmap")
    private String prototypeBitmap;

    public Prototype() {}

    public void setPrototypeId(@NonNull Integer prototypeId) { this.prototypeId = prototypeId; }

    public void setPrototypeName(@NonNull String prototypeName) { this.prototypeName = prototypeName; }

    public void setPrototypeBitmap(@NonNull String prototypeBitmap) { this.prototypeBitmap = prototypeBitmap; }

    public Integer getPrototypeId() { return this.prototypeId; }

    public String getPrototypeName() { return this.prototypeName; }

    public String getPrototypeBitmap() { return this.prototypeBitmap; }
}
