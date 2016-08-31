package com.ddiehl.android.htn.subscriptions;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.BaseFragment;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;
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

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
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
        .subscribe(onSubscriptionsLoaded(), onSubscriptionsLoadError());
  }

  private Action1<Throwable> onSubscriptionsLoadError() {
    return throwable -> {
      Timber.e(throwable, "Error loading subreddit subscriptions");
      // TODO show error toast
    };
  }

  private Action1<List<Subreddit>> onSubscriptionsLoaded() {
    return subscriptions -> {
      mAdapter.addAll(subscriptions);
    };
  }
}
