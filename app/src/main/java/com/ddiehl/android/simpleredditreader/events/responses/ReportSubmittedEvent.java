package com.ddiehl.android.simpleredditreader.events.responses;

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
