package com.ddiehl.android.simpleredditreader.events.requests;

public class SaveEvent {
    private String mId;
    private String mCategory;
    private boolean mToSave;

    public SaveEvent(String id, String category, boolean save) {
        mId = id;
        mCategory = category;
        mToSave = save;
    }

    public String getId() {
        return mId;
    }

    public String getCategory() {
        return mCategory;
    }

    public boolean isToSave() {
        return mToSave;
    }
}
