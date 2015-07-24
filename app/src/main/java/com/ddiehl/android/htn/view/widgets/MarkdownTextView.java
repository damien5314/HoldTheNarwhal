/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.widget.TextView;

import com.ddiehl.android.htn.R;

import in.uncod.android.bypass.Bypass;

/**
 * Decorates a TextView with logic to set spans with stylization based on
 * reddit's markup syntax.
 *
 * Formatting test post
 * http://www.reddit.com/r/test/comments/2w70qo/formatting_test_post/
 *
 * ### Rules ###
 * Single line breaks are removed.
 * Lines which end in 2 spaces create a non-paragraph line break.
 *
 * Text wrapped in a single underscore (_) or asterisk (*) are italicized.
 * Text wrapped in double underscores (__) or double asterisks (**) are bolded.
 * Text wrapped in double tildes (~~) have a strike-through.
 *
 * Text preceded by a caret (^) is superscript, and this can be nested.
 * Text in (parentheses) preceded by a caret (^) is superscript and can be nested.
 *
 * Lines with a line of equals signs (=) below are level 1 headers.
 * Lines with a line of hyphens (-) below are level 2 headers.
 * Lines prepended with X hashtags (#) are level X headers (may also be suffixed with hashtags).
 *
 * Unordered lists are created with lines prepended by an asterisk (*), hyphen (-), or plus (+),
 *   and a space. Line openers can be mixed.
 * Ordered lists are created with lines prepended by a number plus a space. The number prepending
 *   the line is inconsequential; the list always starts with 1 and increments by 1.
 * Ordered and unordered lists may be nested.
 * List items can have multiple paragraphs by adding a line break and four spaces or a tab.
 *
 * Text wrapped in backticks (`) will be escaped from formatting and printed in fixed-width text.
 *
 * Valid links automatically should link to the formatted URL.
 * Text wrapped in brackets ([]) should be formatted as a link to the succeeding text in (parentheses).
 * Text preceded by /u/ will link to a user profile.
 * Text preceded by /r/ will link to a subreddit.
 *
 * Lines preceded by an angle bracket (>) are formatted as a quote.
 *
 * Tables are formed by lines of text delimited by pipes (|).
 * Header lines in tables are stylized in bold.
 * After the header row there is a line which defines the alignment of each column. Alignment is
 *   specified by colons with dashes in-between (:- = Left; :-: = Center; -: = Right). Left alignment
 *   is the default. Any number of dashes may be used.
 *
 * Backslashes will escape any succeeding special character.
 *
 * A line with 5 or more asterisks is a horizontal rule.
 *
 * Special characters should be decoded (e.g. &#3232; ಠ_ಠ)
 */
public class MarkdownTextView extends TextView {

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
        return super.getText();
    }

    @Override
    public void setText(CharSequence text, BufferType type) {
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
                        o.setBlockQuoteColor(c.getResources().getColor(R.color.reddit_blue));
                        _instance = new Bypass(c, o);
                    }
                }
            }
            return _instance;
        }
    }
}
