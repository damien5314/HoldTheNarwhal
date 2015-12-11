package com.ddiehl.android.htn.logging;

import com.orhanobut.logger.*;

public class OrhanobutLogger implements Logger {
  public static final String TAG = "HTN";

  private OrhanobutLogger() {
    com.orhanobut.logger.Logger.init(TAG)
        .hideThreadInfo()
        .setMethodCount(0)
        .setLogLevel(com.ddiehl.android.htn.BuildConfig.DEBUG ? LogLevel.FULL : LogLevel.NONE);
  }

  @Override
  public void d(String message, Object... args) {
    com.orhanobut.logger.Logger.d(message, args);
  }

  @Override
  public void e(String message, Object... args) {
    com.orhanobut.logger.Logger.e(message, args);
  }

  @Override
  public void e(Throwable throwable, String message, Object... args) {
    com.orhanobut.logger.Logger.e(throwable, message, args);
  }

  @Override
  public void w(String message, Object... args) {
    com.orhanobut.logger.Logger.w(message, args);
  }

  @Override
  public void i(String message, Object... args) {
    com.orhanobut.logger.Logger.i(message, args);
  }

  @Override
  public void v(String message, Object... args) {
    com.orhanobut.logger.Logger.v(message, args);
  }

  @Override
  public void wtf(String message, Object... args) {
    com.orhanobut.logger.Logger.wtf(message, args);
  }

  @Override
  public void json(String json) {
    com.orhanobut.logger.Logger.json(json);
  }

  @Override
  public void xml(String xml) {
    com.orhanobut.logger.Logger.xml(xml);
  }

  ///////////////
  // Singleton //
  ///////////////

  private static OrhanobutLogger _instance;

  public static OrhanobutLogger getInstance() {
    if (_instance == null) {
      synchronized (OrhanobutLogger.class) {
        if (_instance == null) _instance = new OrhanobutLogger();
      }
    }
    return _instance;
  }
}
