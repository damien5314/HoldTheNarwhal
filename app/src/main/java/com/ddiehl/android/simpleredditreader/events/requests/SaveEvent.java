package com.ddiehl.android.simpleredditreader.events.requests;

public class SaveEvent {
    private String mId;
    private String mCategory;

    public SaveEvent(String id, String category) {
        mId = id;
        mCategory = category;
    }

    public String getId() {
        return mId;
    }

    public String getCategory() {
        return mCategory;
    }
}
