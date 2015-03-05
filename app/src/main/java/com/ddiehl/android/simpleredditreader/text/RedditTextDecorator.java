package com.ddiehl.android.simpleredditreader.text;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Decorates a TextView with logic to set spans with stylization based on
 * reddit's markup syntax.
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
 */
public class RedditTextDecorator extends TextView {
    private static final String TAG = RedditTextDecorator.class.getSimpleName();

    public RedditTextDecorator(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


}
