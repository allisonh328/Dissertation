package com.example.project.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class PrototypeWithVersions {
    @Embedded public Prototype prototype;

    @Relation(
            parentColumn = "prototype_id",
            entityColumn = "proto_parent_id"
    )
    public List<ProtoVersion> version;
}
