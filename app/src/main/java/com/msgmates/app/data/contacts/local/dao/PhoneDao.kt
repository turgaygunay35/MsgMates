package com.msgmates.app.data.contacts.local.dao

import androidx.room.*
import com.msgmates.app.data.contacts.local.entity.PhoneEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PhoneDao {

    @Query("SELECT * FROM phones WHERE contactId = :contactId ORDER BY id ASC")
    fun getPhonesForContact(contactId: Long): Flow<List<PhoneEntity>>

    @Query("SELECT * FROM phones WHERE contactId = :contactId")
    suspend fun getPhonesForContactSync(contactId: Long): List<PhoneEntity>

    @Query("SELECT * FROM phones WHERE normalizedE164 = :normalizedNumber")
    suspend fun getPhoneByNormalizedNumber(normalizedNumber: String): PhoneEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhone(phone: PhoneEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPhones(phones: List<PhoneEntity>)

    @Update
    suspend fun updatePhone(phone: PhoneEntity)

    @Delete
    suspend fun deletePhone(phone: PhoneEntity)

    @Query("DELETE FROM phones WHERE contactId = :contactId")
    suspend fun deletePhonesForContact(contactId: Long)

    @Query("DELETE FROM phones")
    suspend fun deleteAllPhones()

    @Query("SELECT COUNT(*) FROM phones WHERE contactId = :contactId")
    suspend fun getPhoneCountForContact(contactId: Long): Int

    @Query("SELECT * FROM phones")
    suspend fun getAllPhonesSync(): List<PhoneEntity>
}
