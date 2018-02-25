package com.ddiehl.android.htn.notifications

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import com.ddiehl.android.htn.HoldTheNarwhal
import io.reactivex.disposables.Disposable
import io.reactivex.disposables.Disposables
import rxreddit.api.RedditService
import javax.inject.Inject

private const val JOB_ID = 1
private const val LATENCY_1_MIN_MILLIS = 1 * 60 * 1000L // 15 minutes
private const val LATENCY_15_MIN_MILLIS = 1 * 60 * 1000L // 15 minutes

@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
fun getJobInfo(context: Context): JobInfo {
    val serviceComponent = ComponentName(context, NotificationCheckJobService::class.java)
    return JobInfo.Builder(JOB_ID, serviceComponent)
            .setPeriodic(LATENCY_1_MIN_MILLIS)
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
        val unreadInboxChecker = UnreadInboxChecker(applicationContext, redditService)
        subscription = unreadInboxChecker.check()
                .subscribe(
                        { jobFinished(params, false) },
                        { jobFinished(params, true) }
                )
    }
}
