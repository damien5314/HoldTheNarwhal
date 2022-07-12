package com.ddiehl.android.htn.listings.subreddit;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.ChooseTimespanDialog;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsActivity;
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.listings.links.ChooseLinkSortDialog;
import com.ddiehl.android.htn.listings.profile.UserProfileFragment;
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostActivity;
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostFragment;
import com.ddiehl.android.htn.settings.SettingsManager;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;

import javax.inject.Inject;

import rxreddit.model.Link;
import rxreddit.model.Subreddit;

@FragmentWithArgs
public class SubredditFragment extends BaseListingsFragment implements SubredditView {

    public static final String TAG = SubredditFragment.class.getSimpleName();

    @Inject
    SettingsManager settingsManager;
    @Inject
    LinkCommentsRouter linkCommentsRouter;
    @Inject
    MediaGalleryRouter mediaGalleryRouter;
    @Inject
    VideoPlayerRouter videoPlayerRouter;

    @Arg(required = false)
    String subredditName;
    @Arg(required = false)
    String sort;
    @Arg(required = false)
    String timespan;

    private CoordinatorLayout coordinatorLayout;
    private FloatingActionButton submitNewPostButton;

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
        FragmentArgs.inject(this);

        if (TextUtils.isEmpty(sort)) sort = "hot";
        if (TextUtils.isEmpty(timespan)) timespan = "all";

        SubredditPresenter presenter = new SubredditPresenter(
                this,
                redditNavigationView,
                linkCommentsRouter,
                mediaGalleryRouter,
                videoPlayerRouter,
                this
        );
        subredditPresenter = presenter;
        setListingsPresenter(presenter);
        setCallbacks(presenter);
    }

    @NotNull
    @Override
    public View onCreateView(@NotNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = super.onCreateView(inflater, container, state);

        coordinatorLayout = view.findViewById(R.id.coordinator_layout);
        submitNewPostButton = view.findViewById(R.id.submit_new_post);
        submitNewPostButton.setOnClickListener(this::onSubmitNewPostClicked);

        // Show submit button if we're on a valid subreddit
        boolean showSubmitButton = subredditName != null && !"all".equals(subredditName);
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
        Subreddit subredditInfo = subredditPresenter.getSubreddit();
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

    @NotNull
    @Override
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

    private void onSubmitNewPostClicked(View view) {
        Intent intent = SubmitPostActivity.getIntent(getContext(), subredditName);
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
            subredditName = ((Link) getListingsPresenter().getListingAt(0)).getSubreddit();
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
        return subredditName;
    }

    @Override
    public String getSort() {
        return sort;
    }

    @Override
    public String getTimespan() {
        return timespan;
    }

    @NotNull
    @Override
    protected View getChromeView() {
        return coordinatorLayout;
    }

    //region Options menu

    /**
     * Same implementation in {@link UserProfileFragment}
     */

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.listings_subreddit, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        hideTimespanOptionIfUnsupported(menu, sort);
        showSubscribeOptions(menu);
    }

    @Override
    public void refreshOptionsMenu() {
        final Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
        }
    }

    private void showSubscribeOptions(@NonNull Menu menu) {
        final MenuItem subscribeMenuItem = menu.findItem(R.id.action_subreddit_subscribe);
        final MenuItem unsubscribeMenuItem = menu.findItem(R.id.action_subreddit_unsubscribe);

        if (subredditName == null) {
            subscribeMenuItem.setVisible(false);
            unsubscribeMenuItem.setVisible(false);
        } else {
            final Subreddit subreddit = subredditPresenter.getSubreddit();
            if (subreddit != null) {
                final Boolean userIsSubscriber =
                        subreddit.getUserIsSubscriber();
                subscribeMenuItem.setVisible(userIsSubscriber != null && !userIsSubscriber);
                unsubscribeMenuItem.setVisible(userIsSubscriber != null && userIsSubscriber);
            } else {
                subscribeMenuItem.setVisible(false);
                unsubscribeMenuItem.setVisible(false);
            }
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
            case R.id.action_subreddit_subscribe:
                subredditPresenter.subscribeToSubreddit(subredditName, true);
                return true;
            case R.id.action_subreddit_unsubscribe:
                subredditPresenter.subscribeToSubreddit(subredditName, false);
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
    public void onRandomSubredditLoaded(String randomSubreddit) {
        subredditName = randomSubreddit;

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
