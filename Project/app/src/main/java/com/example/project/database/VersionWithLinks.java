package com.example.project.database;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class VersionWithLinks {
    @Embedded
    public ProtoVersion version;

    @Relation(
            parentColumn = "version_id",
            entityColumn = "version_parent_id"
    )
    public List<Link> links;
}
