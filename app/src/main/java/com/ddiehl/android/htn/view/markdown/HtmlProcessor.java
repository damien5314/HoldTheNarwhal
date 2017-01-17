package com.ddiehl.android.htn.view.markdown;

import android.os.Build;
import android.text.Html;
import android.text.Spanned;

import static android.text.Html.FROM_HTML_MODE_LEGACY;

public class HtmlProcessor {

    public Spanned convert(String text) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return Html.fromHtml(text, FROM_HTML_MODE_LEGACY);
        } else {
            //noinspection deprecation
            return Html.fromHtml(text);
        }
    }
}
