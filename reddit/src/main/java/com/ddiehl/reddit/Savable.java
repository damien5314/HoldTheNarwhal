package com.ddiehl.reddit;

public interface Savable {
  String getName();
  Boolean isSaved();
  void isSaved(boolean b);
}
