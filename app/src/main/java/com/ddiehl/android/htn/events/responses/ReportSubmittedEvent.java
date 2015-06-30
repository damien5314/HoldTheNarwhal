/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.events.responses;

public class ReportSubmittedEvent extends FailableEvent {
    private String mId;

    public ReportSubmittedEvent(String id) {
        mId = id;
    }

    public ReportSubmittedEvent(Exception e) {
        super(e);
    }

    public String getId() {
        return mId;
    }
}
