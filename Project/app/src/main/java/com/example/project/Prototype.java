package com.example.project;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "prototype_table")
public class Prototype {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "link_id")
    private Integer linkId;

    @NonNull
    @ColumnInfo(name="link_name")
    private String linkName;

    public Prototype() {
        this.linkName = "link" + Integer.toString(linkId);
    }

    public void setLinkName(@NonNull String linkName) {
        this.linkName = linkName;
    }

    public Integer getLinkId() { return this.linkId; }

    public String getLinkName() { return this.linkName; }
}
