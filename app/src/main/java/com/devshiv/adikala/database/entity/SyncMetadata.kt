package com.devshiv.adikala.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sync_metadata")
data class SyncMetadata(
    @PrimaryKey
    @ColumnInfo(name = "sync_key")
    val syncKey: String,
    val value: String = "",
    val lastSynced: Long = System.currentTimeMillis()
)