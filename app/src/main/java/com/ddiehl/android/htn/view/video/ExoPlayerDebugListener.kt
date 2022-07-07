package com.ddiehl.android.htn.view.video

import com.google.android.exoplayer2.Player
import timber.log.Timber

/**
 * Logs all events available via [Player.Listener] to the Timber logger
 */
class ExoPlayerDebugListener : Player.Listener {

    override fun onEvents(player: Player, events: Player.Events) {
        super.onEvents(player, events)
        Timber.d("ExoPlayerDebug_onEvents: ${events.size()}")
        for (i in 0..events.size()) {
            val event = events.get(i)
            Timber.d("ExoPlayerDebug_onEvent: $event")
        }
    }
}
