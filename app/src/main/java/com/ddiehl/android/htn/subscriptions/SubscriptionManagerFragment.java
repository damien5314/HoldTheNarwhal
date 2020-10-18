package com.ddiehl.android.htn.subscriptions;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.subredditinfo.SubredditInfoActivity;
import com.ddiehl.android.htn.subredditinfo.SubredditInfoFragment;
import com.ddiehl.android.htn.view.BaseFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import rxreddit.model.Listing;
import rxreddit.model.ListingResponse;
import rxreddit.model.Subreddit;
import timber.log.Timber;

import static android.app.Activity.RESULT_OK;

public class SubscriptionManagerFragment extends BaseFragment implements SubscriptionManagerView {

    public static final String TAG = SubscriptionManagerFragment.class.getSimpleName();

    private static final int REQUEST_GET_SUBREDDIT_INFO = 1000;
    private static final int REQUEST_SEARCH = 1001;

    public static SubscriptionManagerFragment newInstance() {
        return new SubscriptionManagerFragment();
    }

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(R.id.search_button)
    FloatingActionButton searchButton;

    SubscriptionManagerAdapter adapter;
    SubscriptionManagerPresenter presenter;
    String nextPageId;

    Observable<ListingResponse> getSubscriptionsObservable;
    Snackbar snackbar;

    @Override
    protected int getLayoutResId() {
        return R.layout.subscription_manager_fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("Showing subscriptions");
        HoldTheNarwhal.getApplicationComponent().inject(this);

        presenter = new SubscriptionManagerPresenter();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setTitle(R.string.subscription_manager_title);

        swipeRefreshLayout.setOnRefreshListener(onSwipeRefresh());

        initListView(recyclerView);

        searchButton.setOnClickListener(button -> {
            SubredditSearchDialog dialog = new SubredditSearchDialog();
            dialog.setTargetFragment(this, REQUEST_SEARCH);
            dialog.show(getFragmentManager(), SubredditSearchDialog.TAG);
        });
    }

    SwipeRefreshLayout.OnRefreshListener onSwipeRefresh() {
        return () -> {
            // Cancel refreshing state and refresh data
            swipeRefreshLayout.setRefreshing(false);
            refreshData();
        };
    }

    @NotNull
    @Override
    protected View getChromeView() {
        return coordinatorLayout;
    }

