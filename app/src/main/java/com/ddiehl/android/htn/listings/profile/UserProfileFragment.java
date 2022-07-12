package com.ddiehl.android.htn.listings.profile;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.gallery.MediaGalleryRouter;
import com.ddiehl.android.htn.identity.IdentityManager;
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.ChooseTimespanDialog;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsRouter;
import com.ddiehl.android.htn.listings.links.ChooseLinkSortDialog;
import com.ddiehl.android.htn.listings.subreddit.SubredditFragment;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.utils.Utils;
import com.ddiehl.android.htn.view.video.VideoPlayerRouter;
import com.google.android.material.tabs.TabLayout;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rxreddit.model.Listing;
import rxreddit.model.Trophy;
import rxreddit.model.UserIdentity;

@FragmentWithArgs
public class UserProfileFragment extends BaseListingsFragment
        implements UserProfileView, TabLayout.OnTabSelectedListener {

    public static final String TAG = UserProfileFragment.class.getSimpleName();

    private static final int NUM_DEFAULT_TABS = 5;

    @Inject
    IdentityManager identityManager;
    @Inject
    LinkCommentsRouter linkCommentsRouter;
    @Inject
    MediaGalleryRouter mediaGalleryRouter;
    @Inject
    VideoPlayerRouter videoPlayerRouter;

    protected CoordinatorLayout coordinatorLayout;
    protected View userProfileSummary;
    protected View listView;
    protected View friendNoteLayout;

    // Views for user profile summary elements
    private TextView createDate;
    private View karmaLayout;
    private TextView linkKarma;
    private TextView commentKarma;
    private Button friendButton;
    private TextView friendNote;
    private Button friendNoteSave;
    private TrophyCaseLayout trophies;

    @Arg
    String username;
    @Arg
    String show;
    @Arg
    String sort;
    @Arg
    String timespan;

    private TabLayout.Tab tabUpvoted;
    private TabLayout.Tab tabDownvoted;
    private TabLayout.Tab tabHidden;
    private TabLayout.Tab tabSaved;

    private UserProfilePresenter userProfilePresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.listings_fragment_user_profile;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this);

        if (TextUtils.isEmpty(show)) show = "summary";
        if (TextUtils.isEmpty(sort)) sort = "new";
        if (TextUtils.isEmpty(timespan)) timespan = "all";

        userProfilePresenter = new UserProfilePresenter(
                this,
                redditNavigationView,
                linkCommentsRouter,
                mediaGalleryRouter,
                videoPlayerRouter,
                this
        );
        setListingsPresenter(userProfilePresenter);
        setCallbacks(userProfilePresenter);
    }

    @NotNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        tabLayout = getActivity().findViewById(R.id.tab_layout);

        coordinatorLayout = view.findViewById(R.id.coordinator_layout);
        userProfileSummary = view.findViewById(R.id.user_profile_summary);
        listView = view.findViewById(R.id.recycler_view);
        friendNoteLayout = view.findViewById(R.id.user_note_layout);

        // Views for user profile summary elements
        createDate = view.findViewById(R.id.user_created);
        karmaLayout = view.findViewById(R.id.user_karma_layout);
        linkKarma = view.findViewById(R.id.user_link_karma);
        commentKarma = view.findViewById(R.id.user_comment_karma);
        friendButton = view.findViewById(R.id.user_friend_button);
        friendNote = view.findViewById(R.id.user_friend_note_edit);
        friendNoteSave = view.findViewById(R.id.user_friend_note_confirm);
        trophies = view.findViewById(R.id.user_trophies);

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        showHideView(show);

        initializeUserProfileTabs();

        karmaLayout.setVisibility(View.GONE);
        friendButton.setVisibility(View.GONE);
        friendNoteLayout.setVisibility(View.GONE);

        friendNoteSave.setOnClickListener(saveView -> {
            String note = friendNote.getText().toString();
            userProfilePresenter.saveFriendNote(note);
        });

        setTitle(String.format(
                getString(R.string.username_formatter),
                getUsernameContext()));
    }

    private void initializeUserProfileTabs() {
        tabUpvoted = tabLayout.newTab()
                .setText(R.string.navigation_tabs_upvoted)
                .setTag("upvoted");
        tabDownvoted = tabLayout.newTab()
                .setText(R.string.navigation_tabs_downvoted)
                .setTag("downvoted");
        tabHidden = tabLayout.newTab()
                .setText(R.string.navigation_tabs_hidden)
                .setTag("hidden");
        tabSaved = tabLayout.newTab()
                .setText(R.string.navigation_tabs_saved)
                .setTag("saved");

        tabLayout.removeAllTabs();
        for (TabLayout.Tab tab : buildDefaultTabs(tabLayout)) {
            tabLayout.addTab(tab, tab.getTag().equals(show));
        }

        boolean isAuthenticated = userProfilePresenter.isAuthenticatedUser();
        showAuthenticatedTabs(tabLayout, isAuthenticated);

        selectTab(show);

        tabLayout.addOnTabSelectedListener(this);
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
                tabLayout.addTab(tabUpvoted);
                tabLayout.addTab(tabDownvoted);
                tabLayout.addTab(tabHidden);
                tabLayout.addTab(tabSaved);
            }
        } else {
            if (tabLayout.getTabCount() > NUM_DEFAULT_TABS)
                for (int i = tabLayout.getTabCount() - 1; i >= NUM_DEFAULT_TABS; i--) {
                    tabLayout.removeTabAt(i);
                }
        }
    }

    private ListingsAdapter listingsAdapter;

    @NotNull
    @Override
    public ListingsAdapter getListingsAdapter() {
        if (listingsAdapter == null) {
            listingsAdapter = new ListingsAdapter(getListingsPresenter(), this, this, null);
        }
        return listingsAdapter;
    }

    //region Options menu

    /**
     * Same implementation in {@link SubredditFragment}
     */

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.listings_user_profile, menu);

        if (show.equals("summary")) {
            hideSortAndTimespanOptions(menu);
        } else {
            hideTimespanOptionIfUnsupported(menu, sort);
        }
    }

    private void hideSortAndTimespanOptions(Menu menu) {
        menu.findItem(R.id.action_change_sort)
                .setVisible(false);
        menu.findItem(R.id.action_change_timespan)
                .setVisible(false);
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
    private String selectedSort;

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

    //endregion

    @Override
    public void showUserInfo(@NotNull UserIdentity user) {
        Date createDate = new Date(user.getCreatedUTC() * 1000);
        String created = String.format(
                getContext().getString(R.string.user_profile_summary_created),
                SimpleDateFormat.getDateInstance().format(createDate));
        this.createDate.setText(created);
        karmaLayout.setVisibility(View.VISIBLE);
        linkKarma.setText(NumberFormat.getInstance().format(user.getLinkKarma()));
        commentKarma.setText(NumberFormat.getInstance().format(user.getCommentKarma()));
        // If user is not self, show friend button
        // TODO This should come from presenter
        UserIdentity self = identityManager.getUserIdentity();
        if (self != null && !user.getName().equals(self.getName())) {
            friendButton.setVisibility(View.VISIBLE);
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
            friendButton.setText(R.string.user_friend_delete_button_text);
            friendButton.setOnClickListener((v) -> {
                userProfilePresenter.deleteFriend();
            });
        } else {
            friendButton.setText(R.string.user_friend_add_button_text);
            friendButton.setOnClickListener((v) -> {
                userProfilePresenter.addFriend();
            });
        }
    }

    @Override
    public void showFriendNote(@NotNull String note) {
        friendNoteLayout.setVisibility(View.VISIBLE);
        friendNote.setText(note);
    }

    @Override
    public void hideFriendNote() {
        friendNoteLayout.setVisibility(View.GONE);
        friendNote.setText(null);
    }

    @Override
    public void showTrophies(List<Listing> trophies) {
        this.trophies.removeAllViews();

        if (trophies == null || trophies.size() == 0) {
            return; // Nothing to show
        }

        // Convert to list of Trophies
        List<Trophy> trophyList = Utils.convert(trophies);

        // Bind to TrophyCase
        this.trophies.bind(trophyList);
    }

    private TabLayout.Tab getCurrentSelectedTab() {
        return tabLayout.getTabAt(
                tabLayout.getSelectedTabPosition());
    }

    @Override
    public void selectTab(String show) {
        this.show = show;

        tabLayout.removeOnTabSelectedListener(this);

        for (int i = 0; i < AndroidUtils.getChildrenInTabLayout(tabLayout); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                String tag = (String) tab.getTag();
                if (tag != null && tag.equals(show)) {
                    tab.select();
                    break;
                }
            }
        }

        tabLayout.addOnTabSelectedListener(this);

        showHideView(this.show);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        show = (String) tab.getTag();
        showHideView(show);
        getActivity().invalidateOptionsMenu();
        userProfilePresenter.requestData();
    }

    private void showHideView(String show) {
        if ("summary".equals(show)) {
            listView.setVisibility(View.GONE);
            userProfileSummary.setVisibility(View.VISIBLE);
        } else {
            userProfileSummary.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public String getShow() {
        return show;
    }

    @Override
    public String getUsernameContext() {
        return username;
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
}
