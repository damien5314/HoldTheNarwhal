package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.SubredditPresenter;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.reddit.listings.Link;

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
    mListingsPresenter = new SubredditPresenter(mMainView, this, subreddit, "hot", "all");
    mListingsAdapter = new ListingsAdapter(mListingsPresenter);
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.listings_fragment, container, false);
    instantiateListView(v);
    updateTitle();
    return v;
  }

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
