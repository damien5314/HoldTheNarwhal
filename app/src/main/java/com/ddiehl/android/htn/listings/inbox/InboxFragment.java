package com.ddiehl.android.htn.listings.inbox;

import android.os.Bundle;
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
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.ListingsAdapter;
import com.ddiehl.android.htn.utils.AndroidUtils;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import butterknife.BindView;

@FragmentWithArgs
public class InboxFragment extends BaseListingsFragment
        implements InboxView, TabLayout.OnTabSelectedListener {

    public static final String TAG = InboxFragment.class.getSimpleName();

    @Arg(key = "ARG_SHOW") String mShow;

    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

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

        mInboxPresenter = new InboxPresenter(this, redditNavigationView, this);
        setListingsPresenter(mInboxPresenter);
        setCallbacks(mInboxPresenter);
    }

    @NotNull @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
        View view = super.onCreateView(inflater, container, state);

        initializeTabs();

        setTitle(R.string.inbox_fragment_title);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (identityManager.getUserIdentity() == null) {
            // User was signed out, they can't view inbox anymore
            finish();
        } else {
            // User is authenticated, defer to base implementation
            super.onResume();
        }
    }

    private void initializeTabs() {
        tabLayout.removeOnTabSelectedListener(this);

        for (TabLayout.Tab tab : buildTabs()) {
            tabLayout.addTab(tab);
        }

        selectTab(mShow);

        tabLayout.addOnTabSelectedListener(this);
    }

    private List<TabLayout.Tab> buildTabs() {
        return Arrays.asList(
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_all)
                        .setTag("inbox"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_unread)
                        .setTag("unread"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_messages)
                        .setTag("messages"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_comment_replies)
                        .setTag("comments"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_post_replies)
                        .setTag("selfreply"),
                tabLayout.newTab()
                        .setText(R.string.navigation_tabs_mentions)
                        .setTag("mentions")

        );
    }

    private ListingsAdapter listingsAdapter;

    @NotNull @Override
    public ListingsAdapter getListingsAdapter() {
        if (listingsAdapter == null) {
            listingsAdapter = new ListingsAdapter(getListingsPresenter(), this, this, this);
        }
        return listingsAdapter;
    }

    @Override
    public void selectTab(@NotNull String show) {
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
    public String getShow() {
        return mShow;
    }

    @NotNull @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }
}
