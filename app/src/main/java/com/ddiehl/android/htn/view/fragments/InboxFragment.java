package com.ddiehl.android.htn.view.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.InboxPresenter;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.InboxView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import butterknife.Bind;

@FragmentWithArgs
public class InboxFragment extends BaseListingsFragment
    implements InboxView, TabLayout.OnTabSelectedListener {

  public static final String TAG = InboxFragment.class.getSimpleName();

  private static final String ARG_SHOW = "arg_show";

  @Bind(R.id.tab_layout) TabLayout mTabs;
  private TabLayout.Tab mTabAll, mTabUnread, mTabMessages,
      mTabCommentReplies, mTabPostReplies, mTabMentions;

  private InboxPresenter mInboxPresenter;
  @Arg String mShow;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    FragmentArgs.inject(this);
    if (TextUtils.isEmpty(mShow)) mShow = "inbox";
    mInboxPresenter = new InboxPresenter(mMainView, this, this, this, this, mShow);
    mLinkPresenter = mInboxPresenter;
    mCommentPresenter = mInboxPresenter;
    mMessagePresenter = mInboxPresenter;
    mListingsPresenter = mInboxPresenter;
    mCallbacks = mInboxPresenter;
  }

  @Override
  public void onPause() {
    getArguments().putString(ARG_SHOW, mListingsPresenter.getShow());
    super.onPause();
  }

  @Nullable @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View view = super.onCreateView(inflater, container, savedInstanceState);
    initializeTabs();
    mMainView.setTitle(R.string.inbox_fragment_title);
    return view;
  }

  @Override
  public ListingsAdapter getListingsAdapter() {
    return new ListingsAdapter(
        mListingsPresenter, mLinkPresenter, mCommentPresenter, mMessagePresenter);
  }

  @Override
  protected int getLayoutResId() {
    return R.layout.listings_fragment_inbox;
  }

  private void initializeTabs() {
    mTabs.setOnTabSelectedListener(null);
    mTabAll = mTabs.newTab()
        .setText(R.string.navigation_tabs_all).setTag("inbox");
    mTabUnread = mTabs.newTab()
        .setText(R.string.navigation_tabs_unread).setTag("unread");
    mTabMessages = mTabs.newTab()
        .setText(R.string.navigation_tabs_messages).setTag("messages");
    mTabCommentReplies = mTabs.newTab()
        .setText(R.string.navigation_tabs_comment_replies).setTag("comments");
    mTabPostReplies = mTabs.newTab()
        .setText(R.string.navigation_tabs_post_replies).setTag("selfreply");
    mTabMentions = mTabs.newTab()
        .setText(R.string.navigation_tabs_mentions).setTag("mentions");
    mTabs.addTab(mTabAll);
    mTabs.addTab(mTabUnread);
    mTabs.addTab(mTabMessages);
    mTabs.addTab(mTabCommentReplies);
    mTabs.addTab(mTabPostReplies);
    mTabs.addTab(mTabMentions);
    mTabs.setOnTabSelectedListener(this);
  }

  @Override
  public void selectTab(@NonNull String show) {
    mTabs.setOnTabSelectedListener(null);
    for (int i = 0; i < AndroidUtils.getChildrenInTabLayout(mTabs); i++) {
      TabLayout.Tab tab = mTabs.getTabAt(i);
      if (tab != null) {
        String tag = (String) tab.getTag();
        if (tag != null && tag.equals(show)) {
          tab.select();
          break;
        }
      }
    }
    mTabs.setOnTabSelectedListener(this);
  }

  @Override
  public void onTabSelected(TabLayout.Tab tab) {
    String show = (String) tab.getTag();
    mInboxPresenter.requestData(show);
  }

  @Override public void onTabUnselected(TabLayout.Tab tab) { }
  @Override public void onTabReselected(TabLayout.Tab tab) { }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.listings_inbox, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_mark_messages_read:
        mInboxPresenter.onMarkMessagesRead();
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void showSubject(@NonNull String subject) {
    /* no-op for this view */
  }
}
