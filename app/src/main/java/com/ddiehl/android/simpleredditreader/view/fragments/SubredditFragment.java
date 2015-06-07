package com.ddiehl.android.simpleredditreader.view.fragments;

import android.os.Bundle;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.presenter.SubredditPresenter;
import com.ddiehl.android.simpleredditreader.view.adapters.ListingsAdapter;

public class SubredditFragment extends AbsListingsFragment {

    private static final String ARG_SUBREDDIT = "subreddit";

    public SubredditFragment() { }

    public static SubredditFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        SubredditFragment fragment = new SubredditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        String subreddit = args.getString(ARG_SUBREDDIT);
        mListingsPresenter = new SubredditPresenter(getActivity(), this, subreddit, "hot", "all");
        mListingsAdapter = new ListingsAdapter(mListingsPresenter);
    }

    @Override
    public void updateTitle() {
        String subreddit = mListingsPresenter.getSubreddit();
        setTitle(subreddit == null ?
                getString(R.string.front_page_title) :
                String.format(getString(R.string.link_subreddit), subreddit));
    }

    public void updateSubreddit(String subreddit) {
        mListingsPresenter.updateSubreddit(subreddit);
    }
}
