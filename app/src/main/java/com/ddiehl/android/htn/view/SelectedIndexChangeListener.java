package com.ddiehl.android.htn.view;

import android.widget.CompoundButton;

public abstract class SelectedIndexChangeListener implements CompoundButton.OnCheckedChangeListener {
    int index = -1;

    public SelectedIndexChangeListener(int index) {
        this.index = index;
    }

    public int getSelectedIndex() {
        return index;
    }
}