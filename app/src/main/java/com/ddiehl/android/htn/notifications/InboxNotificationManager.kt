package com.ddiehl.android.htn.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import android.support.annotation.RequiresApi
import android.support.v4.app.NotificationCompat
import android.support.v4.app.NotificationManagerCompat
import android.support.v4.app.TaskStackBuilder
import com.ddiehl.android.htn.R
import com.ddiehl.android.htn.listings.inbox.InboxActivity


private const val NOTIFICATION_CHANNEL_ID = "inbox_notifications"
private const val NOTIFICATION_ID = 1

@RequiresApi(Build.VERSION_CODES.O)
fun getNotificationChannel(context: Context): NotificationChannel {
    val channelName = context.getString(R.string.unread_inbox_notification_channel_name)
    return NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT
    )
}
/**
 * Class that displays a notification linking to the user's inbox.
 */
class InboxNotificationManager(private val applicationContext: Context) {

    fun showNotificationWithUnreads(numUnreads: Int) {
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
                .setContentIntent(getInboxPendingIntent())
                .setAutoCancel(true)
                .build()
    }

    private fun getInboxPendingIntent(): PendingIntent? {
        val taskStack = TaskStackBuilder.create(applicationContext)
        val inboxIntent = InboxActivity.getIntent(applicationContext, "unread")
        taskStack.addNextIntentWithParentStack(inboxIntent)
        return taskStack.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }
}
