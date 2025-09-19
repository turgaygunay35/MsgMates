package com.msgmates.app.domain.groups

import java.time.LocalDateTime

data class Group(
    val id: String,
    val name: String,
    val description: String? = null,
    val avatarUrl: String? = null,
    val createdBy: String, // User ID who created the group
    val createdAt: LocalDateTime,
    val isActive: Boolean = true,
    val memberCount: Int = 0
)

data class GroupMember(
    val id: String,
    val groupId: String,
    val userId: String,
    val userName: String,
    val userAvatarUrl: String? = null,
    val role: GroupRole = GroupRole.MEMBER,
    val joinedAt: LocalDateTime,
    val isActive: Boolean = true
)

enum class GroupRole {
    ADMIN,
    MODERATOR,
    MEMBER
}

data class CreateGroupRequest(
    val name: String,
    val description: String? = null,
    val avatarUri: String? = null,
    val memberIds: List<String>
)
