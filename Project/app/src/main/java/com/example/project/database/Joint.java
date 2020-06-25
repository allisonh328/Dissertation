package com.example.project.database;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

//https://codelabs.developers.google.com/codelabs/android-room-with-a-view/index.html
@Entity(tableName = "joint_table",
        foreignKeys = { @ForeignKey(entity = Link.class,
                parentColumns = "link_id",
                childColumns = "link1_parent_id",
                onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Link.class,
                parentColumns = "link_id",
                childColumns = "link2_parent_id",
                onDelete = ForeignKey.CASCADE),
                @ForeignKey(entity = Prototype.class,
                parentColumns = "prototype_id",
                childColumns = "proto_parent_id",
                onDelete = ForeignKey.CASCADE)},
        indices = {@Index("link1_parent_id"),
                @Index("link2_parent_id"),
                @Index("proto_parent_id"),
                @Index(value = {"joint_name"}, unique = true)})
public class Joint {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name = "joint_id")
    private Integer jointId;

    @NonNull
    @ColumnInfo(name="joint_name")
    private String jointName;

    @NonNull
    @ColumnInfo(name="link1_parent_id")
    private Integer link1ID;

    @ColumnInfo(name="link2_parent_id")
    private Integer link2ID;

    @NonNull
    @ColumnInfo(name="proto_parent_id")
    private Integer prototypeID;

    @ColumnInfo(name="fixed")
    private Boolean fixed = false;

    public Joint() {}

    public void setJointId(@NonNull Integer jointID) { this.jointId = jointID; }

    public void setJointName(@NonNull String jointName) {
        this.jointName = jointName;
    }

    public void setLink1ID(@NonNull Integer parentID) { link1ID = parentID; }

    public void setLink2ID(Integer parentID) { link2ID = parentID; }

    public void setPrototypeID(Integer prototypeID) { this.prototypeID = prototypeID; }

    public void setFixed(Boolean fixed) { this.fixed = fixed; }

    public Integer getJointId() { return jointId; }

    public String getJointName() { return jointName; }

    public Integer getLink1ID() { return link1ID; }

    public Integer getLink2ID() { return link2ID; }

    public Integer getPrototypeID() { return prototypeID; }

    public Boolean isFixed() { return fixed; }
}

