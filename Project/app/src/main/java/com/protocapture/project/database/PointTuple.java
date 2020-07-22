package com.protocapture.project.database;

import androidx.room.ColumnInfo;

public class PointTuple {
    @ColumnInfo(name = "x")
    public String x;

    @ColumnInfo(name = "y")
    public String y;
}
