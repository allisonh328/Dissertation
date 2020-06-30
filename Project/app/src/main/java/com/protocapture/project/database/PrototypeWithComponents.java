package com.protocapture.project.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class PrototypeWithComponents {
    @Embedded public Prototype prototype;

    @Relation(
            parentColumn = "prototype_id",
            entityColumn = "proto_parent_id"
    )
    public List<Joint> joint;

    @Relation(
            parentColumn = "prototype_id",
            entityColumn = "proto_parent_id"
    )
    public List<Link> link;
}
