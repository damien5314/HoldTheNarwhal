/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.BaseUtils;

import java.io.InputStream;

import in.uncod.android.bypass.Bypass;

public class MarkdownTextFragment extends Fragment {
    private static final String TAG = MarkdownTextFragment.class.getSimpleName();

    public static final String ARG_TEXT = "arg_text";

    private String mText;

    public MarkdownTextFragment() { }

    public static Fragment newInstance(@NonNull String text) {
        Fragment f = new MarkdownTextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        f.setArguments(args);
        return f;
    }

    public static Fragment newInstance(@NonNull InputStream in_s) {
        String text = BaseUtils.getStringFromInputStream(in_s);
        return newInstance(text == null ? "" : text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        mText = args.getString(ARG_TEXT, "");
    }

    @Nullable @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = View.inflate(getActivity(), R.layout.fragment_markdown_text, null);
        TextView tv = (TextView) v.findViewById(R.id.about_app_text_view);

        // Bypass markdown formatting
        Bypass bp = new Bypass(getActivity());
        CharSequence formatted = bp.markdownToSpannable(mText);
        tv.setText(formatted);
        tv.setMovementMethod(LinkMovementMethod.getInstance());

        return v;
    }
}
