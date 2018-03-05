package com.ddiehl.android.htn.navigation;

import android.content.Context;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;

public class SubredditEditText extends AppCompatEditText {

    private static final String SUBREDDIT_PREFIX = "r/";

    TextWatcher textChangedListener;

    public SubredditEditText(Context context) {
        this(context, null);
        init();
    }

    public SubredditEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        ButterKnife.bind(this);
        init();
    }

    void init() {
        textChangedListener = getTextChangedListener();
        addTextChangedListener(textChangedListener);
    }

    TextWatcher getTextChangedListener() {
        return new TextWatcher() {
            private CharSequence before;

            @Override
            public void beforeTextChanged(CharSequence input, int start, int count, int after) {
                before = input.subSequence(0, input.length());
            }

            @Override
            public void onTextChanged(CharSequence input, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable input) {
                // Blank field
                if (input.length() == 0) {
                    return;
                }

                // Trim single spaces
                if (input.toString().equals(" ")) {
                    removeTextChangedListener(textChangedListener);
                    input.clear();
                    addTextChangedListener(textChangedListener);
                    return;
                }

                // If input doesn't start with the subreddit link prefix, add it
                if (!input.toString().startsWith(SUBREDDIT_PREFIX)) {
                    removeTextChangedListener(textChangedListener);
                    input.insert(0, SUBREDDIT_PREFIX);
                    addTextChangedListener(textChangedListener);
                    return;
                }

                // If input is length 3 ("/r/"), just clear the field
                if (input.length() == SUBREDDIT_PREFIX.length()) {
                    removeTextChangedListener(textChangedListener);
                    input.clear();
                    addTextChangedListener(textChangedListener);
                    return;
                }

                // Extra check if user typed a space
                if (!input.toString().subSequence(0, SUBREDDIT_PREFIX.length()).equals(SUBREDDIT_PREFIX)
                        || input.toString().contains(" ")) {
                    removeTextChangedListener(textChangedListener);
                    input.replace(0, input.length(), before);
                    addTextChangedListener(textChangedListener);
                }
            }
        };
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

    public String getInput() {
        String input = getText().toString();
        if (input.equals("")) {
            return "";
        } else {
            // Trim the "r/" portion of the input field
            input = input.substring(SUBREDDIT_PREFIX.length());
            return input;
        }
    }
}
