package com.ddiehl.android.htn.view;

import android.net.MailTo;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

public interface MainView {

    void loadImageIntoDrawerHeader(@Nullable String url);

    void setTitle(@NonNull CharSequence title);

    void setTitle(@StringRes int id);

    void showSpinner();

    void dismissSpinner();

    void showToast(@NonNull CharSequence msg);

    void showError(Throwable error, CharSequence message);

    void doSendEmail(MailTo mailTo);
}
