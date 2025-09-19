package com.msgmates.app.data.chats

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.msgmates.app.domain.chats.ChatFilter
import com.msgmates.app.domain.chats.Conversation
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

/**
 * Repository for managing conversations data.
 * Currently uses stub data with random generation for testing purposes.
 */
@Singleton
class ChatsRepository @Inject constructor() {

    private val random = Random()
    // Sample data kaldırıldı - gerçek API'den gelecek

    /**
     * Get paged conversations based on filter
     */
    fun pagedConversations(filter: ChatFilter): Flow<PagingData<Conversation>> {
        val conversations = generateConversations(filter)
        return flowOf(PagingData.from(conversations))
    }

    /**
     * Mute or unmute a conversation
     */
    suspend fun mute(id: String, mute: Boolean) {
        delay(200) // Simulate network delay
        // In real implementation, this would update the database
    }

    /**
     * Archive a conversation
     */
    suspend fun archive(id: String) {
        delay(200) // Simulate network delay
        // In real implementation, this would update the database
    }

    /**
     * Delete a conversation
     */
    suspend fun delete(id: String) {
        delay(200) // Simulate network delay
        // In real implementation, this would update the database
    }

    /**
     * Get unread count for a specific filter
     */
    fun getUnreadCount(filter: ChatFilter): Flow<Int> = flow {
        val conversations = generateConversations(filter)
        val count = conversations.sumOf { it.unreadCount }
        emit(count)
    }

    /**
     * Generate conversations for a specific page
     */
    fun generateConversationsForPage(filter: ChatFilter, page: Int, pageSize: Int): List<Conversation> {
        val allConversations = generateConversations(filter)
        val startIndex = page * pageSize
        val endIndex = minOf(startIndex + pageSize, allConversations.size)

        return if (startIndex >= allConversations.size) {
            emptyList()
        } else {
            allConversations.subList(startIndex, endIndex)
        }
    }

    /**
     * Generate sample conversations based on filter
     */
    private fun generateConversations(filter: ChatFilter): List<Conversation> {
        // Sample data kaldırıldı - gerçek API'den gelecek
        return emptyList()
    }

    /**
     * Check if conversation matches the given filter
     */
    private fun matchesFilter(conversation: Conversation, filter: ChatFilter): Boolean {
        return when (filter) {
            ChatFilter.ALL -> true
            ChatFilter.UNREAD -> conversation.hasUnreadMessages()
            ChatFilter.GROUPS -> conversation.isGroup
            ChatFilter.FAVORITES -> false // TODO: Implement favorites
            ChatFilter.ARCHIVED -> false // TODO: Implement archived
            ChatFilter.DISASTER -> false // TODO: Implement disaster mode
        }
    }
}

/**
 * PagingSource for conversations
 */
class ConversationsPagingSource(
    private val filter: ChatFilter,
    private val repository: ChatsRepository
) : PagingSource<Int, Conversation>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Conversation> {
        return try {
            val page = params.key ?: 0
            val pageSize = params.loadSize

            // Simulate network delay
            delay(500)

            // Generate conversations for this page
            val conversations = repository.generateConversationsForPage(filter, page, pageSize)

            LoadResult.Page(
                data = conversations,
                prevKey = if (page == 0) null else page - 1,
                nextKey = if (conversations.isEmpty()) null else page + 1
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Conversation>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }
}
