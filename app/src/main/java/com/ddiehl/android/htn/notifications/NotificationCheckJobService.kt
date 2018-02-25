package com.ddiehl.android.htn.notifications

import android.app.Notification
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.ddiehl.android.htn.HoldTheNarwhal
import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import rxreddit.api.RedditService
import rxreddit.model.Listing
import rxreddit.model.ListingResponse
import javax.inject.Inject

private const val JOB_ID = 1
private const val LATENCY_MIN_MILLIS = 1 * 60 * 1000L // 15 minutes
private const val NOTIFICATION_CHANNEL_ID = "inbox_notifications"
private const val NOTIFICATION_ID = 1

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun getJobInfo(context: Context): JobInfo {
    val serviceComponent = ComponentName(context, NotificationCheckJobService::class.java)
    return JobInfo.Builder(JOB_ID, serviceComponent)
            .setPeriodic(LATENCY_MIN_MILLIS)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .build()
}

/**
 * TODO documentation
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class NotificationCheckJobService : JobService() {

    @Inject
    lateinit var redditService: RedditService
    private var subscription: Disposable = Disposables.empty()

    init {
        HoldTheNarwhal.getApplicationComponent().inject(this)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        checkUnreads(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        subscription.dispose()
        return false
    }

    private fun checkUnreads(params: JobParameters?) {
        subscription = redditService.getInbox("unread", null, null)
                .flatMap(this::checkNullResponse)
                .subscribe({
                    onInboxFetched(it)
                    jobFinished(params, false)
                }, {
                    onInboxFetchError(it)
                    jobFinished(params, true)
                })
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
        val notificationService = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
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
