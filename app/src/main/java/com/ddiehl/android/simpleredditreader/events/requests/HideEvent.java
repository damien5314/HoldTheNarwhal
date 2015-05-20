package com.ddiehl.android.simpleredditreader.events.requests;

public class HideEvent {
    private String mId;
    private boolean mToHide;

    public HideEvent(String id, boolean save) {
        mId = id;
        mToHide = save;
    }

    public String getId() {
        return mId;
    }

    public boolean isToHide() {
        return mToHide;
    }
}
