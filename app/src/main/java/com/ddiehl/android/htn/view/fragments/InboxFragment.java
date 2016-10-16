package com.ddiehl.android.htn.view.fragments;

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

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.presenter.InboxPresenter;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.ddiehl.android.htn.view.InboxView;
import com.ddiehl.android.htn.view.adapters.ListingsAdapter;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

@FragmentWithArgs
public class InboxFragment extends BaseListingsFragment
        implements InboxView, TabLayout.OnTabSelectedListener {

    public static final String TAG = InboxFragment.class.getSimpleName();

    @Arg(key = "ARG_SHOW") String mShow;

    @BindView(R.id.tab_layout) protected TabLayout mTabs;
    @BindView(R.id.coordinator_layout) protected CoordinatorLayout mCoordinatorLayout;

    private InboxPresenter mInboxPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.listings_fragment_inbox;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this);

        if (TextUtils.isEmpty(mShow)) mShow = "inbox";

        mInboxPresenter = new InboxPresenter(this, mRedditNavigationView, this);
        mLinkPresenter = mInboxPresenter;
        mCommentPresenter = mInboxPresenter;
        mMessagePresenter = mInboxPresenter;
        mListingsPresenter = mInboxPresenter;
        mCallbacks = mInboxPresenter;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = super.onCreateView(inflater, container, state);

        initializeTabs();

        setTitle(R.string.inbox_fragment_title);

        return view;
    }

    private void initializeTabs() {
        mTabs.removeOnTabSelectedListener(this);

        for (TabLayout.Tab tab : buildTabs()) {
            mTabs.addTab(tab);
        }

        selectTab(mShow);

        mTabs.addOnTabSelectedListener(this);
    }

    private List<TabLayout.Tab> buildTabs() {
        return Arrays.asList(
                mTabs.newTab()
                        .setText(R.string.navigation_tabs_all)
                        .setTag("inbox"),
                mTabs.newTab()
                        .setText(R.string.navigation_tabs_unread)
                        .setTag("unread"),
                mTabs.newTab()
                        .setText(R.string.navigation_tabs_messages)
                        .setTag("messages"),
                mTabs.newTab()
                        .setText(R.string.navigation_tabs_comment_replies)
                        .setTag("comments"),
                mTabs.newTab()
                        .setText(R.string.navigation_tabs_post_replies)
                        .setTag("selfreply"),
                mTabs.newTab()
                        .setText(R.string.navigation_tabs_mentions)
                        .setTag("mentions")

        );
    }

    @Override
    public ListingsAdapter getListingsAdapter() {
        return new ListingsAdapter(
                mListingsPresenter, this, mLinkPresenter, this, mCommentPresenter, this, mMessagePresenter);
    }

    @Override
    public void selectTab(@NonNull String show) {
        mTabs.removeOnTabSelectedListener(this);
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
        mTabs.addOnTabSelectedListener(this);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        mShow = (String) tab.getTag();
        mInboxPresenter.onViewSelected(mShow);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

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

    @Override
    public String getShow() {
        return mShow;
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }
}
