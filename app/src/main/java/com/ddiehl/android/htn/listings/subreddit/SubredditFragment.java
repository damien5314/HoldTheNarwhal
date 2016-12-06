package com.ddiehl.android.htn.listings.subreddit;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CoordinatorLayout;
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
import com.ddiehl.android.htn.listings.links.ChooseLinkSortDialog;
import com.ddiehl.android.htn.listings.profile.UserProfileFragment;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rxreddit.model.Link;
import rxreddit.model.Subreddit;

import static android.app.Activity.RESULT_OK;

@FragmentWithArgs
public class SubredditFragment extends BaseListingsFragment implements SubredditView {

    public static final String TAG = SubredditFragment.class.getSimpleName();

    @Inject protected SettingsManager mSettingsManager;

    @Arg(required = false) String mSubreddit;
    @Arg(required = false) String mSort;
    @Arg(required = false) String mTimespan;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

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

        SubredditPresenter presenter = new SubredditPresenter(this, mRedditNavigationView, this);
        mSubredditPresenter = presenter;
        mListingsPresenter = presenter;
        mCallbacks = presenter;
    }

    @NonNull @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        updateTitle();
    }

    @Override
    public void onStart() {
        super.onStart();

        // Load subreddit image into drawer header
        Subreddit subredditInfo = mSubredditPresenter.getSubredditInfo();
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

    @Override
    public ListingsAdapter getListingsAdapter() {
        return new ListingsAdapter(
                mListingsPresenter, this, null, null);
    }

    private void updateTitle() {
        if (getSubreddit() == null) {
            setTitle(R.string.front_page_title);
        } else {
            String formatter = getString(R.string.link_subreddit);
            setTitle(String.format(formatter, getSubreddit()));
        }
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
            mSubreddit = ((Link) mListingsPresenter.getListingAt(0)).getSubreddit();
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
        return mSubreddit;
    }

    @Override
    public String getSort() {
        return mSort;
    }

    @Override
    public String getTimespan() {
        return mTimespan;
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }

    //region Options menu

    /**
     * Same implementation in {@link UserProfileFragment}
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Disable timespan option if current sort does not support it
        switch (mSort) {
            case "controversial":
            case "top":
                menu.findItem(R.id.action_change_timespan)
                        .setVisible(true);
                break;
            case "hot":
            case "new":
            case "rising":
            default:
                menu.findItem(R.id.action_change_timespan)
                        .setVisible(false);
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_change_sort:
                showSortOptionsMenu();
                mAnalytics.logOptionChangeSort();
                return true;
            case R.id.action_change_timespan:
                showTimespanOptionsMenu();
                mAnalytics.logOptionChangeTimespan();
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
    /** Strange way to update the view, but works for now */
    public void onRandomSubredditLoaded(String randomSubreddit) {
        mSubreddit = randomSubreddit;

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
        }
    }

    private void onNsfwSelected(boolean allowed) {
        if (allowed) {
            mSettingsManager.setOver18(true);
            mListingsPresenter.refreshData();
        } else {
            dismissSpinner();
            finish();
        }
    }

    private void onSortSelected(@NonNull String sort) {
        mAnalytics.logOptionChangeSort(sort);

        if (sort.equals(mSort)) return;

        if (sort.equals("top") || sort.equals("controversial")) {
            mSelectedSort = sort;
            showTimespanOptionsMenu();
        } else {
            mSort = sort;
            getActivity().invalidateOptionsMenu();
            mListingsPresenter.onSortChanged();
        }
    }

    private void onTimespanSelected(@NonNull String timespan) {
        mAnalytics.logOptionChangeTimespan(timespan);

        mSort = mSelectedSort;
        mTimespan = timespan;
        getActivity().invalidateOptionsMenu();
        mListingsPresenter.onSortChanged();
    }

    //endregion
}
