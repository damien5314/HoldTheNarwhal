package com.ddiehl.android.htn;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.Context;
import android.os.Build;
import android.support.annotation.VisibleForTesting;

import com.crashlytics.android.Crashlytics;
import com.ddiehl.android.htn.di.ApplicationComponent;
import com.ddiehl.android.htn.di.ApplicationModule;
import com.ddiehl.android.htn.di.DaggerApplicationComponent;
import com.ddiehl.android.htn.notifications.InboxNotificationManagerKt;
import com.ddiehl.android.htn.notifications.UnreadInboxCheckJobServiceKt;
import com.ddiehl.android.logging.CrashlyticsLogger;
import com.ddiehl.android.logging.CrashlyticsLoggingTree;
import com.ddiehl.android.logging.LogcatLogger;
import com.ddiehl.android.logging.LogcatLoggingTree;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class HoldTheNarwhal extends Application {

    private static ApplicationComponent component;

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        component = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(this))
                .build();

//    LeakCanary.install(this);

        // Install logging trees
        if (BuildConfig.DEBUG) {
            Timber.Tree tree = new LogcatLoggingTree(new LogcatLogger());
            Timber.plant(tree);
        } else {
            Timber.Tree tree = new CrashlyticsLoggingTree(new CrashlyticsLogger());
            Timber.plant(tree);
        }

        createNotificationChannels();
        scheduleInboxNotifications();

        // Add logging for CPU ABI support
        logSupportedCpuAbis();
    }

    public static ApplicationComponent getApplicationComponent() {
        return component;
    }

    @VisibleForTesting
    public static void setTestComponent(ApplicationComponent testComponent) {
        component = testComponent;
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (notificationManager == null) return;

            final NotificationChannel notificationChannel =
                    InboxNotificationManagerKt.getNotificationChannel(this);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    void scheduleInboxNotifications() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            JobScheduler jobScheduler =
                    (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            if (jobScheduler == null) {
                return;
            }

            jobScheduler.cancel(UnreadInboxCheckJobServiceKt.JOB_ID);

            final JobInfo jobInfo = UnreadInboxCheckJobServiceKt.getJobInfo(this);
            jobScheduler.schedule(jobInfo);
        }
    }

    void logSupportedCpuAbis() {
        String debugString;
        if (Build.VERSION.SDK_INT >= 21) {
            String[] supported = Build.SUPPORTED_ABIS;
            debugString = Arrays.toString(supported);
        } else {
            debugString = String.format(
                    "(0) %s, (1) %s",
                    Build.CPU_ABI, Build.CPU_ABI2
            );
        }

        Timber.i("Supported ABIs: %s", debugString);
    }
}
