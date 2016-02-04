package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.BuildConfig;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.widgets.MarkdownTextView;

import java.io.IOException;
import java.io.InputStream;

import butterknife.ButterKnife;

public class AboutAppFragment extends MarkdownTextFragment {
  public static AboutAppFragment newInstance() {
    // Get input
    String text = "";
    try {
      InputStream in_s;
      in_s = HoldTheNarwhal.getContext()
          .getAssets().open("htn_about_app.md");
      text = Utils.getStringFromInputStream(in_s);
    } catch (IOException e) { e.printStackTrace(); }
    // Pass to Fragment
    AboutAppFragment f = new AboutAppFragment();
    Bundle args = new Bundle();
    args.putString(ARG_TEXT, text);
    f.setArguments(args);
    return f;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    if (view == null) return null;
    view.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
    CharSequence ins = String.format("Version %1$s\n\nReleased %2$s\n\n",
        BuildConfig.VERSION_NAME,
        AndroidUtils.getBuildTimeFormatted(view.getContext()));
    MarkdownTextView tv = ButterKnife.findById(view, R.id.markdown_text_view);
    tv.setText(TextUtils.concat(ins, tv.getText()));
    return view;
  }
}
