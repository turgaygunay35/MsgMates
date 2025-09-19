package com.msgmates.app.data.contacts.local.dao

import androidx.room.*
import com.msgmates.app.data.contacts.local.entity.ContactEntity
import com.msgmates.app.data.contacts.local.entity.ContactWithPhones
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {

    @Query("SELECT * FROM contacts ORDER BY displayName COLLATE NOCASE ASC")
    fun getAllContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE id = :contactId")
    suspend fun getContactById(contactId: Long): ContactEntity?

    @Transaction
    @Query("SELECT * FROM contacts WHERE id = :contactId")
    fun getContactWithPhonesById(contactId: Long): Flow<ContactWithPhones?>

    @Query("SELECT * FROM contacts WHERE favorite = 1 ORDER BY displayName COLLATE NOCASE ASC")
    fun getFavoriteContacts(): Flow<List<ContactEntity>>

    @Query("SELECT * FROM contacts WHERE isMsgMatesUser = 1 ORDER BY displayName COLLATE NOCASE ASC")
    fun getMsgMatesContacts(): Flow<List<ContactEntity>>

    @Query(
        """
        SELECT * FROM contacts 
        WHERE displayName LIKE '%' || :query || '%' 
        OR normalizedPrimary LIKE '%' || :query || '%'
        ORDER BY displayName COLLATE NOCASE ASC
    """
    )
    fun searchContacts(query: String): Flow<List<ContactEntity>>

    @Query(
        """
        SELECT * FROM contacts 
        JOIN contacts_fts ON contacts.id = contacts_fts.rowid
        WHERE contacts_fts MATCH :query
        ORDER BY displayName COLLATE NOCASE ASC
    """
    )
    fun searchContactsFts(query: String): Flow<List<ContactEntity>>

    @Query(
        """
        SELECT * FROM contacts 
        WHERE (:favoritesOnly = 0 OR favorite = 1)
        AND (:msgMatesOnly = 0 OR isMsgMatesUser = 1)
        ORDER BY displayName COLLATE NOCASE ASC
    """
    )
    fun getFilteredContacts(
        favoritesOnly: Boolean = false,
        msgMatesOnly: Boolean = false
    ): Flow<List<ContactEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: ContactEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContacts(contacts: List<ContactEntity>)

    @Update
    suspend fun updateContact(contact: ContactEntity)

    @Query("UPDATE contacts SET favorite = :isFavorite WHERE id = :contactId")
    suspend fun updateFavoriteStatus(contactId: Long, isFavorite: Boolean)

    @Query("UPDATE contacts SET isMsgMatesUser = :isMsgMatesUser WHERE id = :contactId")
    suspend fun updateMsgMatesStatus(contactId: Long, isMsgMatesUser: Boolean)

    @Query("UPDATE contacts SET lastSeenEpoch = :lastSeen, presenceOnline = :isOnline WHERE id = :contactId")
    suspend fun updatePresence(contactId: Long, lastSeen: Long?, isOnline: Boolean?)

    @Query("UPDATE contacts SET isMsgMatesUser = :isMsgMatesUser WHERE normalizedPrimary = :normalizedNumber")
    suspend fun updateMsgMatesStatusByNumber(normalizedNumber: String, isMsgMatesUser: Boolean)

    @Query("DELETE FROM contacts WHERE id = :contactId")
    suspend fun deleteContact(contactId: Long)

    @Query("DELETE FROM contacts")
    suspend fun deleteAllContacts()

    @Query("SELECT COUNT(*) FROM contacts")
    suspend fun getContactCount(): Int

    @Query("SELECT COUNT(*) FROM contacts WHERE isMsgMatesUser = 1")
    suspend fun getMsgMatesContactCount(): Int

    @Query("SELECT * FROM contacts WHERE normalizedPrimary = :phoneNumber")
    suspend fun findByNormalizedPrimary(phoneNumber: String): ContactEntity?

    @Query("SELECT * FROM contacts WHERE id IN (:contactIds)")
    suspend fun getContactsByIds(contactIds: List<Long>): List<ContactEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(contact: ContactEntity): Long

    @Query("UPDATE contacts SET lastSyncAt = :syncTime WHERE id = :contactId")
    suspend fun updateLastSyncTime(contactId: Long, syncTime: Long)

    @Query("UPDATE contacts SET lastSyncAt = :syncTime")
    suspend fun updateAllLastSyncTime(syncTime: Long)

    @Query("SELECT MAX(lastSyncAt) FROM contacts WHERE lastSyncAt IS NOT NULL")
    suspend fun getLastSyncTime(): Long?

    @Query("SELECT MIN(lastSyncAt) FROM contacts WHERE lastSyncAt IS NOT NULL")
    suspend fun getOldestSyncTime(): Long?
}
