package com.ddiehl.android.htn.view.video

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.Player
import timber.log.Timber

/**
 * TODO: Describe what this class is responsible for
 */
class ExoPlayerErrorLogger : Player.EventListener {

    override fun onPlayerError(error: ExoPlaybackException) {
        if (isLoggable(error.type)) {
            Timber.e(error, "ExoPlayer encountered a PlaybackException")
        }
    }

    private fun isLoggable(errorType: Int): Boolean {
        return when (errorType) {
            ExoPlaybackException.TYPE_SOURCE -> false
            ExoPlaybackException.TYPE_RENDERER -> true
            ExoPlaybackException.TYPE_UNEXPECTED -> true
            ExoPlaybackException.TYPE_REMOTE -> false
            ExoPlaybackException.TYPE_OUT_OF_MEMORY -> true
            ExoPlaybackException.TYPE_TIMEOUT -> false
            else -> true
        }
    }
}
