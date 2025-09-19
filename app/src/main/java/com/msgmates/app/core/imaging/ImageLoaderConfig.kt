package com.msgmates.app.core.imaging

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Scale
import coil.size.Size
import coil.transform.CircleCropTransformation
import coil.transform.RoundedCornersTransformation
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Image loading configuration and utilities for optimized image loading
 */
@Singleton
class ImageLoaderConfig @Inject constructor(
    private val imageLoader: ImageLoader
) {

    /**
     * Load image with thumbnail optimization
     */
    fun loadThumbnail(
        context: Context,
        data: Any?,
        size: Int = 150,
        onSuccess: (android.graphics.drawable.Drawable) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val request = ImageRequest.Builder(context)
            .data(data)
            .size(Size(size, size))
            .scale(Scale.FIT)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .crossfade(200)
            .build()

        imageLoader.enqueue(request)
    }

    /**
     * Load profile image with circle crop
     */
    fun loadProfileImage(
        context: Context,
        data: Any?,
        size: Int = 200,
        onSuccess: (android.graphics.drawable.Drawable) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val request = ImageRequest.Builder(context)
            .data(data)
            .size(Size(size, size))
            .scale(Scale.FIT)
            .transformations(CircleCropTransformation())
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(300)
            .build()

        imageLoader.enqueue(request)
    }

    /**
     * Load chat image with rounded corners
     */
    fun loadChatImage(
        context: Context,
        data: Any?,
        width: Int = 300,
        height: Int = 300,
        onSuccess: (android.graphics.drawable.Drawable) -> Unit,
        onError: (Throwable) -> Unit
    ) {
        val request = ImageRequest.Builder(context)
            .data(data)
            .size(Size(width, height))
            .scale(Scale.FIT)
            .transformations(RoundedCornersTransformation(12f))
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .crossfade(200)
            .build()

        imageLoader.enqueue(request)
    }

    /**
     * Preload images for better performance
     */
    fun preloadImages(context: Context, urls: List<String>) {
        urls.forEach { url ->
            val request = ImageRequest.Builder(context)
                .data(url)
                .size(Size.ORIGINAL)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build()

            imageLoader.enqueue(request)
        }
    }

    /**
     * Clear memory cache
     */
    fun clearMemoryCache() {
        imageLoader.memoryCache?.clear()
    }

    /**
     * Clear disk cache
     */
    suspend fun clearDiskCache() {
        imageLoader.diskCache?.clear()
    }
}
