package com.ddiehl.android.simpleredditreader.view.widgets;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

public class RedditNavEditText extends EditText {

    public RedditNavEditText(Context context) {
        super(context);
    }

    public RedditNavEditText(Context context, AttributeSet attrs) {
        super(context, attrs);

        // Adds /r/ before any input
        addTextChangedListener(new TextWatcher() {
            CharSequence before;
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                before = s.subSequence(0, s.length());
            }

            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override public void afterTextChanged(Editable s) {
                if (s.length() == 0) { // Blank field
                    return;
                }

                if (s.length() < 3) {
                    s.insert(0, "/r/");
                    return;
                }

                if (s.length() == 3) {
                    s.clear();
                    return;
                }

                CharSequence cs = s.subSequence(0, 3);
                if (!cs.toString().equals("/r/") || s.toString().contains(" ")) {
                    s.replace(0, s.length(), before);
                }
            }
        });

        // Hide keyboard once field no longer has focus
        setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) { // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) v.getContext()
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        // Causes submit button to be clicked on keyboard "done" button click
        setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
//                    mSubmitView.performClick();
                    return true;
                }
                return false;
            }
        });
    }


}
