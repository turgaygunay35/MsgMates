package com.msgmates.app.core.messaging.model

import android.net.Uri

data class LocalAttachment(
    val kind: String, // "image", "video", "audio", "file", "location"
    val mime: String,
    val size: Long?,
    val width: Int?,
    val height: Int?,
    val durationMs: Long?, // for video/audio
    val localUri: Uri,
    val fileName: String?,
    val thumbB64: String? // Base64 encoded thumbnail
)
