package com.funkymuse.aurora.di

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import com.funkymuse.aurora.R
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by FunkyMuse, date 2/28/21
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun imageLoader(@ApplicationContext context: Context): ImageLoader = ImageLoader.Builder(context)
            .error(R.drawable.ic_logo)
            .crossfade(true)
            .diskCachePolicy(CachePolicy.DISABLED)
            .fallback(R.drawable.ic_logo)
            .build()

}