package com.msgmates.app.data.repository

import com.msgmates.app.data.local.db.dao.UserDao
import com.msgmates.app.domain.model.User
import com.msgmates.app.network.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepository(
    private val apiService: ApiService,
    private val userDao: UserDao
) {

    suspend fun getUserProfile(): Result<User> {
        return try {
            val response = apiService.getUserProfile()
            if (response.isSuccessful && response.body() != null) {
                val userResponse = response.body()!!
                val user = User(
                    id = userResponse.id,
                    username = userResponse.username,
                    email = userResponse.email,
                    phoneNumber = userResponse.phoneNumber,
                    profileImageUrl = userResponse.profileImageUrl,
                    isOnline = userResponse.isOnline,
                    lastSeen = userResponse.lastSeen,
                    isVerified = userResponse.isVerified,
                    bio = userResponse.bio,
                    createdAt = userResponse.createdAt,
                    updatedAt = userResponse.updatedAt
                )
                Result.success(user)
            } else {
                Result.failure(Exception("Failed to get user profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(user: User): Result<User> {
        return try {
            val userResponse = com.msgmates.app.data.remote.model.response.UserResponse(
                id = user.id,
                username = user.username,
                email = user.email,
                phoneNumber = user.phoneNumber,
                profileImageUrl = user.profileImageUrl,
                isOnline = user.isOnline,
                lastSeen = user.lastSeen,
                isVerified = user.isVerified,
                bio = user.bio,
                createdAt = user.createdAt,
                updatedAt = user.updatedAt
            )

            val response = apiService.updateUserProfile(userResponse)
            if (response.isSuccessful && response.body() != null) {
                val updatedUserResponse = response.body()!!
                val updatedUser = User(
                    id = updatedUserResponse.id,
                    username = updatedUserResponse.username,
                    email = updatedUserResponse.email,
                    phoneNumber = updatedUserResponse.phoneNumber,
                    profileImageUrl = updatedUserResponse.profileImageUrl,
                    isOnline = updatedUserResponse.isOnline,
                    lastSeen = updatedUserResponse.lastSeen,
                    isVerified = updatedUserResponse.isVerified,
                    bio = updatedUserResponse.bio,
                    createdAt = updatedUserResponse.createdAt,
                    updatedAt = updatedUserResponse.updatedAt
                )
                Result.success(updatedUser)
            } else {
                Result.failure(Exception("Failed to update user profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteAccount(): Result<Unit> {
        return try {
            val response = apiService.deleteAccount()
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Failed to delete account: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getLocalUser(userId: String): Flow<User?> {
        return userDao.getUserById(userId).map { userEntity ->
            userEntity?.let { entity ->
                User(
                    id = entity.id,
                    username = entity.username,
                    email = entity.email,
                    phoneNumber = entity.phoneNumber,
                    profileImageUrl = entity.profileImageUrl,
                    isOnline = entity.isOnline,
                    lastSeen = entity.lastSeen,
                    isVerified = entity.isVerified,
                    bio = entity.bio,
                    createdAt = entity.createdAt,
                    updatedAt = entity.updatedAt
                )
            }
        }
    }

    suspend fun saveUserLocally(user: User) {
        val userEntity = com.msgmates.app.data.local.db.entity.UserEntity(
            id = user.id,
            username = user.username,
            email = user.email,
            phoneNumber = user.phoneNumber,
            profileImageUrl = user.profileImageUrl,
            isOnline = user.isOnline,
            lastSeen = user.lastSeen,
            isVerified = user.isVerified,
            bio = user.bio,
            createdAt = user.createdAt,
            updatedAt = user.updatedAt
        )
        userDao.insertUser(userEntity)
    }
}
