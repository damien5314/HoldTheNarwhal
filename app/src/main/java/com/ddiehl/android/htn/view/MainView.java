package com.ddiehl.android.htn.view;

import android.net.MailTo;

import androidx.annotation.StringRes;

import org.jetbrains.annotations.NotNull;

public interface MainView {

    void setTitle(@NotNull CharSequence title);

    void setTitle(@StringRes int id);

    void showSpinner();

    void dismissSpinner();

    void showToast(@NotNull CharSequence msg);

    void showToast(@StringRes int messageResId);

    void showError(@NotNull CharSequence message);

    void doSendEmail(MailTo mailTo);
}
