package com.msgmates.app.data.contacts

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.database.ContentObserver
import android.net.Uri
import android.provider.ContactsContract
import androidx.core.content.ContextCompat
import com.msgmates.app.analytics.ContactsTelemetryService
import com.msgmates.app.core.datastore.ContactsPreferences
import com.msgmates.app.data.contacts.local.dao.ContactDao
import com.msgmates.app.data.contacts.local.dao.PhoneDao
import com.msgmates.app.data.contacts.local.entity.ContactEntity
import com.msgmates.app.data.contacts.local.entity.PhoneEntity
import com.msgmates.app.data.contacts.mapper.ContactMapper
import com.msgmates.app.data.contacts.remote.api.ContactsApiService
import com.msgmates.app.data.contacts.remote.model.*
import com.msgmates.app.domain.contacts.model.Contact
import com.msgmates.app.domain.contacts.model.ContactPresence
import com.msgmates.app.util.PhoneNormalizer
import com.msgmates.app.util.Result
import dagger.hilt.android.qualifiers.ApplicationContext
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import timber.log.Timber

@Singleton
class ContactsRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val contentResolver: ContentResolver,
    private val contactDao: ContactDao,
    private val phoneDao: PhoneDao,
    private val apiService: ContactsApiService,
    private val contactsPreferences: ContactsPreferences,
    private val telemetryService: ContactsTelemetryService
) {

    private var contentObserver: ContentObserver? = null

    init {
        tryRegisterObserverIfPermitted()
    }

    private fun hasReadContactsPermission(): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) ==
            PackageManager.PERMISSION_GRANTED

    private fun buildContentObserver(): ContentObserver =
        object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                Timber.d("Contacts changed, refreshing data")

                // Basit implementasyon - sadece local güncelleme
                GlobalScope.launch(Dispatchers.IO) {
                    try {
                        updateLocalContactsOnly()
                    } catch (e: Exception) {
                        Timber.w(e, "Failed to handle contacts change")
                    }
                }
            }
        }

    private fun tryRegisterObserverIfPermitted() {
        if (!hasReadContactsPermission()) return
        if (contentObserver == null) contentObserver = buildContentObserver()
        try {
            contentResolver.registerContentObserver(
                ContactsContract.Contacts.CONTENT_URI,
                true,
                contentObserver!!
            )
        } catch (e: SecurityException) {
            Timber.w(e, "Failed to register contacts observer - permission may have been revoked")
        }
    }

    fun startObservingIfPermitted() {
        tryRegisterObserverIfPermitted()
    }

    private fun setupContentObserver() {
        contentObserver = object : ContentObserver(null) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                super.onChange(selfChange, uri)
                Timber.d("Contacts changed, refreshing data")
                // In a real implementation, you might want to refresh specific contacts
                // For now, we'll let the UI handle refresh through user action
            }
        }

        contentResolver.registerContentObserver(
            ContactsContract.Contacts.CONTENT_URI,
            true,
            contentObserver!!
        )
    }

    fun getAllContacts(): Flow<List<Contact>> {
        return contactDao.getAllContacts().map { entities ->
            entities.map { ContactMapper.mapToDomain(it) }
        }
    }

    fun getContactById(contactId: Long): Flow<Contact?> {
        return contactDao.getContactWithPhonesById(contactId).map { contactWithPhones ->
            contactWithPhones?.let { ContactMapper.mapToDomain(it) }
        }
    }

    fun searchContacts(query: String): Flow<List<Contact>> {
        return if (query.isBlank()) {
            getAllContacts()
        } else {
            // Normalize search query for diacritic-insensitive search
            val normalizedQuery = com.msgmates.app.util.DiacriticRemover.normalizeForSearch(query)

            // Use FTS for better performance with large datasets
            val searchFlow = if (normalizedQuery.length >= 2) {
                // Use FTS for queries with 2+ characters
                contactDao.searchContactsFts(normalizedQuery)
            } else {
                // Use LIKE search for single character queries
                contactDao.searchContacts(normalizedQuery)
            }

            searchFlow.map { entities ->
                entities.map { ContactMapper.mapToDomain(it) }
            }
        }
    }

    fun getFavoriteContacts(): Flow<List<Contact>> {
        return contactDao.getFavoriteContacts().map { entities ->
            entities.map { ContactMapper.mapToDomain(it) }
        }
    }

    fun getMsgMatesContacts(): Flow<List<Contact>> {
        return contactDao.getMsgMatesContacts().map { entities ->
            entities.map { ContactMapper.mapToDomain(it) }
        }
    }

    fun getFilteredContacts(
        favoritesOnly: Boolean = false,
        msgMatesOnly: Boolean = false
    ): Flow<List<Contact>> {
        return contactDao.getFilteredContacts(favoritesOnly, msgMatesOnly).map { entities ->
            entities.map { ContactMapper.mapToDomain(it) }
        }
    }

    suspend fun toggleFavorite(contactId: Long): Result<Boolean> {
        return try {
            val contact = contactDao.getContactById(contactId)
            if (contact != null) {
                val newFavoriteStatus = !contact.favorite
                contactDao.updateFavoriteStatus(contactId, newFavoriteStatus)
                Result.Success(newFavoriteStatus)
            } else {
                Result.Error(Exception("Contact not found"))
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to toggle favorite for contact $contactId")
            Result.Error(e)
        }
    }

    suspend fun refreshContacts(force: Boolean = false): Result<Unit> {
        val startTime = System.currentTimeMillis()
        return try {
            if (force || contactDao.getContactCount() == 0) {
                loadContactsFromDevice()
                // Server eşleştirmesini background'a al - UI'yi bloklamasın
                // Note: Bu işlem suspend fonksiyon olarak çağrılmalı, GlobalScope kullanımı önerilmez
                try {
                    val matchResult = matchContactsWithServer()
                    if (matchResult is Result.Success) {
                        val duration = System.currentTimeMillis() - startTime
                        telemetryService.recordSyncDuration(duration)
                    }
                } catch (e: Exception) {
                    Timber.w(e, "Background contact matching failed")
                    telemetryService.recordErrorCode("SYNC_ERROR_${e.javaClass.simpleName}")
                }
            }
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to refresh contacts")
            telemetryService.recordErrorCode("REFRESH_ERROR_${e.javaClass.simpleName}")
            Result.Error(e)
        }
    }

    private suspend fun loadContactsFromDevice(): Result<Unit> {
        if (!hasReadContactsPermission()) {
            Timber.w("READ_CONTACTS not granted; skipping device contact load")
            return Result.Success(Unit)
        }

        return withContext(Dispatchers.IO) {
            try {
                val contacts = mutableListOf<ContactEntity>()
                val phones = mutableListOf<PhoneEntity>()

                val cursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI,
                    arrayOf(
                        ContactsContract.Contacts._ID,
                        ContactsContract.Contacts.DISPLAY_NAME,
                        ContactsContract.Contacts.PHOTO_URI
                    ),
                    null,
                    null,
                    ContactsContract.Contacts.DISPLAY_NAME + " ASC"
                )

                cursor?.use { c ->
                    while (c.moveToNext()) {
                        val contactId = c.getLong(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID))
                        val displayName = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME))
                        val photoUri = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts.PHOTO_URI))

                        val contact = ContactEntity(
                            id = contactId,
                            displayName = displayName ?: "Unknown",
                            photoUri = photoUri,
                            normalizedPrimary = com.msgmates.app.util.DiacriticRemover.normalizeForSearch(
                                displayName ?: "Unknown"
                            )
                        )
                        contacts.add(contact)
                    }
                }

                // Tüm telefonları tek sorguda al (N+1 problemini çöz)
                loadAllPhonesForContacts(contacts.map { it.id }, phones)

                // Insert contacts and phones
                contactDao.insertContacts(contacts)
                phoneDao.insertPhones(phones)

                Timber.d("Loaded ${contacts.size} contacts and ${phones.size} phones from device")
                Result.Success(Unit)
            } catch (e: Exception) {
                Timber.e(e, "Failed to load contacts from device")
                Result.Error(e)
            }
        }
    }

    private suspend fun loadAllPhonesForContacts(contactIds: List<Long>, phones: MutableList<PhoneEntity>) {
        if (contactIds.isEmpty()) return

        val placeholders = contactIds.joinToString(",") { "?" }
        val phoneCursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone._ID,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL
            ),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} IN ($placeholders)",
            contactIds.map { it.toString() }.toTypedArray(),
            null
        )

        phoneCursor?.use { c ->
            while (c.moveToNext()) {
                val contactId = c.getLong(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.CONTACT_ID))
                val phoneId = c.getLong(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone._ID))
                val rawNumber = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER))
                val type = c.getInt(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.TYPE))
                val label = c.getString(c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.LABEL))

                val normalizedE164 = PhoneNormalizer.normalizeToE164(rawNumber ?: "")

                val phone = PhoneEntity(
                    id = phoneId,
                    contactId = contactId,
                    rawNumber = rawNumber ?: "",
                    normalizedE164 = normalizedE164,
                    type = getPhoneTypeString(type),
                    label = label
                )
                phones.add(phone)
            }
        }
    }

    private suspend fun loadPhonesForContact(contactId: Long, phones: MutableList<PhoneEntity>) {
        // Bu metod artık kullanılmıyor, loadAllPhonesForContacts kullanılıyor
        loadAllPhonesForContacts(listOf(contactId), phones)
    }

    private fun getPhoneTypeString(type: Int): String {
        return when (type) {
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME -> "HOME"
            ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE -> "MOBILE"
            ContactsContract.CommonDataKinds.Phone.TYPE_WORK -> "WORK"
            ContactsContract.CommonDataKinds.Phone.TYPE_OTHER -> "OTHER"
            else -> "UNKNOWN"
        }
    }

    private suspend fun matchContactsWithServer(): Result<Unit> {
        return try {
            // Get salt from server
            val saltResponse = apiService.getSalt()
            val salt = saltResponse.salt

            // Get all normalized phone numbers
            val allPhones = phoneDao.getAllPhonesSync()
            val normalizedNumbers = allPhones.mapNotNull { it.normalizedE164 }

            if (normalizedNumbers.isEmpty()) {
                return Result.Success(Unit)
            }

            // Create hashes
            val hashes = normalizedNumbers.map { number ->
                val input = salt + number
                val digest = MessageDigest.getInstance("SHA-256")
                val hashBytes = digest.digest(input.toByteArray())
                hashBytes.joinToString("") { "%02x".format(it) }
            }

            // Match with server
            val matchRequest = MatchRequest(hashes)
            val matchResponse = apiService.matchContacts(matchRequest)

            // Record batch size for telemetry
            telemetryService.recordBatchSize(normalizedNumbers.size)

            // Update local database
            matchResponse.matches.forEach { match ->
                val phone = allPhones.find { phone ->
                    val input = salt + phone.normalizedE164
                    val digest = MessageDigest.getInstance("SHA-256")
                    val hashBytes = digest.digest(input.toByteArray())
                    val hash = hashBytes.joinToString("") { "%02x".format(it) }
                    hash == match.hash
                }

                phone?.let {
                    contactDao.updateMsgMatesStatusByNumber(it.normalizedE164!!, match.isUser)
                }
            }

            Timber.d("Matched ${matchResponse.matches.size} contacts with server")
            Result.Success(Unit)
        } catch (e: Exception) {
            Timber.e(e, "Failed to match contacts with server")
            Result.Error(e)
        }
    }

    suspend fun getPresence(contactIds: List<Long>): Result<List<ContactPresence>> {
        return try {
            val now = System.currentTimeMillis()
            val ttlMs = 60_000L // 60 saniye TTL

            val contacts = contactDao.getContactsByIds(contactIds)
            val contactsToUpdate = mutableListOf<Long>()

            // TTL kontrolü - sadece TTL süresi dolmuş kişiler için API çağrısı yap
            contacts.forEach { contact ->
                val lastSeen = contact.lastSeenEpoch ?: 0L
                if (now - lastSeen > ttlMs) {
                    contactsToUpdate.add(contact.id)
                }
            }

            if (contactsToUpdate.isNotEmpty()) {
                // Sadece TTL süresi dolmuş kişiler için presence API çağrısı yap
                // ContactId'leri hash'e çevir (burada basitleştiriyoruz, gerçekte hash mapping gerekir)
                val contactHashes = contactsToUpdate.map { it.toString() }
                val presenceRequest = PresenceRequest(contactHashes)
                val presenceResponse = apiService.getPresence(presenceRequest)

                // Güncel presence bilgilerini Room'a kaydet
                presenceResponse.presence?.forEach { presence ->
                    // Hash'i contactId'ye çevir (basitleştirilmiş)
                    val contactId = presence.hash.toLongOrNull()
                    if (contactId != null) {
                        contactDao.updatePresence(
                            contactId = contactId,
                            lastSeen = presence.lastSeenEpoch,
                            isOnline = presence.online
                        )
                    }
                }
            }

            // Tüm kişilerin güncel presence bilgilerini döndür
            val allContacts = contactDao.getContactsByIds(contactIds)
            val presences = allContacts.map { contact ->
                ContactPresence(
                    contactId = contact.id,
                    isOnline = contact.presenceOnline ?: false,
                    lastSeenEpoch = contact.lastSeenEpoch
                )
            }

            Result.Success(presences)
        } catch (e: Exception) {
            Timber.e(e, "Failed to get presence")
            Result.Error(e)
        }
    }

    internal suspend fun updateLocalContactsOnly() {
        try {
            loadContactsFromDevice()
            Timber.d("Local contacts updated without sync")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update local contacts")
        }
    }

    private suspend fun updateLocalContactsAndSync() {
        try {
            loadContactsFromDevice()
            // Background sync - don't block UI
            GlobalScope.launch(Dispatchers.IO) {
                try {
                    matchContactsWithServer()
                } catch (e: Exception) {
                    Timber.w(e, "Background sync failed after contacts change")
                }
            }
            Timber.d("Local contacts updated with background sync")
        } catch (e: Exception) {
            Timber.e(e, "Failed to update local contacts and sync")
        }
    }

    suspend fun getLastSyncTime(): Long? {
        return try {
            contactDao.getOldestSyncTime()
        } catch (e: Exception) {
            Timber.e(e, "Failed to get last sync time")
            null
        }
    }

    suspend fun updateLastSyncTime() {
        try {
            val currentTime = System.currentTimeMillis()
            contactDao.updateAllLastSyncTime(currentTime)
        } catch (e: Exception) {
            Timber.e(e, "Failed to update last sync time")
        }
    }

    fun cleanup() {
        contentObserver?.let {
            try {
                contentResolver.unregisterContentObserver(it)
            } catch (e: SecurityException) {
                Timber.w(e, "Failed to unregister contacts observer")
            }
        }
    }
}
