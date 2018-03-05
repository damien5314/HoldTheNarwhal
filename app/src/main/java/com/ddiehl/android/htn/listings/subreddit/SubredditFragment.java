package com.ddiehl.android.htn.listings.subreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.ChooseTimespanDialog;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsActivity;
import com.ddiehl.android.htn.listings.links.ChooseLinkSortDialog;
import com.ddiehl.android.htn.listings.profile.UserProfileFragment;
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostActivity;
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostFragment;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.Link;
import rxreddit.model.Subreddit;

import static android.app.Activity.RESULT_OK;

@FragmentWithArgs
public class SubredditFragment extends BaseListingsFragment implements SubredditView {

    public static final String TAG = SubredditFragment.class.getSimpleName();

    @Inject protected SettingsManager mSettingsManager;

    @Arg(required = false) String mSubredditName;
    @Arg(required = false) String mSort;
    @Arg(required = false) String mTimespan;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.submit_new_post)
    FloatingActionButton mSubmitNewPostButton;

    SubredditPresenter mSubredditPresenter;

    // Cache for sort selected before showing timespan dialog
    private String mSelectedSort;

    @Override
    protected int getLayoutResId() {
        return R.layout.listings_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);

        if (TextUtils.isEmpty(mSort)) mSort = "hot";
        if (TextUtils.isEmpty(mTimespan)) mTimespan = "all";

        SubredditPresenter presenter = new SubredditPresenter(this, redditNavigationView, this);
        mSubredditPresenter = presenter;
        setListingsPresenter(presenter);
        setCallbacks(presenter);
    }

    @NotNull @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        ButterKnife.bind(this, view);

        // Show submit button if we're on a valid subreddit
        boolean showSubmitButton = mSubredditName != null && !"all".equals(mSubredditName);
        mSubmitNewPostButton.setVisibility(showSubmitButton ? View.VISIBLE : View.GONE);

        return view;
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTitle();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Load subreddit image into drawer header
        Subreddit subredditInfo = mSubredditPresenter.getSubreddit();
        if (subredditInfo != null) {
            String url = subredditInfo.getHeaderImageUrl();
            loadImageIntoDrawerHeader(url);
        }
    }

    @Override
    public void onPause() {
        // Clear subreddit image from drawer header
        loadImageIntoDrawerHeader(null);

        super.onPause();
    }

    private ListingsAdapter listingsAdapter;

    @NotNull @Override
    public ListingsAdapter getListingsAdapter() {
        if (listingsAdapter == null) {
            listingsAdapter = new ListingsAdapter(listingsPresenter, this, null, null);
        }
        return listingsAdapter;
    }

    private void updateTitle() {
        if (getSubreddit() == null) {
            setTitle(R.string.front_page_title);
        } else {
            String formatter = getString(R.string.link_subreddit);
            setTitle(String.format(formatter, getSubreddit()));
        }
    }

    @OnClick(R.id.submit_new_post)
    void onSubmitNewPostClicked() {
        Intent intent = SubmitPostActivity.getIntent(getContext(), mSubredditName);
        startActivityForResult(intent, REQUEST_SUBMIT_NEW_POST);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int position) {
        super.notifyItemChanged(position);
    }

    @Override
    public void notifyItemInserted(int position) {
        super.notifyItemInserted(position);
        if ("random".equals(getSubreddit())) {
            mSubredditName = ((Link) getListingsPresenter().getListingAt(0)).getSubreddit();
            updateTitle();
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        super.notifyItemRemoved(position);
    }

    @Override
    public void notifyItemRangeChanged(int position, int count) {
        super.notifyItemRangeChanged(position, count);
    }

    @Override
    public void notifyItemRangeInserted(int position, int count) {
        super.notifyItemRangeInserted(position, count);
    }

    @Override
    public void notifyItemRangeRemoved(int position, int count) {
        super.notifyItemRangeRemoved(position, count);
    }

    @Override
    public String getSubreddit() {
        return mSubredditName;
    }

    @Override
    public String getSort() {
        return mSort;
    }

    @Override
    public String getTimespan() {
        return mTimespan;
    }

    @NotNull @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }

    //region Options menu

    /**
     * Same implementation in {@link UserProfileFragment}
     */

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.listings_subreddit, menu);

        hideTimespanOptionIfUnsupported(menu, mSort);
        showSubscribeOptions(menu);
    }

    private void showSubscribeOptions(@NonNull Menu menu) {
        final MenuItem subscribeMenuItem = menu.findItem(R.id.action_subreddit_subscribe);
        final MenuItem unsubscribeMenuItem = menu.findItem(R.id.action_subreddit_unsubscribe);

        if (mSubredditName == null) {
            subscribeMenuItem.setVisible(false);
            unsubscribeMenuItem.setVisible(false);
        } else {
            final Boolean userIsSubscriber =
                    mSubredditPresenter.getSubreddit().getUserIsSubscriber();
            subscribeMenuItem.setVisible(!userIsSubscriber);
            unsubscribeMenuItem.setVisible(userIsSubscriber);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_sort:
                showSortOptionsMenu();
                return true;
            case R.id.action_change_timespan:
                showTimespanOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortOptionsMenu() {
        ChooseLinkSortDialog chooseLinkSortDialog =
                ChooseLinkSortDialog.newInstance(mSort);
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(getFragmentManager(), ChooseLinkSortDialog.TAG);
    }

    private void showTimespanOptionsMenu() {
        ChooseTimespanDialog chooseTimespanDialog =
                ChooseTimespanDialog.newInstance(mTimespan);
        chooseTimespanDialog.setTargetFragment(this, REQUEST_CHOOSE_TIMESPAN);
        chooseTimespanDialog.show(getFragmentManager(), ChooseTimespanDialog.TAG);
    }

    @Override
    public void showNsfwWarningDialog() {
        NsfwWarningDialog dialog = new NsfwWarningDialog();
        dialog.setTargetFragment(this, REQUEST_NSFW_WARNING);
        dialog.show(getFragmentManager(), NsfwWarningDialog.TAG);
    }

    @Override
    public void onRandomSubredditLoaded(String randomSubreddit) {
        mSubredditName = randomSubreddit;

        String formatter = getString(R.string.link_subreddit);
        setTitle(String.format(formatter, getSubreddit()));
    }

    @Override
    public void loadHeaderImage() {

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == RESULT_OK) {
                    String sort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    onSortSelected(sort);
                }
                break;
            case REQUEST_CHOOSE_TIMESPAN:
                if (resultCode == RESULT_OK) {
                    String timespan = data.getStringExtra(ChooseTimespanDialog.EXTRA_TIMESPAN);
                    onTimespanSelected(timespan);
                }
                break;
            case REQUEST_NSFW_WARNING:
                boolean result = resultCode == RESULT_OK;
                onNsfwSelected(result);
                break;
            case REQUEST_SUBMIT_NEW_POST:
                if (data != null) {
                    String subreddit = data.getStringExtra(SubmitPostFragment.EXTRA_SUBMIT_SUBREDDIT);
                    String id = data.getStringExtra(SubmitPostFragment.EXTRA_SUBMIT_ID);
                    onPostSubmitted(subreddit, id);
                }
                break;
        }
    }

    private void onNsfwSelected(boolean allowed) {
        if (allowed) {
            mSettingsManager.setOver18(true);
            getListingsPresenter().refreshData();
        } else {
            dismissSpinner();
            finish();
        }
    }

    private void onSortSelected(@NotNull String sort) {
        if (sort.equals(mSort)) return;

        if (sort.equals("top") || sort.equals("controversial")) {
            mSelectedSort = sort;
            showTimespanOptionsMenu();
        } else {
            mSort = sort;
            getActivity().invalidateOptionsMenu();
            getListingsPresenter().onSortChanged();
        }
    }

    private void onTimespanSelected(@NotNull String timespan) {
        mSort = mSelectedSort;
        mTimespan = timespan;
        getActivity().invalidateOptionsMenu();
        getListingsPresenter().onSortChanged();
    }

    private void onPostSubmitted(String subreddit, String linkId) {
        Intent intent = LinkCommentsActivity.getIntent(getContext(), subreddit, linkId, null);
        startActivity(intent);
    }

    //endregion
}
