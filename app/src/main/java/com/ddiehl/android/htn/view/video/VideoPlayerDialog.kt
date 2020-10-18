package com.ddiehl.android.htn.view.video

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.VideoView
import androidx.fragment.app.DialogFragment
import butterknife.BindView
import butterknife.ButterKnife
import com.ddiehl.android.htn.R
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

    @BindView(R.id.video_view)
    lateinit var videoView: VideoView

    @Arg
    lateinit var url: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FragmentArgs.inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.video_dialog, container, false)
        ButterKnife.bind(this, view)
        videoView.setVideoPath(url)
        return view
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog =
        Dialog(activity, R.style.DialogOverlay)

    override fun onResume() {
        super.onResume()
        startVideo()
    }

    override fun onPause() {
        stopVideo()
        super.onPause()
    }

    private fun startVideo() {
        videoView.start()
    }

    private fun stopVideo() {
        videoView.stopPlayback()
    }
}
