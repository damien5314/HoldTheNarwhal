package com.ddiehl.android.htn.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.view.markdown.MarkdownParser;

import javax.inject.Inject;

public class MarkdownTextView extends AppCompatTextView {

    @Inject @Nullable MarkdownParser mMarkdownParser;

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

        if (isInEditMode() || mMarkdownParser == null) {
            super.setText(text, type);
        } else {
            CharSequence formatted = mMarkdownParser.convert(text.toString());
            super.setText(formatted, type);
        }
    }
}
