package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.htn.R;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
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
      text = linkify(text);
      Bypass b = BypassWrapper.getInstance(getContext());
      CharSequence formatted = b.markdownToSpannable(text.toString());
      SpannableString s = SpannableString.valueOf(formatted);
      Linkify.addLinks(s, Pattern.compile("\\s/*[ru](ser)*/[^ \n]*"), "https://www.reddit.com", null,
          (match, url) -> {
            url = url.trim();
            if (!url.startsWith("/")) url = "/" + url;
            return url;
          });
      super.setText(s, type);
    }
  }

  private CharSequence linkify(CharSequence markdown) {
//    markdown = linkifySubreddit(markdown, "\\s\\/*r\\/[^ \n]*");
    markdown = linkifyUrl(markdown, "[a-z]+://[^ \\n]*");
    return markdown;
  }

  private CharSequence linkifySubreddit(CharSequence markdown, String regex) {
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(markdown);
    List<Integer> bounds = new ArrayList<>();
    while (m.find()) {
      int start = m.start(); // Inclusive
      int end = m.end(); // Exclusive
      // Remove the white space
      while (markdown.charAt(start) != '/' && markdown.charAt(start) != 'r') start++;
      bounds.add(start);
      bounds.add(end);
    }
    // Insert links from saved bounds
    StringBuilder result = new StringBuilder(markdown);
    for (int i = bounds.size() - 2; i >= 0; i -= 2) {
      String s = result.substring(bounds.get(i), bounds.get(i+1));
//      result.insert(bounds.get(i+1), "](https://www.reddit.com" + s + ")");
//      result.insert(bounds.get(i), "[");
//      if (s.charAt(0) != '/') result.insert(bounds.get(i), "https://www.reddit.com/");
//      else result.insert(bounds.get(i), "https://www.reddit.com");
//      result.insert(bounds.get(i+1), "</a>");
//      result.insert(bounds.get(i), "<a href='https://www.reddit.com" + s + "'>");

    }
    return result;
  }

  private CharSequence linkifyUrl(CharSequence markdown, String regex) {
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(markdown);
    List<Integer> bounds = new ArrayList<>();
    while (m.find()) {
      int start = m.start(); // Inclusive
      int end = m.end(); // Exclusive
      // Check for a URL wrapped in parentheses, where the last parentheses was included
      if (start > 0 && markdown.charAt(start-1) == '(' && markdown.charAt(end-1) == ')') {
        end--; // Get rid of closing parentheses
      }
      if ((start == 0 || markdown.charAt(start-1) != '<')
//          && (end < markdown.length() - 1 && markdown.charAt(end-1) != '>')
          ) {
        bounds.add(start);
        bounds.add(end);
      }
    }
    // Insert brackets from saved locations
    StringBuilder result = new StringBuilder(markdown);
    for (int i = bounds.size() - 2; i >= 0; i -= 2) {
      result.insert(bounds.get(i+1), ">");
      result.insert(bounds.get(i), "<");
    }
    return result;
  }

  // http://stackoverflow.com/questions/285619/how-to-detect-the-presence-of-url-in-a-string
  private String surroundUrlWithBrackets(String markdown) {
    String [] parts = markdown.split("\\s+");
    for(int i = 0; i < parts.length; i++) {
      if (isURL(parts[i])) {
        parts[i] = "<" + parts[i] + ">";
      }
    }
    StringBuilder b = new StringBuilder();
    for (String s : parts) b.append(s).append(" ");
    markdown = b.toString();
    return markdown;
  }

  private boolean isURL(String s) {
    try {
      new URL(s);
      return true;
    } catch (MalformedURLException ignored) { }
    return false;
  }

  private static class BypassWrapper {
    private static Bypass _instance;

    public static Bypass getInstance(Context c) {
      if (_instance == null) {
        synchronized (Bypass.class) {
          if (_instance == null) {
            Bypass.Options o = new Bypass.Options();
            o.setBlockQuoteColor(
                c.getResources().getColor(R.color.markdown_quote_block));
            _instance = new Bypass(c, o);
          }
        }
      }
      return _instance;
    }
  }
}
