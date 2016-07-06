package com.ddiehl.android.htn.view;

import android.net.MailTo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

public interface MainView {

  void loadImageIntoDrawerHeader(@Nullable String url);

  void setTitle(@NonNull CharSequence title);

  void setTitle(@StringRes int id);

  void showSpinner(@Nullable String msg);

  void showSpinner(@StringRes int resId);

  void dismissSpinner();

  void showToast(@NonNull String msg);

  void showToast(@StringRes int resId);

  void showError(Throwable error, String message);

  void doSendEmail(MailTo mailTo);
}
