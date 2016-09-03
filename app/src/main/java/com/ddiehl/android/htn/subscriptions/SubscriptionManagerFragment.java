package com.ddiehl.android.htn.subscriptions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import timber.log.Timber;

public class SubscriptionManagerFragment extends BaseFragment {

  public static final String TAG = SubscriptionManagerFragment.class.getSimpleName();

  public static SubscriptionManagerFragment newInstance() {
    return new SubscriptionManagerFragment();
  }

  @BindView(R.id.coordinator_layout)  CoordinatorLayout mCoordinatorLayout;
  @BindView(R.id.recycler_view)       RecyclerView mRecyclerView;

  SubscriptionManagerAdapter mAdapter;
  SubscriptionManagerPresenter mPresenter;
  String mNextPageId;

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    HoldTheNarwhal.getApplicationComponent().inject(this);
    mPresenter = new SubscriptionManagerPresenter();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.subscription_manager_fragment, container, false);
    ButterKnife.bind(this, view);
    initListView(mRecyclerView);
    return view;
  }

  @Override
  protected View getChromeView() {
    return mCoordinatorLayout;
  }

  private void initListView(RecyclerView recyclerView) {
    mAdapter = new SubscriptionManagerAdapter();
    recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
    recyclerView.setAdapter(mAdapter);
  }

  @Override
  public void onResume() {
    super.onResume();
    if (!mAdapter.hasData()) {
      loadSubscriptions();
    }
  }

  void loadSubscriptions() {
    mPresenter.getSubscriptions()
        .doOnSubscribe(() -> showSpinner(null))
        .doOnUnsubscribe(this::dismissSpinner)
        .subscribe(onSubscriptionsLoaded(), onSubscriptionsLoadError());
  }

  private Action1<Throwable> onSubscriptionsLoadError() {
    return throwable -> {
      Timber.e(throwable, "Error loading subreddit subscriptions");
      showError(throwable, getString(R.string.subscriptions_load_failed));
    };
  }

  private Action1<ListingResponse> onSubscriptionsLoaded() {
    return response -> {
      mNextPageId = response.getData().getAfter();

      // Translate list of Listings into list of Subreddits
      List<Listing> listings = response.getData().getChildren();
      List<Subreddit> subreddits = new ArrayList<>(listings.size());
      for (Listing l : listings) {
        subreddits.add((Subreddit) l);
      }

      mAdapter.addAll(subreddits);
    };
  }
}
