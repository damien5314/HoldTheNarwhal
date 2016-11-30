package com.ddiehl.android.htn.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import com.ddiehl.android.htn.HoldTheNarwhal;

import java.util.regex.Pattern;

import javax.inject.Inject;

import in.uncod.android.bypass.Bypass;

public class MarkdownTextView extends AppCompatTextView {

    @Inject @Nullable Bypass mBypass;

    private CharSequence mRawText;

    public MarkdownTextView(Context context) {
        this(context, null);
    }

    public MarkdownTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkdownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    void init() {
        setMovementMethod(LinkMovementMethod.getInstance());
        if (isInEditMode()) return;
        HoldTheNarwhal.getApplicationComponent().inject(this);
    }

    @Override
    public CharSequence getText() {
        if (isInEditMode()) return super.getText();
        return mRawText;
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
        mRawText = text;

        if (isInEditMode() || mBypass == null) {
            super.setText(text, type);
        } else {
            CharSequence formatted = mBypass.markdownToSpannable(text.toString());
            SpannableString s = SpannableString.valueOf(formatted);

            // Add links for /r/ and /u/ patterns
            Linkify.addLinks(
                    s, Pattern.compile("\\s/*[ru](ser)*/[^ \n]*"), "https://www.reddit.com", null,
                    (match, url) -> {
                        url = url.trim();
                        if (!url.startsWith("/")) url = "/" + url;
                        return url;
                    }
            );

            // Add links missing protocol
            Linkify.addLinks(
                    s, Pattern.compile("\\swww\\.[^ \\n]*"), "http://", null,
                    (match, url) -> {
                        return url.trim();
                    }
            );

            // Add links with any protocol
            Linkify.addLinks(s, Pattern.compile("[a-z]+://[^ \\n]*"), null);

            super.setText(s, type);
        }
    }
}
