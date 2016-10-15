package com.ddiehl.android.htn.presenter;

public interface SettingsPresenter extends BasePresenter {

    void refresh(boolean pullFromServer);

    boolean isRefreshable();
}
