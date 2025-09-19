package com.msgmates.app.core.di

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.msgmates.app.BuildConfig
import com.msgmates.app.core.audio.AudioPlayer
import com.msgmates.app.core.audio.AudioRecorder
import com.msgmates.app.core.auth.TokenRepository
import com.msgmates.app.core.auth.TokenRepositoryImpl
import com.msgmates.app.core.auth.remote.AuthApi
import com.msgmates.app.core.call.CallSignalingManager
import com.msgmates.app.core.datastore.AuthTokenStore
import com.msgmates.app.core.download.AttachmentDownloader
import com.msgmates.app.core.env.EnvConfig
import com.msgmates.app.core.messaging.FloodProtection
import com.msgmates.app.core.messaging.ReceiptBatcher
import com.msgmates.app.core.messaging.remote.MessagingApi
import com.msgmates.app.core.messaging.worker.OutboxWorkManager
import com.msgmates.app.core.metrics.MetricsCollector
import com.msgmates.app.core.network.AuthInterceptor
import com.msgmates.app.core.network.CacheControlInterceptor
import com.msgmates.app.core.network.NetworkConfig
import com.msgmates.app.core.network.RefreshAuthenticator
import com.msgmates.app.core.network.RequestIdInterceptor
import com.msgmates.app.core.notification.ChatNotification
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.msgmates.app.core.secure.SecureStore
import com.msgmates.app.core.secure.SecureStoreImpl
import com.msgmates.app.core.typing.TypingManager
import com.msgmates.app.core.upload.AttachmentUploader
import com.msgmates.app.core.upload.UploadApi
import com.msgmates.app.data.remote.auth.AuthApiService
import com.msgmates.app.data.repository.auth.AuthRepository
import com.msgmates.app.data.repository.auth.AuthRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // ---- LOGGING ----
    @Provides @Singleton
    fun provideLogger(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    // ---- JSON (kotlinx-serialization) ----
    @Provides @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    // ---- DEFAULT HEADERS ----
    @Provides @Singleton
    fun provideDefaultHeaders(): Interceptor = Interceptor { chain ->
        val req = chain.request().newBuilder()
            .header("Accept", "application/json")
            .build()
        chain.proceed(req)
    }

    // ---- REQUEST ID INTERCEPTOR ----
    @Provides @Singleton
    fun provideRequestIdInterceptor(): com.msgmates.app.core.network.RequestIdInterceptor = com.msgmates.app.core.network.RequestIdInterceptor()

    // ---- REFRESH (AYRI) MÜŞTERİ/RETROFIT (token yenileme için) ----
    @Provides @Singleton @Named("refreshOkHttp")
    fun provideRefreshOkHttp(logger: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor(logger)
            .build()

    @Provides @Singleton @Named("refreshRetrofit")
    fun provideRefreshRetrofit(
        @Named("refreshOkHttp") client: OkHttpClient,
        json: Json
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(client)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // ---- REFRESH API ----
    @Provides @Singleton
    fun provideAuthApi(@Named("refreshRetrofit") retrofit: Retrofit): AuthApi =
        retrofit.create(AuthApi::class.java)

    // ---- TOKEN READ ONLY PROVIDER ----
    @Provides @Singleton
    fun provideTokenReadOnlyProvider(
        tokenRepo: TokenRepository
    ): com.msgmates.app.core.auth.TokenReadOnlyProvider = tokenRepo

    // ---- TOKEN REFRESHER ----
    @Provides @Singleton
    fun provideTokenRefresher(
        @Named("refreshRetrofit") retrofit: Retrofit
    ): com.msgmates.app.core.auth.TokenRefresher {
        val authApi = retrofit.create(com.msgmates.app.data.remote.auth.AuthApiService::class.java)
        return com.msgmates.app.core.auth.TokenRefresherImpl(authApi)
    }

    // ---- REFRESH COORDINATOR ----
    @Provides @Singleton
    fun provideRefreshCoordinator(
        tokenRepo: TokenRepository,
        authRepo: AuthRepository
    ): com.msgmates.app.core.auth.RefreshCoordinator {
        return com.msgmates.app.core.auth.RefreshCoordinator(tokenRepo, authRepo)
    }

    // ---- AUTHENTICATOR ----
    @Provides
    @Singleton
    fun provideAuthenticator(
        tokenProvider: com.msgmates.app.core.auth.TokenReadOnlyProvider,
        tokenRefresher: com.msgmates.app.core.auth.TokenRefresher,
        tokenRepository: TokenRepository,
        metrics: com.msgmates.app.core.metrics.MetricsCollector
    ): RefreshAuthenticator = RefreshAuthenticator(tokenProvider, tokenRefresher, tokenRepository, metrics)

    // ---- ANA OKHTTP (UYGULAMA İÇİN) ----
    @Provides
    @Singleton
    fun provideHttpCache(@ApplicationContext context: Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, NetworkConfig.HTTP_CACHE_SIZE_MB * 1024 * 1024)
    }

    @Provides
    @Singleton
    fun provideCacheControlInterceptor(): CacheControlInterceptor = CacheControlInterceptor()

    @Provides
    @Singleton
    fun provideOkHttp(
        logger: HttpLoggingInterceptor,
        defaultHeaders: Interceptor,
        requestIdInterceptor: RequestIdInterceptor,
        cacheControlInterceptor: CacheControlInterceptor,
        authInterceptor: AuthInterceptor,
        refreshAuthenticator: RefreshAuthenticator,
        httpCache: Cache
    ): OkHttpClient = OkHttpClient.Builder()
        .cache(httpCache)
        .addInterceptor(defaultHeaders)
        .addInterceptor(requestIdInterceptor)
        .addInterceptor(cacheControlInterceptor)
        .addInterceptor(authInterceptor)
        .authenticator(refreshAuthenticator)
        .addInterceptor(logger)
        .connectTimeout(NetworkConfig.connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(NetworkConfig.readTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(NetworkConfig.writeTimeoutSeconds, TimeUnit.SECONDS)
        .retryOnConnectionFailure(NetworkConfig.enableHttpRetry)
        .build()

    // ---- ANA RETROFIT ----
    @Provides @Singleton
    fun provideRetrofit(
        json: Json,
        okHttpClient: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // ---- MESSAGING API ----
    @Provides @Singleton
    fun provideMessagingApi(retrofit: Retrofit): MessagingApi =
        retrofit.create(MessagingApi::class.java)

    // ---- UPLOAD API ----
    @Provides @Singleton
    fun provideUploadApi(retrofit: Retrofit): UploadApi =
        retrofit.create(UploadApi::class.java)

    // ---- AUTH API SERVICE (NEW OTP ENDPOINTS) ----
    @Provides @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService =
        retrofit.create(AuthApiService::class.java)

    // ---- AUTH TOKEN STORE ----
    @Provides @Singleton
    fun provideAuthTokenStore(@ApplicationContext context: Context): AuthTokenStore =
        AuthTokenStore(context)

    // ---- SECURE STORE ----
    @Provides @Singleton
    fun provideSecureStore(@ApplicationContext context: Context): SecureStore =
        SecureStoreImpl(context)

    // ---- TOKEN REPOSITORY ----
    @Provides
    @Singleton
    fun provideTokenRepository(impl: com.msgmates.app.core.auth.TokenRepositoryImpl): com.msgmates.app.core.auth.TokenRepository =
        impl

    // ---- SECURE TOKEN STORE ----
    @Provides
    @Singleton
    fun provideSecureTokenStore(
        @ApplicationContext context: Context
    ): com.msgmates.app.core.auth.SecureTokenStore = com.msgmates.app.core.auth.SecureTokenStore(context)

    // ---- TINK KEYSET MANAGER ----
    @Provides
    @Singleton
    fun provideTinkKeysetManager(
        @ApplicationContext context: Context
    ): com.msgmates.app.core.auth.TinkKeysetManager = com.msgmates.app.core.auth.TinkKeysetManager(context)

    // ---- METRICS COLLECTOR ----
    @Provides
    @Singleton
    fun provideMetricsCollector(): com.msgmates.app.core.metrics.MetricsCollector = com.msgmates.app.core.metrics.MetricsCollector()

    // ---- AUTH REPOSITORY (NEW OTP) ----
    @Provides @Singleton
    fun provideAuthRepository(api: AuthApiService): AuthRepository =
        AuthRepositoryImpl(api)

    // ---- ATTACHMENT UPLOADER ----
    @Provides @Singleton
    fun provideAttachmentUploader(
        @ApplicationContext context: Context,
        uploadApi: UploadApi,
        envConfig: EnvConfig
    ): AttachmentUploader = AttachmentUploader(context, uploadApi, envConfig)

    // ---- ATTACHMENT DOWNLOADER ----
    @Provides @Singleton
    fun provideAttachmentDownloader(
        @ApplicationContext context: Context,
        okHttp: OkHttpClient
    ): AttachmentDownloader = AttachmentDownloader(context, okHttp)

    // ---- AUDIO RECORDER ----
    @Provides @Singleton
    fun provideAudioRecorder(
        @ApplicationContext context: Context
    ): AudioRecorder = AudioRecorder(context)

    // ---- AUDIO PLAYER ----
    @Provides @Singleton
    fun provideAudioPlayer(
        @ApplicationContext context: Context
    ): AudioPlayer = AudioPlayer(context)

    // ---- TYPING MANAGER ----
    @Provides @Singleton
    fun provideTypingManager(): TypingManager = TypingManager()

    // ---- CHAT NOTIFICATION ----
    @Provides @Singleton
    fun provideChatNotification(
        @ApplicationContext context: Context
    ): ChatNotification = ChatNotification(context)

    // ---- CALL SIGNALING MANAGER ----
    @Provides @Singleton
    fun provideCallSignalingManager(): CallSignalingManager = CallSignalingManager()

    // ---- RECEIPT BATCHER ----
    @Provides @Singleton
    fun provideReceiptBatcher(
        messageRepository: com.msgmates.app.core.messaging.MessageRepository
    ): ReceiptBatcher = ReceiptBatcher(messageRepository)

    // ---- OUTBOX WORK MANAGER ----
    @Provides @Singleton
    fun provideOutboxWorkManager(
        @ApplicationContext context: Context
    ): OutboxWorkManager = OutboxWorkManager(context)

    // ---- COIL IMAGE LOADER ----
    @Provides @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): ImageLoader = ImageLoader.Builder(context)
        .okHttpClient(okHttpClient)
        .memoryCache {
            MemoryCache.Builder(context)
                .maxSizePercent(0.25) // 25% of available memory
                .build()
        }
        .diskCache {
            DiskCache.Builder()
                .directory(File(context.cacheDir, "image_cache"))
                .maxSizeBytes(256 * 1024 * 1024) // 256MB
                .build()
        }
        .respectCacheHeaders(false) // Always use our cache
        .allowHardware(true) // Use hardware bitmaps
        .allowRgb565(true) // Use RGB565 for better memory usage
        .build()

    // ---- FLOOD PROTECTION ----
    @Provides @Singleton
    fun provideFloodProtection(): FloodProtection = FloodProtection()

    // ---- METRICS ----
    @Provides @Singleton
    fun provideMetrics(): com.msgmates.app.core.metrics.Metrics = com.msgmates.app.core.metrics.MetricsCollector()
}
