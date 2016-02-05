package com.ddiehl.android.htn.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.UserProfilePresenter;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.MainView;
import com.ddiehl.android.htn.view.UserProfileView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.ddiehl.reddit.identity.UserIdentity;
import com.ddiehl.reddit.listings.Listing;
import com.ddiehl.reddit.listings.Trophy;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class UserProfileFragment extends BaseListingsFragment
    implements UserProfileView, TabLayout.OnTabSelectedListener {
  private static final String ARG_SHOW = "arg_show";
  private static final String ARG_USERNAME = "arg_username";
  private static final String ARG_SORT = "arg_sort";
  private static final String ARG_TIMESPAN = "arg_timespan";

  @Bind(R.id.tab_layout) TabLayout mUserProfileTabs;
  @Bind(R.id.user_profile_summary) View mUserProfileSummary;
  @Bind(R.id.recycler_view) View mListView;
  @Bind(R.id.user_note_layout) View mFriendNoteLayout;

  // Views for user profile summary elements
  @Bind(R.id.user_created) TextView mCreateDate;
  @Bind(R.id.user_karma_layout) View mKarmaLayout;
  @Bind(R.id.user_link_karma) TextView mLinkKarma;
  @Bind(R.id.user_comment_karma) TextView mCommentKarma;
  @Bind(R.id.user_friend_button) Button mFriendButton;
  @Bind(R.id.user_friend_note_edit) TextView mFriendNote;
  @Bind(R.id.user_friend_note_confirm) Button mFriendNoteSave;
  @Bind(R.id.user_trophies) GridLayout mTrophies;

  private TabLayout.Tab mTabSummary, mTabOverview, mTabComments, mTabSubmitted, mTabGilded,
      mTabUpvoted, mTabDownvoted, mTabHidden, mTabSaved;

  private UserProfilePresenter mUserProfilePresenter;

  public UserProfileFragment() { }

  public static UserProfileFragment newInstance(
      @NonNull String username, @Nullable String show, @Nullable String sort) {
    UserProfileFragment f = new UserProfileFragment();
    Bundle args = new Bundle();
    args.putString(ARG_USERNAME, username);
    if (TextUtils.isEmpty(show)) show = "summary";
    args.putString(ARG_SHOW, show);
    if (TextUtils.isEmpty(sort)) sort = "new";
    args.putString(ARG_SORT, sort);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    String username = args.getString(ARG_USERNAME);
    String show = args.getString(ARG_SHOW);
    String sort = args.getString(ARG_SORT);
    String timespan = args.getString(ARG_TIMESPAN);
    if (TextUtils.isEmpty(timespan)) timespan = "all";
    mUserProfilePresenter =
        new UserProfilePresenter(mMainView, this, this, this, this, show, username, sort, timespan);
    mLinkPresenter = mUserProfilePresenter;
    mCommentPresenter = mUserProfilePresenter;
    mListingsPresenter = mUserProfilePresenter;
  }

  @Override
  public void refreshTabs(boolean showAuthenticatedTabs) {
    if (showAuthenticatedTabs) {
      if (mUserProfileTabs.getTabCount() == 4) {
        mUserProfileTabs.addTab(mTabUpvoted);
        mUserProfileTabs.addTab(mTabDownvoted);
        mUserProfileTabs.addTab(mTabHidden);
        mUserProfileTabs.addTab(mTabSaved);
      }
    } else {
      if (mUserProfileTabs.getTabCount() > 4)
        for (int i = mUserProfileTabs.getTabCount() - 1; i >= 4; i--) {
          mUserProfileTabs.removeTabAt(i);
        }
    }
  }

  @Nullable @Override
  public View onCreateView(
      LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = super.onCreateView(inflater, container, savedInstanceState);
    initializeUserProfileTabs();
    mKarmaLayout.setVisibility(View.GONE);
    mFriendButton.setVisibility(View.GONE);
    mFriendNoteLayout.setVisibility(View.GONE);
    mFriendNoteSave.setOnClickListener(view -> {
      String note = mFriendNote.getText().toString();
      mUserProfilePresenter.saveFriendNote(note);
    });
    return v;
  }

  @Override
  public ListingsAdapter getListingsAdapter() {
    return new ListingsAdapter(
        mListingsPresenter, mLinkPresenter, mCommentPresenter, mMessagePresenter);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    updateTitle(); // FIXME Why does this have to go in onActivityCreated?
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment_user_profile;
  }

  @Override
  public void onPause() {
    getArguments().putString(ARG_SHOW, mListingsPresenter.getShow());
    getArguments().putString(ARG_SORT, mListingsPresenter.getSort());
    getArguments().putString(ARG_TIMESPAN, mListingsPresenter.getTimespan());
    super.onPause();
  }

  @Override
  public void showUserInfo(@NonNull UserIdentity user) {
    Date createDate = new Date(user.getCreatedUTC() * 1000);
    Context context = HoldTheNarwhal.getContext();
    String created = String.format(
        context.getString(R.string.user_profile_summary_created),
        SimpleDateFormat.getDateInstance().format(createDate));
    mCreateDate.setText(created);
    mKarmaLayout.setVisibility(View.VISIBLE);
    mLinkKarma.setText(NumberFormat.getInstance().format(user.getLinkKarma()));
    mCommentKarma.setText(NumberFormat.getInstance().format(user.getCommentKarma()));
    // If user is not self, show friend button
    // TODO This should come from presenter
    UserIdentity self = HoldTheNarwhal.getIdentityManager().getUserIdentity();
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
        ((MainView) getActivity()).showSpinner(null);
        mUserProfilePresenter.deleteFriend();
      });
    } else {
      mFriendButton.setText(R.string.user_friend_add_button_text);
      mFriendButton.setOnClickListener((v) -> {
        ((MainView) getActivity()).showSpinner(null);
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
    if (trophies == null || trophies.size() == 0) return; // Nothing to show
    LayoutInflater inflater = getActivity().getLayoutInflater();
    for (Listing listing : trophies) {
      LinearLayout view = (LinearLayout) inflater.inflate(R.layout.trophy_layout, mTrophies, false);
      TextView trophyNameView = ButterKnife.findById(view, R.id.trophy_name);
      Trophy trophy = (Trophy) listing;
      String name = trophy.getName();
      trophyNameView.setText(name);
      Picasso.with(getActivity())
          .load(trophy.getIcon70())
          .into(ButterKnife.<ImageView>findById(view, R.id.trophy_icon));
      mTrophies.addView(view);
    }
  }

  private void initializeUserProfileTabs() {
    mTabSummary = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_summary).setTag("summary");
    mTabOverview = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_overview).setTag("overview");
    mTabComments = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_comments).setTag("comments");
    mTabSubmitted = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_submitted).setTag("submitted");
    mTabGilded = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_gilded).setTag("gilded");
    mTabUpvoted = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_upvoted).setTag("upvoted");
    mTabDownvoted = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_downvoted).setTag("downvoted");
    mTabHidden = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_hidden).setTag("hidden");
    mTabSaved = mUserProfileTabs.newTab()
        .setText(R.string.navigation_tabs_saved).setTag("saved");
    mUserProfileTabs.removeAllTabs();
    mUserProfileTabs.addTab(mTabSummary);
    mUserProfileTabs.addTab(mTabOverview);
    mUserProfileTabs.addTab(mTabComments);
    mUserProfileTabs.addTab(mTabSubmitted);
  }

  private TabLayout.Tab getCurrentSelectedTab() {
    return mUserProfileTabs.getTabAt(
        mUserProfileTabs.getSelectedTabPosition());
  }

  @Override
  public void selectTab(String show) {
    mUserProfileTabs.setOnTabSelectedListener(null);
    for (int i = 0; i < AndroidUtils.getChildrenInTabLayout(mUserProfileTabs); i++) {
      TabLayout.Tab tab = mUserProfileTabs.getTabAt(i);
      if (tab != null) {
        String tag = (String) tab.getTag();
        if (tag != null && tag.equals(show)) {
          tab.select();
          showViewForTab(tab);
          break;
        }
      }
    }
    mUserProfileTabs.setOnTabSelectedListener(this);
  }
  
  @Override public void onTabUnselected(TabLayout.Tab tab) {}
  @Override public void onTabReselected(TabLayout.Tab tab) {}

  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    showViewForTab(tab);
    mUserProfilePresenter.requestData((String) tab.getTag());
  }

  private void showViewForTab(TabLayout.Tab tab) {
    String tag = (String) tab.getTag();
    if (tag != null && tag.equals("summary")) {
      mListView.setVisibility(View.GONE);
      mUserProfileSummary.setVisibility(View.VISIBLE);
    } else {
      mUserProfileSummary.setVisibility(View.GONE);
      mListView.setVisibility(View.VISIBLE);
    }
  }

  @Override
  public void updateTitle() {
    mMainView.setTitle(String.format(getString(R.string.username),
        mUserProfilePresenter.getUsernameContext()));
  }
}
