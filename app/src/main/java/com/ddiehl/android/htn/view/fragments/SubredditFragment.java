package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.SubredditPresenter;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.reddit.listings.Link;

public class SubredditFragment extends AbsListingsFragment {
  private static final String ARG_SUBREDDIT = "arg_subreddit";
  private static final String ARG_SORT = "arg_sort";

  public SubredditFragment() { }

  public static SubredditFragment newInstance(@Nullable String subreddit, @Nullable String sort) {
    Bundle args = new Bundle();
    args.putString(ARG_SUBREDDIT, subreddit);
    args.putString(ARG_SORT, sort);
    SubredditFragment fragment = new SubredditFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    String subreddit = args.getString(ARG_SUBREDDIT);
    String sort = args.getString(ARG_SORT);
    if (TextUtils.isEmpty(sort)) sort = "hot";
    mListingsPresenter = new SubredditPresenter(mMainView, this, subreddit, sort, "all");
    mListingsAdapter = new ListingsAdapter(mListingsPresenter);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.listings_fragment, container, false);
    instantiateListView(v);
    updateTitle();
    return v;
  }

  @Override
  public void updateTitle() {
    String subreddit = mListingsPresenter.getSubreddit();
    if (subreddit != null) {
      if (subreddit.equals("random")) {
        if (mListingsPresenter.getNumListings() > 0) {
          subreddit = ((Link) mListingsPresenter.getListing(0)).getSubreddit();
        }
      }
      mMainView.setTitle(String.format(getString(R.string.link_subreddit), subreddit));
    } else {
      mMainView.setTitle(getString(R.string.front_page_title));
    }
  }
}
