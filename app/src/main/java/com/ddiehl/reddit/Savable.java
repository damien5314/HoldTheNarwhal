package com.ddiehl.reddit;

public interface Savable {
  boolean isSaved();
  void isSaved(boolean b);
  String getFullName();
}
