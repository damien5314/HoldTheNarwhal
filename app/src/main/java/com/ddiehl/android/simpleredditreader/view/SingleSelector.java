package com.ddiehl.android.simpleredditreader.view;

public class SingleSelector {
    private int mSelectedPosition = -1;
    private boolean mIsSelectable;

    public void setItemSelected(int position, boolean b) {
        if (b)
            mSelectedPosition = position;
        else
            mSelectedPosition = -1;
    }

    public boolean isItemSelected(int position) {
        return mSelectedPosition == position;
    }

    public void setSelectable(boolean b) {
        mIsSelectable = b;
    }

    public boolean isSelectable() {
        return mIsSelectable;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }
}
