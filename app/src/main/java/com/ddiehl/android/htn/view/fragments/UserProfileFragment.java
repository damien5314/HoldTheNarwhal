package com.ddiehl.android.htn.view.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
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

public class UserProfileFragment extends AbsListingsFragment
    implements UserProfileView, TabLayout.OnTabSelectedListener {
  private static final String ARG_SHOW = "arg_show";
  private static final String ARG_USERNAME = "arg_username";
  private static final String ARG_SORT = "arg_sort";

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
      @NonNull String show, @NonNull String username, @NonNull String sort) {
    UserProfileFragment f = new UserProfileFragment();
    Bundle args = new Bundle();
    args.putString(ARG_SHOW, show);
    args.putString(ARG_USERNAME, username);
    args.putString(ARG_SORT, sort);
    f.setArguments(args);
    return f;
  }

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Bundle args = getArguments();
    String show = args.getString(ARG_SHOW);
    String username = args.getString(ARG_USERNAME);
    String sort = args.getString(ARG_SORT);
    mUserProfilePresenter =
        new UserProfilePresenter(mMainView, this, this, this, this, show, username, sort, "all");
    mLinkPresenter = mUserProfilePresenter;
    mCommentPresenter = mUserProfilePresenter;
    mListingsPresenter = mUserProfilePresenter;
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View v = super.onCreateView(inflater, container, savedInstanceState);
    initializeUserProfileTabs();
    mKarmaLayout.setVisibility(View.GONE);
    mFriendButton.setVisibility(View.GONE);
    mFriendNoteLayout.setVisibility(View.GONE);
    mFriendNoteSave.setOnClickListener((view) -> {
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
    updateTitle();
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment_user_profile;
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
    mUserProfileTabs.setOnTabSelectedListener(null);

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

    // Normal tabs
    mUserProfileTabs.addTab(mTabSummary);
    mUserProfileTabs.addTab(mTabOverview);
    mUserProfileTabs.addTab(mTabComments);
    mUserProfileTabs.addTab(mTabSubmitted);
    mUserProfileTabs.addTab(mTabGilded);

    // Authorized tabs
    UserIdentity id = mUserProfilePresenter.getAuthorizedUser();
    boolean showAuthorizedTabs = id != null &&
        id.getName().equals(mUserProfilePresenter.getUsernameContext());
    if (showAuthorizedTabs) {
      mUserProfileTabs.addTab(mTabUpvoted);
      mUserProfileTabs.addTab(mTabDownvoted);
      mUserProfileTabs.addTab(mTabHidden);
      mUserProfileTabs.addTab(mTabSaved);
    }

    mUserProfileTabs.setOnTabSelectedListener(this);
  }

  public void updateUserProfileTabs() {
    UserIdentity id = mUserProfilePresenter.getAuthorizedUser();
    boolean showAuthorizedTabs = id != null &&
        id.getName().equals(mUserProfilePresenter.getUsernameContext());
    if (showAuthorizedTabs) {
      if (AndroidUtils.getChildrenInTabLayout(mUserProfileTabs) != 9) {
        while (AndroidUtils.getChildrenInTabLayout(mUserProfileTabs) > 5) {
          mUserProfileTabs.removeTabAt(5);
        }
        mUserProfileTabs.addTab(mTabUpvoted);
        mUserProfileTabs.addTab(mTabDownvoted);
        mUserProfileTabs.addTab(mTabHidden);
        mUserProfileTabs.addTab(mTabSaved);
      }
    } else {
      while (AndroidUtils.getChildrenInTabLayout(mUserProfileTabs) > 5) {
        mUserProfileTabs.removeTabAt(5);
      }
    }
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
    String tag = (String) tab.getTag();
    if (tag != null && tag.equals("summary")) {
      mListView.setVisibility(View.GONE);
      mUserProfileSummary.setVisibility(View.VISIBLE);
    } else {
      mUserProfileSummary.setVisibility(View.GONE);
      mListView.setVisibility(View.VISIBLE);
    }
    mUserProfilePresenter.requestData(tag);
  }

  @Override
  public void updateTitle() {
    mMainView.setTitle(String.format(getString(R.string.username),
        mUserProfilePresenter.getUsernameContext()));
  }
}
