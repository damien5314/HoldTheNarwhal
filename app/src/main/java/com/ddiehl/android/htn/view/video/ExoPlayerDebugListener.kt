package com.ddiehl.android.htn.view.video

import com.google.android.exoplayer2.*
import com.google.android.exoplayer2.source.TrackGroupArray
import com.google.android.exoplayer2.trackselection.TrackSelectionArray
import timber.log.Timber

/**
 * Logs all events available via [Player.EventListener] to the Timber logger
 */
class ExoPlayerDebugListener : Player.EventListener {

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        Timber.d("onTimelineChanged, reason: $reason")
    }

    override fun onTimelineChanged(timeline: Timeline, manifest: Any?, reason: Int) {
        Timber.d("onTimelineChanged with Manifest, reason: $reason")
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        Timber.d("onMediaItemTransition, reason: $reason")
    }

    override fun onTracksChanged(trackGroups: TrackGroupArray, trackSelections: TrackSelectionArray) {
        Timber.d("onTracksChanged")
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        Timber.d("onIsLoadingChanged")
    }

    override fun onLoadingChanged(isLoading: Boolean) {
        Timber.d("onLoadingChanged")
    }

    override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
        Timber.d("onPlayerStateChanged")
    }

    override fun onPlaybackStateChanged(state: Int) {
        Timber.d("onPlaybackStateChanged")
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        Timber.d("onPlayWhenReadyChanged")
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        Timber.d("onPlaybackSuppressionReasonChanged")
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        Timber.d("onIsPlayingChanged")
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        Timber.d("onRepeatModeChanged")
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        Timber.d("onShuffleModeEnabledChanged")
    }

    override fun onPlayerError(error: ExoPlaybackException) {
        Timber.d("onPlayerError")
    }

    override fun onPositionDiscontinuity(reason: Int) {
        Timber.d("onPositionDiscontinuity")
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        Timber.d("onPlaybackParametersChanged")
    }

    override fun onSeekProcessed() {
        Timber.d("onSeekProcessed")
    }

    override fun onExperimentalOffloadSchedulingEnabledChanged(offloadSchedulingEnabled: Boolean) {
        Timber.d("onExperimentalOffloadSchedulingEnabledChanged")
    }
}
