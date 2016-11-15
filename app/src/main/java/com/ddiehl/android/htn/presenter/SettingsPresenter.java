package com.ddiehl.android.htn.presenter;

public interface SettingsPresenter {

    void refresh(boolean pullFromServer);

    boolean isRefreshable();

    boolean isUserAuthorized();
}
