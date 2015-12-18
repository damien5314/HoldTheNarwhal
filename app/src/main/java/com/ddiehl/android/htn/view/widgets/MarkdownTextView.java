package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.htn.R;

import in.uncod.android.bypass.Bypass;


public class MarkdownTextView extends TextView {
  private CharSequence mRawText;

  public MarkdownTextView(Context context) {
    super(context);
    setMovementMethod(LinkMovementMethod.getInstance());
  }

  public MarkdownTextView(Context context, AttributeSet attrs) {
    super(context, attrs);
    setMovementMethod(LinkMovementMethod.getInstance());
  }

  public MarkdownTextView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setMovementMethod(LinkMovementMethod.getInstance());
  }

  @Override
  public CharSequence getText() {
    if (isInEditMode()) return super.getText();
    return mRawText;
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    if (isInEditMode()) {
      super.setText(text, type);
      return;
    }
    mRawText = text;
    Bypass b = BypassWrapper.getInstance(getContext());
    CharSequence formatted = b.markdownToSpannable(text.toString());
    super.setText(formatted, type);
  }

  private static class BypassWrapper {

    private static Bypass _instance;

    public static Bypass getInstance(Context c) {
      if (_instance == null) {
        synchronized (Bypass.class) {
          if (_instance == null) {
            Bypass.Options o = new Bypass.Options();
            o.setBlockQuoteColor(
                c.getResources().getColor(R.color.markdown_quote_block, c.getTheme()));
            _instance = new Bypass(c, o);
          }
        }
      }
      return _instance;
    }
  }
}
