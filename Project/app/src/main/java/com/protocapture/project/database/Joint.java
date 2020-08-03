package com.protocapture.project.database;

import androidx.annotation.NonNull;
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
                onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Link.class,
                parentColumns = "link_id",
                childColumns = "link2_parent_id",
                onDelete = ForeignKey.SET_NULL),
                @ForeignKey(entity = Prototype.class,
                parentColumns = "prototype_id",
                childColumns = "proto_parent_id",
                onDelete = ForeignKey.CASCADE)},
        indices = {@Index("link1_parent_id"),
                @Index("link2_parent_id"),
                @Index("proto_parent_id"),
                @Index(value = {"joint_name"}, unique = true)})
public class Joint {

    public static final int FREE = 0;
    public static final int FIXED = 1;

    @PrimaryKey(autoGenerate = true)
    @NonNull
    @ColumnInfo(name="joint_id")
    private Integer jointId;

    @NonNull
    @ColumnInfo(name="joint_name")
    private String jointName;

    @NonNull
    @ColumnInfo(name="joint_x")
    private Double xCoord;

    @NonNull
    @ColumnInfo(name="joint_y")
    private Double yCoord;

    @ColumnInfo(name="link1_parent_id")
    private Integer link1ID;

    @ColumnInfo(name="link2_parent_id")
    private Integer link2ID;

    @NonNull
    @ColumnInfo(name="proto_parent_id")
    private Integer prototypeID;

    @ColumnInfo(name="constraint")
    private Integer constraint = FREE;

    public Joint() {}

    public void setJointId(@NonNull Integer jointID) { this.jointId = jointID; }

    public void setJointName(@NonNull String jointName) {
        this.jointName = jointName;
    }

    public void setXCoord(@NonNull Double x) { xCoord = x; }

    public void setYCoord(@NonNull Double y) { yCoord = y; }

    public void setLink1ID(@NonNull Integer parentID) { link1ID = parentID; }

    public void setLink2ID(Integer parentID) { link2ID = parentID; }

    public void setPrototypeID(Integer prototypeID) { this.prototypeID = prototypeID; }

    public void setConstraint(Integer constraint) { this.constraint = constraint; }

    public Integer getJointId() { return jointId; }

    public String getJointName() { return jointName; }

    public Double getXCoord() { return xCoord; }

    public Double getYCoord() { return yCoord; }

    public Integer getLink1ID() { return link1ID; }

    public Integer getLink2ID() { return link2ID; }

    public Integer getPrototypeID() { return prototypeID; }

    public Integer getConstraint() { return constraint; }
}

