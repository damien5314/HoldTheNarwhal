/*
 * Copyright (c) 2015 Damien Diehl. All rights reserved.
 */

package com.ddiehl.android.htn.text;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.style.StyleSpan;
import android.util.AttributeSet;
import android.widget.TextView;

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
public class RedditTextView extends TextView {

    public RedditTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * *check*\n\n**check**\n\n*check*\n**check**\n\n***check***\n\n^^^^I ^^^^swear ^^^^to ^^^^god ^^^^I ^^^^can ^^^^never ^^^^get ^^^^this ^^^^right ^^^^check\n\n*1\n*2\n*3\n\n* 1\n* 2\n* 3\n\n&gt;check\n\n&gt;&gt;check\n&gt;&gt;&gt;check\n\n&gt;&gt;&gt;&gt;check\n\n    System.out.println(\"check\");\n\n~~check~~\n~~~check~~~\n\n[stuff](https://reddit.com/r/test)\n\n/r/test\n\n/u/test\n\n|check1|check2|check3|\n:--------|:-----:|--------:\n|c|d|e|\n|a|b|c|\n\n48. 46.
     */
    @Override
    public CharSequence getText() {
        Spannable text = (Spannable) super.getText();

        final String LINE_DELIMITER = "\n";
        String[] lines = text.toString().split(LINE_DELIMITER);

        int lineStartIndex;
        int lineEndIndex = 0 - LINE_DELIMITER.length();
        for (String line : lines) {
            lineStartIndex = lineEndIndex + LINE_DELIMITER.length();
            lineEndIndex = lineStartIndex + line.length();

            // Process formatting at line level

            // Process formatting at character level
            for (int i = lineStartIndex; i < lineEndIndex; i++) {
                if (line.substring(i, i+2).equals("**")) {
                    int boldStartIndex = i+1;
                    for (int j = i+2; j < lineEndIndex; j++) {
                        if (line.substring(j, j+2).equals("**")) {
                            text.setSpan(new StyleSpan(Typeface.BOLD), i, j, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

                        }
                    }
                }
            }
        }

        return text;
    }
}
