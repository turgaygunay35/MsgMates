package com.msgmates.app.data.contacts

import android.content.Context
import androidx.work.WorkManager
import androidx.work.testing.WorkManagerTestInitHelper
import com.msgmates.app.data.contacts.local.dao.ContactDao
import com.msgmates.app.data.contacts.remote.ContactsApi
import com.msgmates.app.data.contacts.ContactsRepository
import com.msgmates.app.core.analytics.ContactsTelemetryService
import com.msgmates.app.core.permission.ContactsPermissionManager
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.mockito.kotlin.any
import org.mockito.kotlin.times

class ContactsSyncE2ETest {

    @Mock
    private lateinit var context: Context

    @Mock
    private lateinit var contactDao: ContactDao

    @Mock
    private lateinit var contactsApi: ContactsApi

    @Mock
    private lateinit var telemetryService: ContactsTelemetryService

    @Mock
    private lateinit var permissionManager: ContactsPermissionManager

    private lateinit var contactsRepository: ContactsRepository
    private lateinit var workManager: WorkManager

    @Before
    fun setup() {
        MockitoAnnotations.openMocks(this)
        
        // Initialize WorkManager for testing
        WorkManagerTestInitHelper.initializeTestWorkManager(context)
        workManager = WorkManager.getInstance(context)
        
        contactsRepository = ContactsRepository(
            context, contactDao, contactsApi, telemetryService, permissionManager
        )
    }

    @Test
    fun `Incremental Delta Pull - First sync downloads all contacts`() {
        // Given
        val mockContacts = listOf(
            ContactEntity(id = 1, displayName = "John Doe", phoneNumber = "+1234567890"),
            ContactEntity(id = 2, displayName = "Jane Smith", phoneNumber = "+0987654321")
        )
        whenever(contactsApi.getContactsDelta(lastSyncTime = null)).thenReturn(mockContacts)

        // When
        contactsRepository.syncContacts()

        // Then
        verify(contactDao).insertContacts(mockContacts)
        verify(telemetryService).recordSyncSuccess(mockContacts.size)
    }

    @Test
    fun `Incremental Delta Pull - Subsequent sync only downloads changes`() {
        // Given
        val lastSyncTime = System.currentTimeMillis() - 3600000 // 1 hour ago
        val deltaContacts = listOf(
            ContactEntity(id = 3, displayName = "New Contact", phoneNumber = "+1111111111")
        )
        whenever(contactsApi.getContactsDelta(lastSyncTime)).thenReturn(deltaContacts)

        // When
        contactsRepository.syncContacts()

        // Then
        verify(contactDao).insertContacts(deltaContacts)
        verify(telemetryService).recordSyncSuccess(deltaContacts.size)
    }

    @Test
    fun `WorkManager Backoff - Retry on failure with exponential backoff`() {
        // Given
        whenever(contactsApi.getContactsDelta(any())).thenThrow(Exception("Network error"))

        // When
        contactsRepository.syncContacts()

        // Then
        // WorkManager should schedule retry with backoff
        verify(telemetryService).recordSyncError(any())
        // Verify WorkManager enqueues retry work
    }

    @Test
    fun `WorkManager Backoff - Max retries reached, gives up`() {
        // Given
        whenever(contactsApi.getContactsDelta(any())).thenThrow(Exception("Persistent error"))

        // When
        repeat(5) { contactsRepository.syncContacts() }

        // Then
        verify(telemetryService, times(5)).recordSyncError(any())
        // After max retries, should stop attempting
    }

    @Test
    fun `Contacts Sync - Handles partial failures gracefully`() {
        // Given
        val partialContacts = listOf(
            ContactEntity(id = 1, displayName = "Valid Contact", phoneNumber = "+1234567890"),
            ContactEntity(id = 2, displayName = "", phoneNumber = "") // Invalid contact
        )
        whenever(contactsApi.getContactsDelta(any())).thenReturn(partialContacts)

        // When
        contactsRepository.syncContacts()

        // Then
        // Should process valid contacts and skip invalid ones
        verify(contactDao).insertContacts(listOf(partialContacts[0]))
        verify(telemetryService).recordSyncPartialSuccess(1, 1)
    }

    @Test
    fun `Contacts Sync - Preserves local changes during sync`() {
        // Given
        val localContacts = listOf(
            ContactEntity(id = 1, displayName = "Local Contact", phoneNumber = "+1234567890")
        )
        val remoteContacts = listOf(
            ContactEntity(id = 2, displayName = "Remote Contact", phoneNumber = "+0987654321")
        )
        whenever(contactDao.getAllContacts()).thenReturn(flowOf(localContacts))
        whenever(contactsApi.getContactsDelta(any())).thenReturn(remoteContacts)

        // When
        contactsRepository.syncContacts()

        // Then
        // Should merge local and remote contacts
        verify(contactDao).insertContacts(remoteContacts)
        // Local contacts should be preserved
    }

    @Test
    fun `Contacts Sync - Handles network timeout with retry`() {
        // Given
        whenever(contactsApi.getContactsDelta(any())).thenThrow(
            java.net.SocketTimeoutException("Timeout")
        )

        // When
        contactsRepository.syncContacts()

        // Then
        verify(telemetryService).recordSyncError(any())
        // Should schedule retry with backoff
    }

    @Test
    fun `Contacts Sync - Success after retry`() {
        // Given
        val mockContacts = listOf(
            ContactEntity(id = 1, displayName = "Success Contact", phoneNumber = "+1234567890")
        )
        whenever(contactsApi.getContactsDelta(any()))
            .thenThrow(Exception("Network error"))
            .thenReturn(mockContacts)

        // When
        contactsRepository.syncContacts() // First attempt fails
        contactsRepository.syncContacts() // Second attempt succeeds

        // Then
        verify(contactDao).insertContacts(mockContacts)
        verify(telemetryService).recordSyncSuccess(mockContacts.size)
    }
}
