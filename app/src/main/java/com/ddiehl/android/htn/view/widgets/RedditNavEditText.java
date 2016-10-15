package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

public class RedditNavEditText extends AppCompatEditText {

    public RedditNavEditText(Context context) {
        this(context, null);
    }

    public RedditNavEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        ButterKnife.bind(this);
    }

    private CharSequence before;

    @OnTextChanged(callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    void beforeTextChanged(CharSequence input, int start, int count, int after) {
        before = input.subSequence(0, input.length());
    }

    @OnTextChanged(callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable input) {
        if (input.length() == 0) { // Blank field
            return;
        }

        if (!input.toString().startsWith("/r/")) {
            input.insert(0, "/r/");
            return;
        }

        if (input.length() == 3) {
            input.clear();
            return;
        }

        CharSequence cs = input.subSequence(0, 3);
        if (!cs.toString().equals("/r/") || input.toString().contains(" ")) {
            input.replace(0, input.length(), before);
        }
    }

    @OnFocusChange
    void onFocusChange(View v, boolean hasFocus) {
        if (!hasFocus) { // Hide keyboard
            InputMethodManager imm = (InputMethodManager) v.getContext()
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    // Causes submit button to be clicked on keyboard "done" button click
    @OnEditorAction
    boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return actionId == EditorInfo.IME_ACTION_DONE;
    }
}