    void initListView(RecyclerView recyclerView) {
        // Initialize layout manager
        final LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        // Initialize adapter
        if (adapter == null) {
            SubscriptionManagerAdapter adapter = new SubscriptionManagerAdapter(this, presenter);

            // Add scroll listener for fetching more items
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    final int first = layoutManager.findFirstVisibleItemPosition();
                    final int last = layoutManager.findLastVisibleItemPosition();

                    if (first != 0 && last == SubscriptionManagerFragment.this.adapter.getItemCount() - 1
                            && nextPageId != null) {
                        requestNextPage();
                    }
                }
            });

            // Add touch helper for handling swipe gestures
            ItemTouchHelper.Callback callback = new SimpleItemTouchHelperCallback(adapter);
            ItemTouchHelper helper = new ItemTouchHelper(callback);
            helper.attachToRecyclerView(recyclerView);

            // Cache adapter
            this.adapter = adapter;
        }

        // Set to RecyclerView
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!adapter.hasData()) {
            requestNextPage();
        }
    }

    void refreshData() {
        nextPageId = null;
        adapter.clearData();
        requestNextPage();
    }

    void requestNextPage() {
        if (getSubscriptionsObservable == null) {
            loadSubscriptions("subscriber", null, nextPageId);
        }
    }

    void loadSubscriptions(@NotNull String where, @Nullable String before, @Nullable String after) {
        getSubscriptionsObservable = presenter.getSubscriptions(where, before, after)
                .doOnSubscribe(disposable -> showSpinner())
                .doFinally(() -> {
                    dismissSpinner();
                    getSubscriptionsObservable = null;
                });
        getSubscriptionsObservable
                .subscribe(this::onSubscriptionsLoaded, this::onSubscriptionsLoadError);
    }

    void onSubscriptionsLoadError(Throwable error) {
        if (error instanceof IOException) {
            String message = getString(R.string.error_network_unavailable);
            showError(message);
        } else {
            Timber.w(error, "Error loading subreddit subscriptions");
            showError(getString(R.string.subscriptions_load_failed));
        }
    }

    void onSubscriptionsLoaded(ListingResponse response) {
        nextPageId = response.getData().getAfter();

        // Translate list of Listings into list of Subreddits
        List<Listing> listings = response.getData().getChildren();
        List<Subreddit> subreddits = new ArrayList<>(listings.size());
        for (Listing l : listings) {
            subreddits.add((Subreddit) l);
        }
        Timber.i("Subscriptions loaded: %d", subreddits.size());

        adapter.addAll(subreddits);
    }

    @Override
    public void onSubredditClicked(final @NotNull Subreddit subreddit, final int position) {
        getInfo(subreddit.getDisplayName());
    }

    @Override
    public void onSubredditDismissed(final @NotNull Subreddit subreddit, final int position) {
        // Unsubscribe from subreddit
        unsubscribe(subreddit, position);
    }

    //region Unsubscribe

    void unsubscribe(final @NotNull Subreddit subreddit, final int position) {
        adapter.remove(subreddit);

        presenter.unsubscribe(subreddit)
                .doOnSubscribe(disposable -> showUnsubscribingView(subreddit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> onSubredditUnsubscribed(subreddit, position),
                        error -> onUnsubscribeError(error, subreddit, position)
                );
    }

    void showUnsubscribingView(final @NotNull Subreddit subreddit) {
        String message = getString(R.string.unsubscribing_subreddit, subreddit.getDisplayName());
        snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                SubscriptionManagerFragment.this.snackbar = null;
            }
        });
        snackbar.show();
    }

    void onSubredditUnsubscribed(final @NotNull Subreddit subreddit, int position) {
        // Dismiss unsubscribing Snackbar, if it exists
        if (snackbar != null) {
            snackbar.dismiss();
        }

        // Show Snackbar confirming unsubscribe, with an Undo button to resubscribe
        String message = getString(R.string.unsubscribed_subreddit, subreddit.getDisplayName());
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.unsubscribe_undo, view -> resubscribe(subreddit, position));
        snackbar.show();
    }

    void onUnsubscribeError(Throwable error, final @NotNull Subreddit subreddit, int position) {
        getActivity().runOnUiThread(() -> {
            // Add subreddit back into the adapter
            adapter.add(position, subreddit);

            // Show error
            if (error instanceof IOException) {
                String message = getString(R.string.error_network_unavailable);
                showError(message);
            } else {
                // Show error messaging
                Timber.w(error, "Error unsubscribing from /r/%s", subreddit.getDisplayName());
                String message = getString(R.string.unsubscribe_error, subreddit.getDisplayName());
                showError(message);
            }
        });
    }

    //endregion

    //region Resubscribe

    void resubscribe(final @NotNull Subreddit subreddit, int position) {
        presenter.subscribe(subreddit)
                .doOnSubscribe(diposable -> showResubscribingView(subreddit))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        () -> onSubredditResubscribed(subreddit, position),
                        error -> onResubscribeError(error, subreddit)
                );
    }

    void showResubscribingView(final @NotNull Subreddit subreddit) {
        String message = getString(R.string.resubscribing_subreddit, subreddit.getDisplayName());
        snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_INDEFINITE);
        snackbar.setCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar snackbar, int event) {
                SubscriptionManagerFragment.this.snackbar = null;
            }
        });
        snackbar.show();
    }

    void onSubredditResubscribed(final @NotNull Subreddit subreddit, final int position) {
        // Dismiss Snackbar, if it exists
        if (snackbar != null) {
            snackbar.dismiss();
        }

        // Add subreddit back into the adapter
        adapter.add(position, subreddit);

        // Show Snackbar confirming resubscribe, with an Undo button to unsubscribe
        String message = getString(R.string.resubscribed_subreddit, subreddit.getDisplayName());
        Snackbar snackbar = Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.resubscribe_undo, view -> unsubscribe(subreddit, position));
        snackbar.show();
    }

    void onResubscribeError(Throwable error, final @NotNull Subreddit subreddit) {
        getActivity().runOnUiThread(() -> {
            if (error instanceof IOException) {
                String message = getString(R.string.error_network_unavailable);
                showError(message);
            } else {
                Timber.w(error, "Error resubscribing to /r/%s", subreddit.getDisplayName());
                String message = getString(R.string.resubscribe_error, subreddit.getDisplayName());
                showError(message);
            }
        });
    }

    //endregion

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_SEARCH:
                if (resultCode == RESULT_OK) {
                    String subredditName = data.getStringExtra(SubredditSearchDialog.RESULT_SEARCH);
                    getInfo(subredditName);
                }
                break;
            case REQUEST_GET_SUBREDDIT_INFO:
                if (resultCode == SubredditInfoFragment.RESULT_GET_INFO_ERROR) {
                    String message = getString(R.string.error_get_subreddit_info);
                    showError(message);
                }
                break;
        }
    }

    private void getInfo(String subredditName) {
        Intent intent = new Intent(getContext(), SubredditInfoActivity.class);
        intent.putExtra(SubredditInfoActivity.EXTRA_SUBREDDIT, subredditName);
        startActivityForResult(intent, REQUEST_GET_SUBREDDIT_INFO);
    }
}
