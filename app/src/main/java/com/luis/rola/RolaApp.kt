package com.luis.rola

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import com.luis.rola.feature.library.SongCoverFetcher

class RolaApp : Application(), SingletonImageLoader.Factory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(context: PlatformContext) = ImageLoader.Builder(context)
        .components {
            add(SongCoverFetcher.Factory())
            add(SongCoverFetcher.Key)
        }
        .build()
}
