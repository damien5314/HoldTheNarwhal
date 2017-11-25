package com.ddiehl.android.htn.view;

import android.net.MailTo;
import android.support.annotation.StringRes;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MainView {

    void loadImageIntoDrawerHeader(@Nullable String url);

    void setTitle(@NotNull CharSequence title);

    void setTitle(@StringRes int id);

    void showSpinner();

    void dismissSpinner();

    void showToast(@NotNull CharSequence msg);

    void showError(@NotNull CharSequence message);

    void doSendEmail(MailTo mailTo);
}
