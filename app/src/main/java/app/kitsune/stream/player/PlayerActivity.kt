package app.kitsune.stream.player

import android.app.PictureInPictureParams
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

@androidx.annotation.OptIn(UnstableApi::class)
class PlayerActivity : ComponentActivity() {

    private var player: ExoPlayer? = null
    private var playerView: PlayerView? = null
    private var streamUrl: String? = null
    private var startPosition = 0L
    private var playWhenReady = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        streamUrl = intent.getStringExtra(EXTRA_URL)
        if (streamUrl.isNullOrBlank()) {
            finish()
            return
        }
        title = intent.getStringExtra(EXTRA_TITLE) ?: getString(app.kitsune.stream.R.string.app_name)
        playerView = PlayerView(this)
        setContentView(playerView!!)
    }

    private fun initialize() {
        if (player != null) return
        val url = streamUrl ?: return
        player = ExoPlayer.Builder(this).build().also { exo ->
            playerView?.player = exo
            exo.setMediaItem(MediaItem.fromUri(url))
            exo.seekTo(startPosition)
            exo.playWhenReady = playWhenReady
            exo.prepare()
        }
    }

    private fun release() {
        player?.let { exo ->
            startPosition = exo.currentPosition
            playWhenReady = exo.playWhenReady
            exo.release()
        }
        player = null
        playerView?.player = null
    }

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= 24) initialize()
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT < 24) initialize()
    }

    override fun onPause() {
        super.onPause()
        if (Build.VERSION.SDK_INT < 24) release()
    }

    override fun onStop() {
        super.onStop()
        if (Build.VERSION.SDK_INT >= 24) release()
    }

    override fun onDestroy() {
        release()
        super.onDestroy()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (Build.VERSION.SDK_INT >= 26 && player?.isPlaying == true) {
            enterPictureInPictureMode(
                PictureInPictureParams.Builder()
                    .setAspectRatio(Rational(16, 9))
                    .build()
            )
        }
    }

    companion object {
        private const val EXTRA_URL = "extra_url"
        private const val EXTRA_TITLE = "extra_title"

        fun start(context: Context, url: String, title: String?) {
            context.startActivity(
                Intent(context, PlayerActivity::class.java)
                    .putExtra(EXTRA_URL, url)
                    .putExtra(EXTRA_TITLE, title)
            )
        }
    }
}
