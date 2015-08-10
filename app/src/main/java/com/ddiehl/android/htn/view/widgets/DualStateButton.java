/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class DualStateButton extends Button {

    private State mState = State.NEGATIVE;

    private OnClickListener mCurrentOnClickListener;
    private OnClickListener mPositiveOnClickListener;
    private OnClickListener mNegativeOnClickListener;

    public DualStateButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
//        throw new UnsupportedOperationException("setOnClickListener not supported for " +
//                "DualStateButton. Please use setPositiveOnClickListener or " +
//                "setNegativeOnClickListener.");
    }

    public void setState(State s) {
        mState = s;
        if (s == State.POSITIVE) {
            setOnClickListener(mPositiveOnClickListener);
        } else { // State = NEGATIVE
            setOnClickListener(mNegativeOnClickListener);
        }
    }

    public void setPositiveOnClickListener(OnClickListener l) {
        mPositiveOnClickListener = l;
    }

    public void setNegativeOnClickListener(OnClickListener l) {
        mNegativeOnClickListener = l;
    }

    public enum State {
        POSITIVE, NEGATIVE
    }
}
