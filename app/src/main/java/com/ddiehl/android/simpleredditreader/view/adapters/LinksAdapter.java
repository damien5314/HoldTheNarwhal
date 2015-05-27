package com.ddiehl.android.simpleredditreader.view.adapters;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.view.viewholders.LinkViewHolder;
import com.ddiehl.reddit.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenter;

public class LinksAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LinksPresenter mLinksPresenter;

    public LinksAdapter(LinksPresenter presenter) {
        mLinksPresenter = presenter;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.link_item, parent, false);
        return new LinkViewHolder(view, mLinksPresenter);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder linkHolder, int i) {
        RedditLink link = mLinksPresenter.getLink(i);
        (((LinkViewHolder) linkHolder)).bind(link, false);
    }

    @Override
    public int getItemCount() {
        return mLinksPresenter.getNumLinks();
    }
}