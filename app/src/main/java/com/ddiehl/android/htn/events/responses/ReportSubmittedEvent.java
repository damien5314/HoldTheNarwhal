package com.ddiehl.android.htn.events.responses;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public class ReportSubmittedEvent extends FailableEvent {
    private String mId;

    public ReportSubmittedEvent(@NonNull String id) {
        mId = id;
    }

    public ReportSubmittedEvent(@NonNull Throwable e) {
        super(e);
    }

    @Nullable
    public String getId() {
        return mId;
    }
}
