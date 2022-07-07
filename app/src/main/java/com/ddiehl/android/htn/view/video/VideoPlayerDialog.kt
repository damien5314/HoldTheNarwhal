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
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView
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
        ExoPlayer.Builder(requireContext())
            .build()
            .also { player ->
                player.addListener(ExoPlayerErrorLogger())
            }
    }
    private val videoView by lazy {
        requireView().findViewById<StyledPlayerView>(R.id.video_view).also { videoView ->
            videoView.controllerAutoShow = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.video_dialog, container, false)

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
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.playWhenReady = true
        videoView.player = exoPlayer
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
