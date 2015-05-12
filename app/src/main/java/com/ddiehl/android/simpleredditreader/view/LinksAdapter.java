package com.ddiehl.android.simpleredditreader.view;


import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.presenter.LinksPresenter;

public class LinksAdapter extends RecyclerView.Adapter<LinkViewHolder> {

    private LinksPresenter mLinksPresenter;
    private LinksView mLinksView;

    public LinksAdapter(LinksPresenter presenter, LinksView view) {
        mLinksPresenter = presenter;
        mLinksView = view;
    }

    @Override
    public LinkViewHolder onCreateViewHolder(ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.link_item, parent, false);
        return new LinkViewHolder(view, mLinksPresenter);
    }

    @Override
    public void onBindViewHolder(LinkViewHolder linkHolder, int i) {
        RedditLink link = mLinksPresenter.getLink(i);
        linkHolder.bind(link, false);
    }

    @Override
    public int getItemCount() {
        return mLinksPresenter.getNumLinks();
    }
}
