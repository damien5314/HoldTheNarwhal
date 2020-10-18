package com.ddiehl.android.htn.view.video

import android.app.Dialog
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
        view!!.findViewById<PlayerView>(R.id.video_view)!!.onResume()
        startVideo()
    }

    override fun onStop() {
        stopVideo()
        view!!.findViewById<PlayerView>(R.id.video_view)!!.onPause()
        super.onStop()
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
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
