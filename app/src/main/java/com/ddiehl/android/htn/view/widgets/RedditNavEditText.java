package com.ddiehl.android.htn.view.widgets;

import android.content.Context;
import android.text.Editable;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.OnEditorAction;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;

public class RedditNavEditText extends EditText {

    public RedditNavEditText(Context context) {
        this(context, null);
    }

    public RedditNavEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        ButterKnife.inject(this);
    }

    private CharSequence before;

    @OnTextChanged(callback = OnTextChanged.Callback.BEFORE_TEXT_CHANGED)
    void beforeTextChanged(CharSequence s, int start, int count, int after) {
        before = s.subSequence(0, s.length());
    }

    @OnTextChanged(callback = OnTextChanged.Callback.TEXT_CHANGED)
    void onTextChanged2(CharSequence s, int start, int before, int count) {

    }

    @OnTextChanged(callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void afterTextChanged(Editable s) {
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
        if (actionId == EditorInfo.IME_ACTION_DONE) {
//            mSubmitView.performClick();
            return true;
        }
        return false;
    }
}
