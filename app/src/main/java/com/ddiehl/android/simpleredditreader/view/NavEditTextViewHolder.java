package com.ddiehl.android.simpleredditreader.view;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
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

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) { // Hide keyboard
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        });
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
                inputSubreddit = inputSubreddit.trim();
                if (!inputSubreddit.equals("")) {
                    mEditText.setText("");
                    mMainPresenter.showSubreddit(inputSubreddit);
                }
            }
        });
    }
}