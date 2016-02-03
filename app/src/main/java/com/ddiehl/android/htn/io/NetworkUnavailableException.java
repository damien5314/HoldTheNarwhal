package com.ddiehl.android.htn.io;

public class NetworkUnavailableException extends RuntimeException {
  public NetworkUnavailableException() {
    super("Network is unavailable");
  }
}
