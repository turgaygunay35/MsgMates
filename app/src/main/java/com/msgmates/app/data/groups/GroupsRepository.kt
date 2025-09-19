package com.msgmates.app.data.groups

import com.msgmates.app.domain.groups.CreateGroupRequest
import com.msgmates.app.domain.groups.User
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

@Singleton
class GroupsRepository @Inject constructor() {

    private val random = Random()

    /**
     * Get available users for group creation
     */
    fun getAvailableUsers(): Flow<List<User>> = flow {
        delay(500) // Simulate network delay
        emit(getUsersFromApi())
    }

    /**
     * Search users by name or phone
     */
    fun searchUsers(query: String): Flow<List<User>> = flow {
        delay(300) // Simulate network delay
        val allUsers = getUsersFromApi()
        val filteredUsers = if (query.isBlank()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                    user.phoneNumber.contains(query, ignoreCase = true)
            }
        }
        emit(filteredUsers)
    }

    /**
     * Create a new group
     */
    suspend fun createGroup(request: CreateGroupRequest): String {
        delay(1000) // Simulate network delay

        // Generate fake group ID
        val groupId = "group_${System.currentTimeMillis()}_${random.nextInt(1000, 9999)}"

        // In real implementation, this would:
        // 1. Create group in database
        // 2. Add members to group
        // 3. Send notifications to members
        // 4. Return the created group ID

        return groupId
    }

    /**
     * Get users from API - no hardcoded test users
     */
    private suspend fun getUsersFromApi(): List<User> {
        // TODO: Implement real API call to get users
        // For now, return empty list - no test users
        return emptyList()
    }
}
