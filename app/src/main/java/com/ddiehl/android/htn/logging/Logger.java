package com.ddiehl.android.htn.logging;

public interface Logger {
  void d(String message, Object... args);
  void e(String message, Object... args);
  void e(Throwable throwable, String message, Object... args);
  void w(String message, Object... args);
  void i(String message, Object... args);
  void v(String message, Object... args);
  void wtf(String message, Object... args);
  void json(String json);
  void xml(String xml);
}
