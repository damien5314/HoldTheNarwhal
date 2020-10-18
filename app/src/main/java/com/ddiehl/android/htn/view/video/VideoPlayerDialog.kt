package com.ddiehl.android.htn.view.video

import android.app.Dialog
import android.os.Build.VERSION.SDK_INT
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import com.ddiehl.android.htn.R
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ui.PlayerView
import com.hannesdorfmann.fragmentargs.FragmentArgs
import com.hannesdorfmann.fragmentargs.annotation.Arg
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs

/**
 * Displays videos loaded from URL in a [VideoView].
 */
@FragmentWithArgs
class VideoPlayerDialog : DialogFragment() {

    companion object {
        const val TAG: String = "VideoPlayerDialog"
    }

    @Arg
    lateinit var url: String

    private val exoPlayer by lazy {
        SimpleExoPlayer.Builder(requireContext())
            .build()
    }
    private val videoView by lazy {
        requireView().findViewById<PlayerView>(R.id.video_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.video_dialog, container, false).also { view ->
        val playerView = view.findViewById<PlayerView>(R.id.video_view)
        playerView.player = exoPlayer
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(requireContext(), R.style.DialogOverlay)

    override fun onStart() {
        super.onStart()
        if (SDK_INT >= 24) {
            videoView.onResume()
            startVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        if (SDK_INT < 24) {
            videoView.onResume()
            startVideo()
        }
    }

    override fun onPause() {
        if (SDK_INT < 24) {
            stopVideo()
            videoView.onPause()
        }
        super.onPause()
    }

    override fun onStop() {
        if (SDK_INT >= 24) {
            stopVideo()
            videoView.onPause()
        }
        super.onStop()
    }

    private fun startVideo() {
        val mediaItem = MediaItem.fromUri(url)
        exoPlayer.addListener(ExoPlayerDebugListener())
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playWhenReady = true
        exoPlayer.prepare()
    }

    private fun stopVideo() {
        exoPlayer.stop()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
    }
}
