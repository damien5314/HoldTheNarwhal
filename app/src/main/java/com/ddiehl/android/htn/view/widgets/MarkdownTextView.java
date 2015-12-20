package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.text.method.LinkMovementMethod;
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
    if (isInEditMode()) {
      super.setText(text, type);
    } else {
      text = linkify(text.toString());
      mRawText = text;
      Bypass b = BypassWrapper.getInstance(getContext());
      CharSequence formatted = b.markdownToSpannable(text.toString());
      super.setText(formatted, type);
    }
  }

  private CharSequence linkify(CharSequence markdown) {
//		markdown = surroundUrlWithBrackets(markdown);
    markdown = linkifyUrl(markdown, "[a-z]+://[^ \\n]*");
//    markdown = linkifyUrl(markdown, "(?i)\\b((?:https?:(?:/{1,3}|[a-z0-9%])|[a-z0-9.\\-]+[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)/)(?:[^\\s()<>{}\\[\\]]+|\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|\\([^\\s]+?\\))+(?:\\([^\\s()]*?\\([^\\s()]+\\)[^\\s()]*?\\)|\\([^\\s]+?\\)|[^\\s`!()\\[\\]{};:'\".,<>?«»“”‘’])|(?:(?<!@)[a-z0-9]+(?:[.\\-][a-z0-9]+)*[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)\\b/?(?!@)))\n");
//    markdown = linkifySubreddit(markdown, "\\A[:blank:]/r/.*[a-zA-z]/");
//    markdown = linkifySubreddit(markdown, "(\\s)/r/.*[a-zA-z]/");
//    markdown = linkifySubreddit(markdown, "(\\s)/r/.*[-a-zA-Z0-9@:%_\\+.~#?&//=]/");
    return markdown;
  }

  private CharSequence linkifySubreddit(CharSequence markdown, String regex) {
    Pattern p = Pattern.compile(regex);
    Matcher m = p.matcher(markdown);
    List<Integer> bounds = new ArrayList<>();
    while (m.find()) {
      int start = m.start(); // Inclusive
      int end = m.end(); // Exclusive
      if ((start == 0 || markdown.charAt(start-1) != '<') &&
          (end < markdown.length() - 1 && markdown.charAt(end-1) != '>')) {
        while (markdown.charAt(start) == '\n' || markdown.charAt(start) == ' ') start++;
        bounds.add(start);
        bounds.add(end);
      }
    }
    // Insert links from saved bounds
    StringBuilder result = new StringBuilder(markdown);
    for (int i = bounds.size() - 2; i >= 0; i -= 2) {
      result.insert(bounds.get(i+1), "](https://www.reddit.com" + result.substring(bounds.get(i), bounds.get(i+1)) + ")");
      result.insert(bounds.get(i), "[");
    }
    return result;
  }

  // [-a-zA-Z0-9@:%._\+~#=]{2,256}\.[a-z]{2,6}\b([-a-zA-Z0-9@:%_\+.~#?&//=]*)
  // (?bhttp://[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]
  // /((([A-Za-z]{3,9}:(?:\/\/)?)(?:[-;:&=\+\$,\w]+@)?[A-Za-z0-9.-]+|(?:www.|[-;:&=\+\$,\w]+@)[A-Za-z0-9.-]+)((?:\/[\+~%\/.\w-_]*)?\??(?:[-\+=&;%@.\w_]*)#?(?:[\w]*))?)/
  // (?i)\b((?:https?:(?:/{1,3}|[a-z0-9%])|[a-z0-9.\-]+[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)/)(?:[^\s()<>{}\[\]]+|\([^\s()]*?\([^\s()]+\)[^\s()]*?\)|\([^\s]+?\))+(?:\([^\s()]*?\([^\s()]+\)[^\s()]*?\)|\([^\s]+?\)|[^\s`!()\[\]{};:'".,<>?«»“”‘’])|(?:(?<!@)[a-z0-9]+(?:[.\-][a-z0-9]+)*[.](?:com|net|org|edu|gov|mil|aero|asia|biz|cat|coop|info|int|jobs|mobi|museum|name|post|pro|tel|travel|xxx|ac|ad|ae|af|ag|ai|al|am|an|ao|aq|ar|as|at|au|aw|ax|az|ba|bb|bd|be|bf|bg|bh|bi|bj|bm|bn|bo|br|bs|bt|bv|bw|by|bz|ca|cc|cd|cf|cg|ch|ci|ck|cl|cm|cn|co|cr|cs|cu|cv|cx|cy|cz|dd|de|dj|dk|dm|do|dz|ec|ee|eg|eh|er|es|et|eu|fi|fj|fk|fm|fo|fr|ga|gb|gd|ge|gf|gg|gh|gi|gl|gm|gn|gp|gq|gr|gs|gt|gu|gw|gy|hk|hm|hn|hr|ht|hu|id|ie|il|im|in|io|iq|ir|is|it|je|jm|jo|jp|ke|kg|kh|ki|km|kn|kp|kr|kw|ky|kz|la|lb|lc|li|lk|lr|ls|lt|lu|lv|ly|ma|mc|md|me|mg|mh|mk|ml|mm|mn|mo|mp|mq|mr|ms|mt|mu|mv|mw|mx|my|mz|na|nc|ne|nf|ng|ni|nl|no|np|nr|nu|nz|om|pa|pe|pf|pg|ph|pk|pl|pm|pn|pr|ps|pt|pw|py|qa|re|ro|rs|ru|rw|sa|sb|sc|sd|se|sg|sh|si|sj|Ja|sk|sl|sm|sn|so|sr|ss|st|su|sv|sx|sy|sz|tc|td|tf|tg|th|tj|tk|tl|tm|tn|to|tp|tr|tt|tv|tw|tz|ua|ug|uk|us|uy|uz|va|vc|ve|vg|vi|vn|vu|wf|ws|ye|yt|yu|za|zm|zw)\b/?(?!@)))
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
