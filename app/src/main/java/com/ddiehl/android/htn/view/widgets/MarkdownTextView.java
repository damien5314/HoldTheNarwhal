package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.Linkify;

import java.util.regex.Pattern;

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
    mRawText = text;
    if (isInEditMode()) {
      super.setText(text, type);
    } else {
      Bypass b = BypassWrapper.getInstance(getContext());
      CharSequence formatted = b.markdownToSpannable(text.toString());
      SpannableString s = SpannableString.valueOf(formatted);

      // Add links for /r/ and /u/ patterns
      Linkify.addLinks(s, Pattern.compile("\\s/*[ru](ser)*/[^ \n]*"), "https://www.reddit.com", null,
          (match, url) -> {
            url = url.trim();
            if (!url.startsWith("/")) url = "/" + url;
            return url;
          });

      // Add links missing protocol
      Linkify.addLinks(s, Pattern.compile("\\swww\\.[^ \\n]*"), "http://", null,
          (match, url) -> {
            return url.trim();
          });

      // Add links with any protocol
      Linkify.addLinks(s, Pattern.compile("[a-z]+://[^ \\n]*"), null);

      super.setText(s, type);
    }
  }

  private static class BypassWrapper {
    private static Bypass _instance;

    public static Bypass getInstance(Context c) {
      if (_instance == null) {
        synchronized (Bypass.class) {
          if (_instance == null) {
            Bypass.Options o = new Bypass.Options();
            o.setBlockQuoteColor(
                ContextCompat.getColor(c, R.color.markdown_quote_block));
            _instance = new Bypass(c, o);
          }
        }
      }
      return _instance;
    }
  }
}
