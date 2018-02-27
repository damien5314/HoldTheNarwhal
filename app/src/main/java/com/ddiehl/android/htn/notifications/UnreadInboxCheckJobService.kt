package com.ddiehl.android.htn.notifications

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.ddiehl.android.htn.HoldTheNarwhal
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import io.reactivex.schedulers.Schedulers
import rxreddit.api.RedditService
import rxreddit.model.ListingResponse
import timber.log.Timber
import javax.inject.Inject

const val JOB_ID = 1
private const val LATENCY_1_MIN_MILLIS = 1 * 60 * 1000L // 15 minutes
private const val LATENCY_15_MIN_MILLIS = 1 * 60 * 1000L // 15 minutes

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun getJobInfo(context: Context): JobInfo {
    val serviceComponent = ComponentName(context, UnreadInboxCheckJobService::class.java)
    return JobInfo.Builder(JOB_ID, serviceComponent)
            .setPeriodic(LATENCY_1_MIN_MILLIS)
            .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
            .setPersisted(true)
            .build()
}

/**
 * Service which starts [UnreadInboxChecker] to check if a user
 * has unread inbox messages, then displays a notification with
 * [InboxNotificationManager].
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class UnreadInboxCheckJobService : JobService() {

    @Inject lateinit var redditService: RedditService
    private lateinit var inboxNotificationManager: InboxNotificationManager
    private var subscription: Disposable = Disposables.empty()

    init {
        HoldTheNarwhal.getApplicationComponent().inject(this)
    }

    override fun onStartJob(params: JobParameters?): Boolean {
        inboxNotificationManager = InboxNotificationManager(applicationContext)
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
            inboxNotificationManager.showNotificationWithUnreads(numUnreads)
        }
    }

    private fun onInboxFetchError(throwable: Throwable) {
        Timber.w(throwable)
    }
}
