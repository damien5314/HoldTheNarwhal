package com.ddiehl.android.htn;

import android.app.Application;
import android.os.Build;
import android.support.annotation.VisibleForTesting;

import com.crashlytics.android.Crashlytics;
import com.ddiehl.android.htn.di.ApplicationComponent;
import com.ddiehl.android.htn.di.ApplicationModule;
import com.ddiehl.android.htn.di.DaggerApplicationComponent;
import com.ddiehl.android.htn.di.SharedModule;
import com.ddiehl.android.logging.CrashlyticsLogger;
import com.ddiehl.android.logging.CrashlyticsLoggingTree;
import com.ddiehl.android.logging.LogcatLogger;
import com.ddiehl.android.logging.LogcatLoggingTree;

import java.util.Arrays;

import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

public class HoldTheNarwhal extends Application {

    private static ApplicationComponent mComponent;

    @Override
    public void onCreate() {
        super.onCreate();

        Fabric.with(this, new Crashlytics());

        mComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule())
                .sharedModule(new SharedModule(this))
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

        // Add logging for CPU ABI support
        logSupportedCpuAbis();
    }

    public static ApplicationComponent getApplicationComponent() {
        return mComponent;
    }

    @VisibleForTesting
    public static void setTestComponent(ApplicationComponent testComponent) {
        mComponent = testComponent;
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
