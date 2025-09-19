package com.msgmates.app.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cached_files")
data class CachedFileEntity(
    @PrimaryKey
    val id: String,
    val fileName: String,
    val localPath: String,
    val remoteUrl: String,
    val fileType: String,
    val fileSize: Long,
    val downloadedAt: Long,
    val expiresAt: Long?
)
