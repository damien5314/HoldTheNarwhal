package com.ddiehl.android.htn.listings.subreddit;

import android.content.Intent;
import android.os.Bundle;
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

    @Inject protected SettingsManager settingsManager;

    @Arg(required = false) String subreddit;
    @Arg(required = false) String sort;
    @Arg(required = false) String timespan;

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout coordinatorLayout;
    @BindView(R.id.submit_new_post)
    FloatingActionButton submitNewPostButton;

    SubredditPresenter subredditPresenter;

    // Cache for sort selected before showing timespan dialog
    private String selectedSort;

    @Override
    protected int getLayoutResId() {
        return R.layout.listings_fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);

        if (TextUtils.isEmpty(sort)) sort = "hot";
        if (TextUtils.isEmpty(timespan)) timespan = "all";

        SubredditPresenter presenter = new SubredditPresenter(this, redditNavigationView, this);
        subredditPresenter = presenter;
        setListingsPresenter(presenter);
        setCallbacks(presenter);
    }

    @NotNull @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        ButterKnife.bind(this, view);

        // Show submit button if we're on a valid subreddit
        boolean showSubmitButton = subreddit != null && !"all".equals(subreddit);
        submitNewPostButton.setVisibility(showSubmitButton ? View.VISIBLE : View.GONE);

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
        Subreddit subredditInfo = subredditPresenter.getSubredditInfo();
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
        Intent intent = SubmitPostActivity.getIntent(getContext(), subreddit);
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
            subreddit = ((Link) getListingsPresenter().getListingAt(0)).getSubreddit();
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
        return subreddit;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public String getTimespan() {
        return timespan;
    }

    @NotNull @Override
    protected View getChromeView() {
        return coordinatorLayout;
    }

    //region Options menu

    /**
     * Same implementation in {@link UserProfileFragment}
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        // Disable timespan option if current sort does not support it
        switch (sort) {
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
                return true;
            case R.id.action_change_timespan:
                showTimespanOptionsMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showSortOptionsMenu() {
        ChooseLinkSortDialog chooseLinkSortDialog =
                ChooseLinkSortDialog.newInstance(sort);
        chooseLinkSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
        chooseLinkSortDialog.show(getFragmentManager(), ChooseLinkSortDialog.TAG);
    }

    private void showTimespanOptionsMenu() {
        ChooseTimespanDialog chooseTimespanDialog =
                ChooseTimespanDialog.newInstance(timespan);
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
        subreddit = randomSubreddit;

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
            settingsManager.setOver18(true);
            getListingsPresenter().refreshData();
        } else {
            dismissSpinner();
            finish();
        }
    }

    private void onSortSelected(@NotNull String sort) {
        if (sort.equals(this.sort)) return;

        if (sort.equals("top") || sort.equals("controversial")) {
            selectedSort = sort;
            showTimespanOptionsMenu();
        } else {
            this.sort = sort;
            getActivity().invalidateOptionsMenu();
            getListingsPresenter().onSortChanged();
        }
    }

    private void onTimespanSelected(@NotNull String timespan) {
        sort = selectedSort;
        this.timespan = timespan;
        getActivity().invalidateOptionsMenu();
        getListingsPresenter().onSortChanged();
    }

    private void onPostSubmitted(String subreddit, String linkId) {
        Intent intent = LinkCommentsActivity.getIntent(getContext(), subreddit, linkId, null);
        startActivity(intent);
    }

    //endregion
}
