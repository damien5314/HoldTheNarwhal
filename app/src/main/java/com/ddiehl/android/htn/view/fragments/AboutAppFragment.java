package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.widgets.MarkdownTextView;

import java.io.InputStream;

import butterknife.ButterKnife;

public class AboutAppFragment extends MarkdownTextFragment {

  public static AboutAppFragment newInstance(@NonNull String text) {
    AboutAppFragment f = new AboutAppFragment();
    Bundle args = new Bundle();
    args.putString(ARG_TEXT, text);
    f.setArguments(args);
    return f;
  }

  public static AboutAppFragment newInstance(@NonNull InputStream in_s) {
    String text = Utils.getStringFromInputStream(in_s);
    return newInstance(text);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = super.onCreateView(inflater, container, savedInstanceState);
    if (v == null) return null;
    CharSequence ins = String.format("Version %1$s\n\nReleased %2$s\n\n",
        BuildConfig.VERSION_NAME,
        AndroidUtils.getBuildTimeFormatted(v.getContext()));
    MarkdownTextView tv = ButterKnife.findById(v, R.id.markdown_text_view);
    tv.setText(TextUtils.concat(ins, tv.getText()));
    return v;
  }
}
