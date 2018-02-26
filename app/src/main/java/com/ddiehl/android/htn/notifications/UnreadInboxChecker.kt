package com.ddiehl.android.htn.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import com.ddiehl.android.htn.R
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableSource
import rxreddit.api.RedditService
import rxreddit.model.Listing
import rxreddit.model.ListingResponse

private const val NOTIFICATION_CHANNEL_ID = "inbox_notifications"
private const val NOTIFICATION_ID = 1

/**
 * TODO documentation
 */
class UnreadInboxChecker(
        private val applicationContext: Context,
        private val redditService: RedditService
) {

    fun check(): Completable {
        val isLoggedIn = redditService.isUserAuthorized
        return if (isLoggedIn) {
            redditService.getInbox("unread", null, null)
                    .flatMap(this::checkNullResponse)
                    //TODO: Fix these side effect things, we should handle this in another object
                    .doOnNext { onInboxFetched(it) }
                    .doOnError { onInboxFetchError(it) }
                    .ignoreElements()
        } else {
            Completable.complete()
        }
    }

    private fun checkNullResponse(listingResponse: ListingResponse): ObservableSource<ListingResponse> {
        return if (listingResponse.data.children != null) {
            Observable.just(listingResponse)
        } else {
            Observable.error(NullPointerException("null children in response"))
        }
    }

    private fun onInboxFetched(response: ListingResponse) {
        val children = response.data.children
        val numUnreads = children.size
        if (numUnreads > 0) {
            showNotificationWithUnreads(children)
        }
    }

    private fun onInboxFetchError(throwable: Throwable) {
        // TODO
    }

    private fun showNotificationWithUnreads(messages: List<Listing>) {
        val numUnreads = messages.size
        val notification = getNotification(numUnreads)
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getNotification(unreads: Int): Notification {
        val title = applicationContext.resources
                .getQuantityString(R.plurals.unread_inbox_notification_title, unreads, unreads)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
        } else {
            @Suppress("DEPRECATION")
            NotificationCompat.Builder(applicationContext)
        }
                .setContentTitle(title)
                .setSmallIcon(R.drawable.ic_email_white_24dp)
                .build()
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun getNotificationChannel(context: Context): NotificationChannel {
    val channelName = context.getString(R.string.unread_inbox_notification_channel_name)
    return NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
    )
}
