package com.ddiehl.android.dlogger;

import timber.log.Timber;

public class TimberLogger implements Logger {
  public TimberLogger() {
    Timber.Tree tree = new Timber.DebugTree();
    Timber.plant(tree);
  }

  @Override
  public void d(String message, Object... args) {
    Timber.d(message, args);
  }

  @Override
  public void e(String message, Object... args) {
    Timber.e(message, args);
  }

  @Override
  public void e(Throwable throwable, String message, Object... args) {
    Timber.e(throwable, message, args);
  }

  @Override
  public void w(String message, Object... args) {
    Timber.w(message, args);
  }

  @Override
  public void i(String message, Object... args) {
    Timber.i(message, args);
  }

  @Override
  public void v(String message, Object... args) {
    Timber.v(message, args);
  }

  @Override
  public void wtf(String message, Object... args) {
    Timber.wtf(message, args);
  }

  @Override
  public void json(String json) {
    // Uses debug method by default, since Timber does not have json pretty-printing
    Timber.d(json);
  }

  @Override
  public void xml(String xml) {
    // Uses debug method by default, since Timber does not have xml pretty-printing
    Timber.d(xml);
  }

  ///////////////
  // Singleton //
  ///////////////

  private static TimberLogger _instance;

  public static TimberLogger getInstance() {
    if (_instance == null) {
      synchronized (TimberLogger.class) {
        if (_instance == null) _instance = new TimberLogger();
      }
    }
    return _instance;
  }
}
