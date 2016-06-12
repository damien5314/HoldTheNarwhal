package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.presenter.SubredditPresenter;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;

public class SubredditFragment extends BaseListingsFragment implements LinkView {

  public static final String TAG = SubredditFragment.class.getSimpleName();

  private static final String ARG_SUBREDDIT = "arg_subreddit";
  private static final String ARG_SORT = "arg_sort";
  private static final String ARG_TIMESPAN = "arg_timespan";

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
    String timespan = args.getString(ARG_TIMESPAN);
    if (TextUtils.isEmpty(timespan)) timespan = "all";
    mLinkPresenter = new SubredditPresenter(mMainView, this, this, subreddit, sort, timespan);
    mListingsPresenter = (ListingsPresenter) mLinkPresenter;
    mCallbacks = (Callbacks) mListingsPresenter;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment;
  }

  @Override
  public void onPause() {
    getArguments().putString(ARG_SUBREDDIT, mListingsPresenter.getSubreddit());
    getArguments().putString(ARG_SORT, mListingsPresenter.getSort());
    getArguments().putString(ARG_TIMESPAN, mListingsPresenter.getTimespan());
    super.onPause();
  }

  @Override
  public ListingsAdapter getListingsAdapter() {
    return new ListingsAdapter(
        mListingsPresenter, mLinkPresenter, mCommentPresenter, mMessagePresenter);
  }
}
