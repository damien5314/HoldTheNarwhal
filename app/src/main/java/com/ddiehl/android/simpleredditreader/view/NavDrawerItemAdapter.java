package com.ddiehl.android.simpleredditreader.view;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.MainPresenter;

public class NavDrawerItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_EDIT_TEXT = 0;
    private static final int TYPE_TEXT_VIEW = 1;

    private MainPresenter mMainPresenter;

    public NavDrawerItemAdapter(MainPresenter presenter) {
        mMainPresenter = presenter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case TYPE_EDIT_TEXT:
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.navigation_drawer_edit_text_row, parent, false);
                return new NavEditTextViewHolder(v, mMainPresenter);
            case TYPE_TEXT_VIEW:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.navigation_drawer_text_view_row, parent, false);
                return new NavTextViewHolder(v, mMainPresenter);
            default:
                throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof NavEditTextViewHolder) {
            ((NavEditTextViewHolder) holder).bind();
        } else if (holder instanceof NavTextViewHolder) {
            ((NavTextViewHolder) holder).bind(position - 1);
        }
    }

    @Override
    public int getItemCount() {
        return 7;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0)
            return TYPE_EDIT_TEXT;
        return TYPE_TEXT_VIEW;
    }
}
