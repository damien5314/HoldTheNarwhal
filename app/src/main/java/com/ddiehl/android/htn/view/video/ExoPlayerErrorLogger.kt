package com.ddiehl.android.htn.view.video

import com.google.android.exoplayer2.ExoPlaybackException
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import timber.log.Timber

/**
 * Logs eligible error types to the Timber logger
 */
class ExoPlayerErrorLogger : Player.Listener {

    override fun onPlayerError(error: PlaybackException) {
        if (isLoggable(error.errorCode)) {
            Timber.e(error, "ExoPlayer encountered a PlaybackException")
        }
    }

    private fun isLoggable(errorType: Int): Boolean {
        return when (errorType) {
            ExoPlaybackException.TYPE_SOURCE -> false
            ExoPlaybackException.TYPE_RENDERER -> true
            ExoPlaybackException.TYPE_UNEXPECTED -> true
            ExoPlaybackException.TYPE_REMOTE -> false
            else -> true
        }
    }
}
