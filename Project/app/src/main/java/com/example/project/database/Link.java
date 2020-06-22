package com.example.project.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "link_table",
        foreignKeys = @ForeignKey(entity = Prototype.class,
            parentColumns = "prototype_id",
            childColumns = "parent_id",
            onDelete = ForeignKey.CASCADE),
        indices = {@Index(value = {"parent_id"}, unique = true)})
public class Link {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "link_id")
    private Integer linkId;

    @NonNull
    @ColumnInfo(name="link_name")
    private String linkName;

    @NonNull
    @ColumnInfo(name="parent_id")
    private Integer parentID;

    public Link(Integer parentID) {
        this.linkName = "link" + Integer.toString(linkId);
        this.parentID = parentID;
    }

    public void setLinkId(@NonNull Integer linkID) { this.linkId = linkID; }

    public void setLinkName(@NonNull String linkName) {
        this.linkName = linkName;
    }

    public void setParentID(@NonNull Integer parentID) { this.parentID = parentID; }

    public Integer getLinkId() { return this.linkId; }

    public String getLinkName() { return this.linkName; }

    public Integer getParentID() { return this.parentID; }
}
