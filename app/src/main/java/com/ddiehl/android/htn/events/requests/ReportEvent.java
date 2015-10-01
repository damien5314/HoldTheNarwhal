package com.ddiehl.android.htn.events.requests;

import android.support.annotation.NonNull;

public class ReportEvent {
    private String mId;
    private String mReason;

    public ReportEvent(@NonNull String id, @NonNull String reason) {
        mId = id;
        mReason = reason;
    }


    @NonNull
    public String getId() {
        return mId;
    }

    @NonNull
    public String getReason() {
        return mReason;
    }
}
