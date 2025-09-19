package com.msgmates.app.data.journal

import com.msgmates.app.data.journal.model.JournalEntry
import com.msgmates.app.data.journal.model.JournalType
import com.msgmates.app.data.journal.model.WatchedUser
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@Singleton
class JournalRepository @Inject constructor() {

    private val _userStories = MutableStateFlow<List<JournalEntry>>(emptyList())
    val userStories: Flow<List<JournalEntry>> = _userStories.asStateFlow()

    private val _mutedUserIds = MutableStateFlow<Set<String>>(emptySet())
    val mutedUserIds: Flow<Set<String>> = _mutedUserIds.asStateFlow()

    private val _watchedUsers = MutableStateFlow<List<WatchedUser>>(emptyList())
    val watchedUsers: Flow<List<WatchedUser>> = _watchedUsers.asStateFlow()

    init {
        // Mock data ile başlat
        loadMockData()
    }

    fun addStory(entry: JournalEntry) {
        val currentStories = _userStories.value.toMutableList()
        currentStories.add(0, entry) // En yeni en üstte
        _userStories.value = currentStories
    }

    fun addStory(
        type: JournalType,
        contentUrl: String,
        textContent: String,
        durationHours: Int
    ) {
        val newEntry = JournalEntry(
            id = System.currentTimeMillis().toString(),
            userId = "current_user",
            userName = "Sen",
            profileImageUrl = "",
            hasNewStory = true,
            type = type,
            contentUrl = contentUrl,
            textContent = textContent,
            createdAt = System.currentTimeMillis(),
            durationHours = durationHours
        )
        addStory(newEntry)
    }

    fun deleteStory(id: String) {
        val currentStories = _userStories.value.toMutableList()
        currentStories.removeAll { it.id == id }
        _userStories.value = currentStories
    }

    fun muteUser(userId: String) {
        val currentMuted = _mutedUserIds.value.toMutableSet()
        currentMuted.add(userId)
        _mutedUserIds.value = currentMuted
    }

    fun unmuteUser(userId: String) {
        val currentMuted = _mutedUserIds.value.toMutableSet()
        currentMuted.remove(userId)
        _mutedUserIds.value = currentMuted
    }

    fun isMuted(userId: String): Boolean {
        return userId in _mutedUserIds.value
    }

    fun getMutedUsers(): Flow<Set<String>> = mutedUserIds

    fun onStoryViewed(
        userId: String,
        userName: String,
        profileImageUrl: String,
        viewedAt: Long = System.currentTimeMillis()
    ) {
        val currentWatched = _watchedUsers.value.toMutableList()
        val existingIndex = currentWatched.indexOfFirst { it.userId == userId }

        if (existingIndex >= 0) {
            // Güncelle
            currentWatched[existingIndex] = currentWatched[existingIndex].copy(lastWatchedAt = viewedAt)
        } else {
            // Ekle
            currentWatched.add(WatchedUser(userId, userName, profileImageUrl, viewedAt))
        }

        // Son izlenme zamanına göre sırala (en yeni en üstte)
        currentWatched.sortByDescending { it.lastWatchedAt }
        _watchedUsers.value = currentWatched
    }

    private fun loadMockData() {
        val mockStories = listOf(
            JournalEntry(
                id = "1",
                userId = "user1",
                userName = "Ahmet Yılmaz",
                profileImageUrl = "https://via.placeholder.com/150",
                hasNewStory = true,
                type = com.msgmates.app.data.journal.model.JournalType.PHOTO,
                contentUrl = "https://via.placeholder.com/400x600",
                createdAt = System.currentTimeMillis() - 3600000, // 1 saat önce
                durationHours = 24
            ),
            JournalEntry(
                id = "2",
                userId = "user2",
                userName = "Ayşe Demir",
                profileImageUrl = "https://via.placeholder.com/150",
                hasNewStory = true,
                type = com.msgmates.app.data.journal.model.JournalType.VIDEO,
                contentUrl = "https://via.placeholder.com/400x600",
                createdAt = System.currentTimeMillis() - 7200000, // 2 saat önce
                durationHours = 12
            ),
            JournalEntry(
                id = "3",
                userId = "user3",
                userName = "Mehmet Kaya",
                profileImageUrl = "https://via.placeholder.com/150",
                hasNewStory = false,
                type = com.msgmates.app.data.journal.model.JournalType.TEXT,
                textContent = "Bugün harika bir gün geçirdim! 🎉",
                createdAt = System.currentTimeMillis() - 10800000, // 3 saat önce
                durationHours = 6
            ),
            JournalEntry(
                id = "4",
                userId = "user4",
                userName = "Fatma Öz",
                profileImageUrl = "https://via.placeholder.com/150",
                hasNewStory = true,
                type = com.msgmates.app.data.journal.model.JournalType.AUDIO,
                contentUrl = "https://via.placeholder.com/400x600",
                createdAt = System.currentTimeMillis() - 14400000, // 4 saat önce
                durationHours = 48
            ),
            JournalEntry(
                id = "5",
                userId = "user5",
                userName = "Ali Veli",
                profileImageUrl = "https://via.placeholder.com/150",
                hasNewStory = false,
                type = com.msgmates.app.data.journal.model.JournalType.PHOTO,
                contentUrl = "https://via.placeholder.com/400x600",
                createdAt = System.currentTimeMillis() - 18000000, // 5 saat önce
                durationHours = 24
            )
        )
        _userStories.value = mockStories
    }
}
