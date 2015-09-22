package com.ddiehl.android.htn.events.requests;

public class ReportEvent {
    private String mId;
    private String mReason;

    public ReportEvent(String id, String reason) {
        mId = id;
        mReason = reason;
    }

    public String getId() {
        return mId;
    }

    public String getReason() {
        return mReason;
    }
}
