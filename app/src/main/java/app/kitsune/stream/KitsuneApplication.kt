package app.kitsune.stream

import android.app.Application
import android.content.Context
import android.os.Build
import app.kitsune.stream.data.catalog.AniListCatalog
import app.kitsune.stream.data.catalog.CatalogSource
import app.kitsune.stream.data.provider.ProviderStore
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class KitsuneApplication : Application(), ImageLoaderFactory {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }

    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .components {
                if (Build.VERSION.SDK_INT >= 28) add(ImageDecoderDecoder.Factory())
                else add(GifDecoder.Factory())
            }
            .crossfade(true)
            .build()
}

class AppContainer(context: Context) {
    private val httpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    val catalog: CatalogSource = AniListCatalog(httpClient)
    val providerStore: ProviderStore = ProviderStore(context)
}
