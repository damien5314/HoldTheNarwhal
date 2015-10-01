package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;

public class UserSettingsUpdatedEvent extends FailableEvent {

    public UserSettingsUpdatedEvent() {

    }

    public UserSettingsUpdatedEvent(@NonNull Throwable e) {
        super(e);
    }
}
