package com.ddiehl.android.htn.view;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.markdown.MarkdownParser;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStream;

import javax.inject.Inject;

public class MarkdownTextFragment extends Fragment {

    public static final String ARG_TEXT = "arg_text";

    @Inject @Nullable MarkdownParser mMarkdownParser;

    private String mText;

    public static Fragment newInstance(@NotNull String text) {
        Fragment f = new MarkdownTextFragment();
        Bundle args = new Bundle();
        args.putString(ARG_TEXT, text);
        f.setArguments(args);
        return f;
    }

    public static Fragment newInstance(@NotNull InputStream in_s) {
        String text = Utils.getStringFromInputStream(in_s);
        return newInstance(text);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);

        Bundle args = getArguments();
        mText = args.getString(ARG_TEXT, "");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = View.inflate(getActivity(), R.layout.fragment_markdown_text, null);
        TextView tv = (TextView) v.findViewById(R.id.markdown_text_view);

        if (mMarkdownParser != null) {
            CharSequence formatted = mMarkdownParser.convert(mText);
            tv.setText(formatted);
        } else {
            tv.setText(mText);
        }

        return v;
    }
}
