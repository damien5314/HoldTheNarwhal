package com.ddiehl.android.htn.notifications

import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.os.Build
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
        val applicationContext: Context,
        val redditService: RedditService
) {

    fun check(): Completable {
        return redditService.getInbox("unread", null, null)
                .flatMap(this::checkNullResponse)
                .doOnNext { onInboxFetched(it) }
                .doOnError { onInboxFetchError(it) }
                .ignoreElements()
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
        val notificationService = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                as NotificationManager
        notificationService.notify(NOTIFICATION_ID, notification)
    }

    private fun getNotification(unreads: Int): Notification {
        return if (Build.VERSION.SDK_INT >= 26) {
            Notification.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
                    .setContentTitle("$unreads unread notifications")
                    .build()
        } else {
            @Suppress("DEPRECATION")
            Notification.Builder(applicationContext)
                    .setContentTitle("$unreads unread notifications")
                    .build()
        }
    }
}
