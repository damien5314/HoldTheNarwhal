package com.ddiehl.android.htn;

import android.app.Application;

import com.ddiehl.android.htn.di.ApplicationComponent;
import com.ddiehl.android.htn.di.ApplicationModule;
import com.ddiehl.android.htn.di.DaggerApplicationComponent;
import com.squareup.picasso.Picasso;

import timber.log.Timber;

public class HoldTheNarwhal extends Application {

  private static ApplicationComponent mComponent;

  @Override
  public void onCreate() {
    super.onCreate();

    mComponent = DaggerApplicationComponent.builder()
        .applicationModule(new ApplicationModule(this))
        .build();

//    LeakCanary.install(this);
    Timber.plant(new Timber.DebugTree());

    if (BuildConfig.DEBUG) {
      Picasso.setSingletonInstance(
          new Picasso.Builder(this)
//              .memoryCache(Cache.NONE)
//              .indicatorsEnabled(true)
              .loggingEnabled(false)
              .build());
    }
  }

  public static ApplicationComponent getApplicationComponent() {
    return mComponent;
  }
}
