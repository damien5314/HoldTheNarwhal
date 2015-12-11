package com.ddiehl.android.htn.logging;

public class NoLogger implements Logger {
  @Override public void d(String message, Object... args) { /* no-op */ }
  @Override public void e(String message, Object... args) { /* no-op */ }
  @Override public void e(Throwable throwable, String message, Object... args) { /* no-op */ }
  @Override public void w(String message, Object... args) { /* no-op */ }
  @Override public void i(String message, Object... args) { /* no-op */ }
  @Override public void v(String message, Object... args) { /* no-op */ }
  @Override public void wtf(String message, Object... args) { /* no-op */ }
  @Override public void json(String json) { /* no-op */ }
  @Override public void xml(String xml) { /* no-op */ }
}
