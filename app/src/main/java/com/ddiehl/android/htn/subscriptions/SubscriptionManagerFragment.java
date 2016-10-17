package com.ddiehl.android.htn.subscriptions;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.adapters.SimpleItemTouchHelperCallback;
import com.ddiehl.android.htn.view.fragments.BaseFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import rx.Observable;
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

    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.swipe_refresh_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recycler_view) RecyclerView mRecyclerView;

    SubscriptionManagerAdapter mAdapter;
    SubscriptionManagerPresenter mPresenter;
    String mNextPageId;

    Observable<ListingResponse> mGetSubscriptionsObservable;

    @Override
    protected int getLayoutResId() {
        return R.layout.subscription_manager_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);

        mPresenter = new SubscriptionManagerPresenter();

        getActivity().getWindow().getDecorView()
                .setBackgroundColor(ContextCompat.getColor(getContext(), R.color.white));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showTabs(false);

        mSwipeRefreshLayout.setOnRefreshListener(onSwipeRefresh());

        initListView(mRecyclerView);
    }

    SwipeRefreshLayout.OnRefreshListener onSwipeRefresh() {
        return () -> {
            // Cancel refreshing state and refresh data
            mSwipeRefreshLayout.setRefreshing(false);
            refreshData();
        };
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }

    void initListView(RecyclerView recyclerView) {
        // Initialize layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Initialize adapter
        SubscriptionManagerAdapter adapter = new SubscriptionManagerAdapter();
        recyclerView.setAdapter(adapter);

        // Add scroll listener for fetching more items
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                final int first = layoutManager.findFirstVisibleItemPosition();
                final int last = layoutManager.findLastVisibleItemPosition();

                if (first != 0 && last == mAdapter.getItemCount() - 1
                        && mNextPageId != null) {
                    requestNextPage();
                }
            }
        });

        // Add touch helper for handling swipe gestures
        ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
        ItemTouchHelper helper = new ItemTouchHelper(callback);
        helper.attachToRecyclerView(recyclerView);

        // Cache adapter
        mAdapter = adapter;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (!mAdapter.hasData()) {
            requestNextPage();
        }
    }

    void refreshData() {
        mNextPageId = null;
        mAdapter.clearData();
        requestNextPage();
    }

    void requestNextPage() {
        if (mGetSubscriptionsObservable == null) {
            loadSubscriptions("subscriber", null, mNextPageId);
        }
    }

    void loadSubscriptions(@NonNull String where, @Nullable String before, @Nullable String after) {
        mGetSubscriptionsObservable = mPresenter.getSubscriptions(where, before, after)
                .doOnSubscribe(() -> showSpinner(null))
                .doOnUnsubscribe(() -> {
                    dismissSpinner();
                    mGetSubscriptionsObservable = null;
                });
        mGetSubscriptionsObservable
                .subscribe(onSubscriptionsLoaded(), onSubscriptionsLoadError());
    }

    Action1<Throwable> onSubscriptionsLoadError() {
        return throwable -> {
            Timber.e(throwable, "Error loading subreddit subscriptions");
            showError(throwable, getString(R.string.subscriptions_load_failed));
        };
    }

    Action1<ListingResponse> onSubscriptionsLoaded() {
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
