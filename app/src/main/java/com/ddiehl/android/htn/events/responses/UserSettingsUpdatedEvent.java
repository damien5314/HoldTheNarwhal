/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

public class UserSettingsUpdatedEvent extends FailableEvent {

    public UserSettingsUpdatedEvent() {

    }

    public UserSettingsUpdatedEvent(Throwable e) {
        super(e);
    }
}
