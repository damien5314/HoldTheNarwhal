package com.ddiehl.android.simpleredditreader.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.MainPresenter;

public class NavEditTextViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;
    private EditText mEditText;
    private View mSubmitView;

    private MainPresenter mMainPresenter;

    public NavEditTextViewHolder(View itemView, MainPresenter presenter) {
        super(itemView);
        mContext = itemView.getContext();
        mMainPresenter = presenter;
        mEditText = (EditText) itemView.findViewById(R.id.drawer_navigate_to_subreddit_text);
        mSubmitView = itemView.findViewById(R.id.drawer_navigate_to_subreddit_go);

        // Adds /r/ before any input
        mEditText.addTextChangedListener(new TextWatcher() {
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
        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) { // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });

        // Causes submit button to be clicked on keyboard "done" button click
        mEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    mSubmitView.performClick();
                    return true;
                }
                return false;
            }
        });
    }

    public void bind() {
        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputSubreddit = mEditText.getText().toString();
                inputSubreddit = inputSubreddit.substring(3);
                inputSubreddit = inputSubreddit.trim();
                if (!inputSubreddit.equals("")) {
                    mEditText.setText("");
                    mMainPresenter.showSubreddit(inputSubreddit);
                }
            }
        });
    }
}