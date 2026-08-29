package com.example.util

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object ImagePreloadHelper {

    suspend fun preloadImages(context: Context, imageUrls: List<String>) = withContext(Dispatchers.IO) {
        val imageLoader = ImageLoader(context)
        val validUrls = imageUrls.filter { it.isNotBlank() && (it.startsWith("http://") || it.startsWith("https://")) }.distinct()
        for (url in validUrls) {
            try {
                val request = ImageRequest.Builder(context)
                    .data(url)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                imageLoader.enqueue(request)
            } catch (e: Exception) {
                // Ignore failure for preloading
            }
        }
    }
}
