package com.protocapture.project.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "link_table",
        foreignKeys = {@ForeignKey(entity = Prototype.class,
                parentColumns = "prototype_id",
                childColumns = "proto_parent_id",
                onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Joint.class,
                parentColumns = "joint_id",
                childColumns = "joint1_id",
                onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Joint.class,
                parentColumns = "joint_id",
                childColumns = "joint2_id",
                onDelete = ForeignKey.CASCADE)},
        indices = {@Index("proto_parent_id"),
                @Index("joint1_id"),
                @Index("joint2_id"),
                @Index(value = {"link_name"}, unique = true)})
public class Link {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "link_id")
    private Integer linkId;

    @NonNull
    @ColumnInfo(name="link_name")
    private String linkName;

    @NonNull
    @ColumnInfo(name="proto_parent_id")
    private Integer parentID;

    @NonNull
    @ColumnInfo(name="joint1_id")
    private Integer endpoint1;

    @NonNull
    @ColumnInfo(name="joint2_id")
    private Integer endpoint2;

    public Link() {}

    public void setLinkId(@NonNull Integer linkID) { this.linkId = linkID; }

    public void setLinkName(@NonNull String linkName) {
        this.linkName = linkName;
    }

    public void setParentID(@NonNull Integer parentID) { this.parentID = parentID; }

    public void setEndpoint1(@NonNull Integer endpoint1) { this.endpoint1 = endpoint1; }

    public void setEndpoint2(@NonNull Integer endpoint2) { this.endpoint2 = endpoint2; }

    public Integer getLinkId() { return this.linkId; }

    public String getLinkName() { return this.linkName; }

    public Integer getParentID() { return this.parentID; }

    public Integer getEndpoint1() { return this.endpoint1; }

    public Integer getEndpoint2() { return this.endpoint2; }
}
