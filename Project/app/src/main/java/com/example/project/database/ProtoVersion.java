package com.example.project.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "version_table",
        foreignKeys = @ForeignKey(entity = Prototype.class,
                parentColumns = "prototype_id",
                childColumns = "proto_parent_id",
                onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"proto_parent_id"}, unique = true)})
public class ProtoVersion {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "version_id")
    private Integer versionId;

    @NonNull
    @ColumnInfo(name="version_name")
    private String versionName;

    @NonNull
    @ColumnInfo(name="proto_parent_id")
    private Integer parentID;

    public ProtoVersion() {}

    public void setVersionId(@NonNull Integer versionID) { this.versionId = versionID; }

    public void setVersionName(@NonNull String versionName) {
        this.versionName = versionName;
    }

    public void setParentID(@NonNull Integer parentID) { this.parentID = parentID; }

    public Integer getVersionId() { return this.versionId; }

    public String getVersionName() { return this.versionName; }

    public Integer getParentID() { return this.parentID; }
}

