package com.ddiehl.android.htn.listings.profile;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.ChooseTimespanDialog;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.listings.links.ChooseLinkSortDialog;
import com.ddiehl.android.htn.listings.subreddit.SubredditFragment;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import rxreddit.model.Listing;
import rxreddit.model.Trophy;
import rxreddit.model.UserIdentity;

import static android.app.Activity.RESULT_OK;
import static butterknife.ButterKnife.findById;

@FragmentWithArgs
public class UserProfileFragment extends BaseListingsFragment
        implements UserProfileView, TabLayout.OnTabSelectedListener {

    public static final String TAG = UserProfileFragment.class.getSimpleName();

    private static final int NUM_DEFAULT_TABS = 5;

    @Inject protected IdentityManager mIdentityManager;

    @BindView(R.id.coordinator_layout) protected CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.user_profile_summary) protected View mUserProfileSummary;
    @BindView(R.id.recycler_view) protected View mListView;
    @BindView(R.id.user_note_layout) protected View mFriendNoteLayout;

    // Views for user profile summary elements
    @BindView(R.id.user_created) TextView mCreateDate;
    @BindView(R.id.user_karma_layout) View mKarmaLayout;
    @BindView(R.id.user_link_karma) TextView mLinkKarma;
    @BindView(R.id.user_comment_karma) TextView mCommentKarma;
    @BindView(R.id.user_friend_button) Button mFriendButton;
    @BindView(R.id.user_friend_note_edit) TextView mFriendNote;
    @BindView(R.id.user_friend_note_confirm) Button mFriendNoteSave;
    @BindView(R.id.user_trophies) GridLayout mTrophies;

    @Arg String mUsername;
    @Arg String mShow;
    @Arg String mSort;
    @Arg String mTimespan;

    private TabLayout.Tab mTabUpvoted;
    private TabLayout.Tab mTabDownvoted;
    private TabLayout.Tab mTabHidden;
    private TabLayout.Tab mTabSaved;

    private UserProfilePresenter mUserProfilePresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.listings_fragment_user_profile;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);

        if (TextUtils.isEmpty(mShow)) mShow = "summary";
        if (TextUtils.isEmpty(mSort)) mSort = "new";
        if (TextUtils.isEmpty(mTimespan)) mTimespan = "all";

        mUserProfilePresenter = new UserProfilePresenter(this, mRedditNavigationView, this);
        mListingsPresenter = mUserProfilePresenter;
        mCallbacks = mUserProfilePresenter;
    }

    @NonNull @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        mTabLayout = findById(getActivity(), R.id.tab_layout);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showHideView(mShow);

        initializeUserProfileTabs();

        mKarmaLayout.setVisibility(View.GONE);
        mFriendButton.setVisibility(View.GONE);
        mFriendNoteLayout.setVisibility(View.GONE);

        mFriendNoteSave.setOnClickListener(saveView -> {
            String note = mFriendNote.getText().toString();
            mUserProfilePresenter.saveFriendNote(note);
        });

        setTitle(String.format(
                getString(R.string.username_formatter),
                getUsernameContext()));
    }

    private void initializeUserProfileTabs() {
        mTabUpvoted = mTabLayout.newTab()
                .setText(R.string.navigation_tabs_upvoted)
                .setTag("upvoted");
        mTabDownvoted = mTabLayout.newTab()
                .setText(R.string.navigation_tabs_downvoted)
                .setTag("downvoted");
        mTabHidden = mTabLayout.newTab()
                .setText(R.string.navigation_tabs_hidden)
                .setTag("hidden");
        mTabSaved = mTabLayout.newTab()
                .setText(R.string.navigation_tabs_saved)
                .setTag("saved");

        mTabLayout.removeAllTabs();
        for (TabLayout.Tab tab : buildDefaultTabs(mTabLayout)) {
            mTabLayout.addTab(tab, tab.getTag().equals(mShow));
        }

        boolean isAuthenticated = mUserProfilePresenter.isAuthenticatedUser();
        showAuthenticatedTabs(mTabLayout, isAuthenticated);

        selectTab(mShow);

        mTabLayout.addOnTabSelectedListener(this);
    }

    private List<TabLayout.Tab> buildDefaultTabs(TabLayout tabLayout) {
        return Arrays.asList(
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_summary)
                        .setTag("summary"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_overview)
                        .setTag("overview"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_comments)
                        .setTag("comments"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_submitted)
                        .setTag("submitted"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_gilded)
                        .setTag("gilded")
        );
    }

    private boolean isInAuthenticatedView(String show) {
        return show.equals("upvoted")
                || show.equals("downvoted")
                || show.equals("hidden")
                || show.equals("saved");
    }

    private void showAuthenticatedTabs(TabLayout tabLayout, boolean authenticated) {
        if (authenticated) {
            if (tabLayout.getTabCount() == NUM_DEFAULT_TABS) {
                tabLayout.addTab(mTabUpvoted);
                tabLayout.addTab(mTabDownvoted);
                tabLayout.addTab(mTabHidden);
                tabLayout.addTab(mTabSaved);
            }
        } else {
            if (tabLayout.getTabCount() > NUM_DEFAULT_TABS)
                for (int i = tabLayout.getTabCount() - 1; i >= NUM_DEFAULT_TABS; i--) {
                    tabLayout.removeTabAt(i);
                }
        }
    }

    @Override
    public ListingsAdapter getListingsAdapter() {
        return new ListingsAdapter(
                mListingsPresenter, this, this, null);
    }

    //region Options menu

    /**
     * Same implementation in {@link SubredditFragment}
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        if (mShow.equals("summary")) {
            menu.findItem(R.id.action_change_sort)
                    .setVisible(false);
            menu.findItem(R.id.action_change_timespan)
                    .setVisible(false);
        } else {
            menu.findItem(R.id.action_change_sort)
                    .setVisible(true);
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
        }
    }

    // Cache for sort selected before showing timespan dialog
    private String mSelectedSort;

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

    @Override
    public void showUserInfo(@NonNull UserIdentity user) {
        Date createDate = new Date(user.getCreatedUTC() * 1000);
        String created = String.format(
                getContext().getString(R.string.user_profile_summary_created),
                SimpleDateFormat.getDateInstance().format(createDate));
        mCreateDate.setText(created);
        mKarmaLayout.setVisibility(View.VISIBLE);
        mLinkKarma.setText(NumberFormat.getInstance().format(user.getLinkKarma()));
        mCommentKarma.setText(NumberFormat.getInstance().format(user.getCommentKarma()));
        // If user is not self, show friend button
        // TODO This should come from presenter
        UserIdentity self = mIdentityManager.getUserIdentity();
        if (self != null && !user.getName().equals(self.getName())) {
            mFriendButton.setVisibility(View.VISIBLE);
            if (user.isFriend()) {
                setFriendButtonState(true);
            } else {
                setFriendButtonState(false);
            }
        }
    }

    @Override
    public void setFriendButtonState(boolean isFriend) {
        if (isFriend) {
            mFriendButton.setText(R.string.user_friend_delete_button_text);
            mFriendButton.setOnClickListener((v) -> {
                mUserProfilePresenter.deleteFriend();
            });
        } else {
            mFriendButton.setText(R.string.user_friend_add_button_text);
            mFriendButton.setOnClickListener((v) -> {
                mUserProfilePresenter.addFriend();
            });
        }
    }

    @Override
    public void showFriendNote(@NonNull String note) {
        mFriendNoteLayout.setVisibility(View.VISIBLE);
        mFriendNote.setText(note);
    }

    @Override
    public void hideFriendNote() {
        mFriendNoteLayout.setVisibility(View.GONE);
        mFriendNote.setText(null);
    }

    @Override
    public void showTrophies(List<Listing> trophies) {
        mTrophies.removeAllViews();

        if (trophies == null || trophies.size() == 0) {
            return; // Nothing to show
        }

        LayoutInflater inflater = getActivity().getLayoutInflater();
        for (Listing listing : trophies) {
            Trophy trophy = (Trophy) listing;
            String name = trophy.getName();

            // Inflate trophy layout
            ImageView imageView = (ImageView) inflater.inflate(R.layout.trophy_layout, mTrophies, false);

            // Set accessible description
            imageView.setContentDescription(name);

            // Show toast on click
            imageView.setOnClickListener(
                    view -> Toast.makeText(view.getContext(), name, Toast.LENGTH_SHORT).show()
            );

            // Load image
            Glide.with(getActivity())
                    .load(trophy.getIcon70())
                    .into(imageView);

            // Add view to GridLayout
            mTrophies.addView(imageView);
        }

        // Calculate and set number of columns
        final int trophiesWidth = mTrophies.getWidth();
        final int trophyMargin = ((GridLayout.LayoutParams) mTrophies.getChildAt(0).getLayoutParams())
                .rightMargin;
        final int imageWidth = 70; // We always retrieve the 70px version
        final int columns = trophiesWidth / (imageWidth + trophyMargin);
        mTrophies.setColumnCount(columns);
    }

    private TabLayout.Tab getCurrentSelectedTab() {
        return mTabLayout.getTabAt(
                mTabLayout.getSelectedTabPosition());
    }

    @Override
    public void selectTab(String show) {
        mShow = show;

        mTabLayout.removeOnTabSelectedListener(this);

        for (int i = 0; i < AndroidUtils.getChildrenInTabLayout(mTabLayout); i++) {
            TabLayout.Tab tab = mTabLayout.getTabAt(i);
            if (tab != null) {
                String tag = (String) tab.getTag();
                if (tag != null && tag.equals(show)) {
                    tab.select();
                    break;
                }
            }
        }

        mTabLayout.addOnTabSelectedListener(this);

        showHideView(mShow);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mShow = (String) tab.getTag();
        showHideView(mShow);
        getActivity().invalidateOptionsMenu();
        mUserProfilePresenter.requestData();
    }

    private void showHideView(String show) {
        if ("summary".equals(show)) {
            mListView.setVisibility(View.GONE);
            mUserProfileSummary.setVisibility(View.VISIBLE);
        } else {
            mUserProfileSummary.setVisibility(View.GONE);
            mListView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String getShow() {
        return mShow;
    }

    @Override
    public String getUsernameContext() {
        return mUsername;
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
}
