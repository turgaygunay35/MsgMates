package com.msgmates.app.core.disaster.mesh

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mesh_messages")
data class MeshMessage(
    @PrimaryKey
    val id: String,
    val senderId: String,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val type: MessageType,
    val content: String,
    val priority: String? = null
)
