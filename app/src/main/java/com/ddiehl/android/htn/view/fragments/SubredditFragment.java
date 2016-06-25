package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.text.TextUtils;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.ListingsPresenter;
import com.ddiehl.android.htn.presenter.SubredditPresenter;
import com.ddiehl.android.htn.view.LinkView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

@FragmentWithArgs
public class SubredditFragment extends BaseListingsFragment implements LinkView {

  public static final String TAG = SubredditFragment.class.getSimpleName();

  @Arg String mSubreddit;
  @Arg String mSort;
  @Arg String mTimespan;

  public SubredditFragment() { }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FragmentArgs.inject(this);
    if (TextUtils.isEmpty(mSort)) mSort = "hot";
    if (TextUtils.isEmpty(mTimespan)) mTimespan = "all";
    mLinkPresenter = new SubredditPresenter(mMainView, this, this, mSubreddit, mSort, mTimespan);
    mListingsPresenter = (ListingsPresenter) mLinkPresenter;
    mCallbacks = (Callbacks) mListingsPresenter;
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment;
  }

  @Override
  public void onPause() {
    mSubreddit = mListingsPresenter.getSubreddit();
    mSort = mListingsPresenter.getSort();
    mTimespan = mListingsPresenter.getTimespan();
    super.onPause();
  }

  @Override
  public ListingsAdapter getListingsAdapter() {
    return new ListingsAdapter(
        mListingsPresenter, mLinkPresenter, mCommentPresenter, mMessagePresenter);
  }
}
