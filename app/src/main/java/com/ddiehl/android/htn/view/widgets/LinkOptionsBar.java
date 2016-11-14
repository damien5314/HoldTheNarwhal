package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.ddiehl.android.htn.R;

public class LinkOptionsBar extends LinearLayout {

    static final @LayoutRes int LAYOUT_RES_ID = R.layout.link_options_bar;

    public LinkOptionsBar(Context context) {
        this(context, null);
    }

    public LinkOptionsBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LinkOptionsBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr, 0);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LinkOptionsBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs, defStyleAttr, defStyleRes);
    }

    void init(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        LayoutInflater.from(context)
                .inflate(LAYOUT_RES_ID, this);
    }
}
