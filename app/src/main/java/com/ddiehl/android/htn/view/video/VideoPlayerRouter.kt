package com.ddiehl.android.htn.view.video

import androidx.fragment.app.FragmentActivity
import rxreddit.model.Media.RedditVideo
import javax.inject.Inject

class VideoPlayerRouter @Inject constructor() {

    fun openRedditVideo(activity: FragmentActivity, redditVideo: RedditVideo) {
        val url = redditVideo.dashUrl
        val dialog: VideoPlayerDialog = VideoPlayerDialogBuilder.newVideoPlayerDialog(url)
        dialog.show(activity.supportFragmentManager, VideoPlayerDialog.TAG)
    }
}
