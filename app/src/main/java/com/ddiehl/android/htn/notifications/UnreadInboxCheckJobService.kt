package com.ddiehl.android.htn.notifications

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.content.ComponentName
import android.content.Context
import com.ddiehl.android.htn.BaseJobService
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import rxreddit.api.RedditService
import rxreddit.model.ListingResponse
import timber.log.Timber
import javax.inject.Inject

const val JOB_ID = 1
private const val LATENCY_15_MIN_MILLIS = 15 * 60 * 1000L

fun getJobInfo(context: Context): JobInfo {
    val serviceComponent = ComponentName(context, UnreadInboxCheckJobService::class.java)
    return JobInfo.Builder(JOB_ID, serviceComponent)
        .setPeriodic(LATENCY_15_MIN_MILLIS)
        .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
        .setPersisted(true)
        .build()
}

/**
 * Service which starts [UnreadInboxChecker] to check if a user has unread
 * inbox messages, then displays a notification with [InboxNotificationManager].
 */
class UnreadInboxCheckJobService : BaseJobService() {

    @Inject
    lateinit var redditService: RedditService
    private lateinit var inboxNotificationManager: InboxNotificationManager
    private lateinit var inboxNotificationTracker: InboxNotificationTracker
    private var subscription: Disposable = Disposable.empty()

    override fun onStartJob(params: JobParameters?): Boolean {
        inboxNotificationManager = InboxNotificationManager(applicationContext)
        inboxNotificationTracker = InboxNotificationTracker(applicationContext)
        checkUnreads(params)
        return true
    }

    override fun onStopJob(params: JobParameters?): Boolean {
        if (!subscription.isDisposed) {
            subscription.dispose()
        }
        return false
    }

    private fun checkUnreads(params: JobParameters?) {
        if (!redditService.isUserAuthorized) {
            jobFinished(params, false)
            return
        }
        val unreadInboxChecker = UnreadInboxChecker(redditService)
        subscription = unreadInboxChecker.check()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                onInboxFetched(it)
                jobFinished(params, false)
            }, {
                onInboxFetchError(it)
                jobFinished(params, true)
            })
    }

    private fun onInboxFetched(response: ListingResponse) {
        val children = response.data.children
        val numUnreads = children.size
        if (numUnreads > 0) {
            val latestMessage = children[0]
            val latestMessageIsNew = inboxNotificationTracker.lastMessageId != latestMessage.id
            if (latestMessageIsNew) {
                inboxNotificationTracker.lastMessageId = latestMessage.id
                inboxNotificationManager.showNotificationWithUnreads(numUnreads)
            }
        }
    }

    private fun onInboxFetchError(throwable: Throwable) {
        Timber.e(throwable)
    }
}
