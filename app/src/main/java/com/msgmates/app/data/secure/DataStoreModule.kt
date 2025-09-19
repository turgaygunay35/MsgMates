package com.msgmates.app.data.secure

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    // DataStore will be created in provideTokenStoreDataStore

    @Provides
    @Singleton
    fun provideTokenStoreDataStore(
        @ApplicationContext context: Context,
        tokenStoreSerializer: TokenStoreSerializer
    ): DataStore<TokenStore> {
        return androidx.datastore.core.DataStoreFactory.create(
            serializer = tokenStoreSerializer,
            produceFile = {
                java.io.File(context.filesDir, "datastore/secure_token_store.pb")
            }
        )
    }

    @Provides
    @Singleton
    fun provideSecureTokenStore(
        dataStore: DataStore<TokenStore>
    ): SecureTokenStore = DataStoreTokenStore(dataStore)
}
