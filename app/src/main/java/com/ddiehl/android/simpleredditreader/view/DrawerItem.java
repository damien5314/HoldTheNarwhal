package com.ddiehl.android.simpleredditreader.view;

public class DrawerItem {
    private int mIconResourceId;
    private int mLabelResourceId;

    public DrawerItem(int id, int label) {
        mIconResourceId = id;
        mLabelResourceId = label;
    }

    public int getIconResourceId() {
        return mIconResourceId;
    }

    public int getItemLabel() {
        return mLabelResourceId;
    }
}
