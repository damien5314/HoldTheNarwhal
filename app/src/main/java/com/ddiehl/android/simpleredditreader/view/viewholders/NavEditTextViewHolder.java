package com.ddiehl.android.simpleredditreader.view.viewholders;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;

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
    }

    public void bind() {
        mSubmitView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String inputSubreddit = mEditText.getText().toString();
                if (inputSubreddit.equals(""))
                    return;

                inputSubreddit = inputSubreddit.substring(3);
                inputSubreddit = inputSubreddit.trim();
                mEditText.setText("");
                mMainPresenter.showSubreddit(inputSubreddit);
            }
        });
    }
}