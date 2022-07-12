package com.ddiehl.android.htn.view.video

import androidx.fragment.app.FragmentActivity
import rxreddit.model.Media.RedditVideo
import javax.inject.Inject

class VideoPlayerRouter @Inject constructor(
    private val fragmentActivity: FragmentActivity,
) {

    fun openRedditVideo(redditVideo: RedditVideo) {
        val url = redditVideo.dashUrl
        val dialog: VideoPlayerDialog = VideoPlayerDialogBuilder.newVideoPlayerDialog(url)
        dialog.show(fragmentActivity.supportFragmentManager, VideoPlayerDialog.TAG)
    }
}
