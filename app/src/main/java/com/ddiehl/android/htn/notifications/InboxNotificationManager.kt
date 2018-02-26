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
 * TODO documentation
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
                .build()
    }
}
