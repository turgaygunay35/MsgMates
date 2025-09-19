package com.msgmates.app.data.contacts.remote.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SaltResponse(
    @SerialName("salt")
    val salt: String
)

@Serializable
data class MatchRequest(
    @SerialName("hashes")
    val hashes: List<String>
)

@Serializable
data class MatchResponse(
    @SerialName("matches")
    val matches: List<ContactMatch>
)

@Serializable
data class ContactMatch(
    @SerialName("hash")
    val hash: String,
    @SerialName("is_user")
    val isUser: Boolean
)

@Serializable
data class PresenceRequest(
    @SerialName("hashes")
    val hashes: List<String>
)

@Serializable
data class PresenceResponse(
    @SerialName("presence")
    val presence: List<ContactPresence>
)

@Serializable
data class ContactPresence(
    @SerialName("hash")
    val hash: String,
    @SerialName("online")
    val online: Boolean,
    @SerialName("last_seen_epoch")
    val lastSeenEpoch: Long? = null
)
