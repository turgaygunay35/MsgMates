package com.msgmates.app.core.image

import android.content.Context
import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.Registry
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.engine.cache.LruResourceCache
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions

@GlideModule
class MsgMatesGlideModule : AppGlideModule() {

    override fun applyOptions(context: Context, builder: GlideBuilder) {
        // Memory cache: 50MB for large contact lists
        val memoryCacheSizeBytes = 50 * 1024 * 1024L // 50MB
        builder.setMemoryCache(LruResourceCache(memoryCacheSizeBytes))

        // Disk cache: 100MB for avatar images
        val diskCacheSizeBytes = 100 * 1024 * 1024L // 100MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes))

        // Default request options for avatars
        builder.setDefaultRequestOptions(
            RequestOptions()
                .centerCrop()
                .timeout(10000) // 10 seconds timeout
        )
    }

    override fun registerComponents(context: Context, glide: Glide, registry: Registry) {
        // Register any custom components here if needed
    }

    override fun isManifestParsingEnabled(): Boolean {
        return false // Disable manifest parsing for better performance
    }
}
