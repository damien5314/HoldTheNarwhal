package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.requests.HideEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.requests.SaveEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.reddit.Sort;
import com.ddiehl.reddit.TimeSpan;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LinksPresenterImpl implements LinksPresenter {
    private static final String TAG = LinksPresenterImpl.class.getSimpleName();

    private Context mContext;
    private Bus mBus;
    private List<RedditLink> mLinks;
    private Set<String> mHiddenLinks;
    private LinksView mLinksView;

    private String mSubreddit;
    private String mSort;
    private String mTimeSpan;
    private boolean mLinksRequested = false;

    private RedditLink mLinkSelected;

    public LinksPresenterImpl(Context context, LinksView view, String subreddit) {
        mContext = context.getApplicationContext();
        mLinksView = view;
        mLinks = new ArrayList<>();
        mHiddenLinks = new HashSet<>();
        mBus = BusProvider.getInstance();

        mSubreddit = subreddit;
        mSort = Sort.HOT;
        mTimeSpan = TimeSpan.ALL;
    }

    @Override
    public void getLinks() {
        mLinks.clear();
        mLinksView.updateAdapter();
        getMoreLinks();
    }

    @Override
    public void getMoreLinks() {
        if (mLinksRequested)
            return;

        mLinksRequested = true;
        mLinksView.showSpinner("Getting submissions...");
        String id = mLinks == null || mLinks.size() == 0
                ? null : "t3_" + mLinks.get(mLinks.size() - 1).getId();
        mBus.post(new LoadLinksEvent(mSubreddit, mSort, mTimeSpan, id));
    }

    @Override
    public RedditLink getLink(int position) {
        return mLinks.get(position);
    }

    @Override
    public int getNumLinks() {
        return mLinks.size();
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
    public String getTimeSpan() {
        return mTimeSpan;
    }

    @Override
    public void updateTitle() {
        if (mSubreddit == null) {
            mLinksView.setTitle(mContext.getString(R.string.front_page_title));
        } else {
            mLinksView.setTitle(String.format(mContext.getString(R.string.subreddit_prefix), mSubreddit));
        }
    }

    @Override
    public void updateSubreddit(String subreddit) {
        mLinks.clear();
        mSubreddit = subreddit;
        mSort = Sort.HOT;
        mTimeSpan = TimeSpan.ALL;
        updateTitle();
        getLinks();
    }

    @Override
    public void updateSort(String sort) {
        if (mSort.equals(sort)) {
            return;
        }

        mLinks.clear();
        mSort = sort;
        getLinks();
    }

    @Override
    public void updateTimeSpan(String timespan) {
        if (mTimeSpan.equals(timespan)) {
            return;
        }

        mLinks.clear();
        mTimeSpan = timespan;
        getLinks();
    }

    @Subscribe
    public void onLinksLoaded(LinksLoadedEvent event) {
        mLinksView.dismissSpinner();
        if (event.isFailed()) {
            mLinksRequested = false;
            return;
        }

        if (mSubreddit != null && mSubreddit.equals("random")) {
            mSubreddit = event.getLinks().get(0).getSubreddit();
        }

        mLinks.addAll(event.getLinks());
        mLinksView.updateAdapter();
        updateTitle();
        mLinksRequested = false;
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        if (event.isFailed()) {
            mLinksView.showToast(R.string.vote_failed);
        }

        mLinkSelected.applyVote(event.getDirection());
        mLinksView.updateAdapter();
    }

    @Subscribe
    public void onSaveSubmitted(SaveSubmittedEvent event) {
        if (event.isFailed()) {
            mLinksView.showToast(R.string.save_failed);
        }

        mLinkSelected.isSaved(event.isToSave());
        mLinksView.updateAdapter();
    }

    @Subscribe
    public void onHideSubmitted(HideSubmittedEvent event) {
        if (event.isFailed()) {
            mLinksView.showToast(R.string.hide_failed);
        }

        hide(mLinkSelected, event.isToHide());
        mLinksView.updateAdapter();
    }

    @Subscribe
    public void onUserRequiredError(UserRequiredException e) {
        mLinksView.showToast(R.string.user_required);
    }

    @Override
    public void createContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link) {
        mLinkSelected = link;
        mLinksView.showLinkContextMenu(menu, v, menuInfo);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link_upvote:
                upvote();
                return true;
            case R.id.action_link_downvote:
                downvote();
                return true;
            case R.id.action_link_show_comments:
                openCommentsForLink();
                return true;
            case R.id.action_link_save:
                saveLink();
                return true;
            case R.id.action_link_unsave:
                unsaveLink();
                return true;
            case R.id.action_link_share:
                shareLink();
                return true;
            case R.id.action_link_open_in_browser:
                openLinkInBrowser();
                return true;
            case R.id.action_link_open_comments_in_browser:
                openCommentsInBrowser();
                return true;
            case R.id.action_link_hide:
                hideLink();
                return true;
            case R.id.action_link_report:
                reportLink();
                return true;
            default:
                return false;
        }
    }

    @Override
    public void openLink(RedditLink link) {
        if (link == null)
            return;

        if (link.isSelf()) {
            mLinksView.showCommentsForLink(link);
        } else {
            mLinksView.openWebViewForLink(link);
        }
    }

    @Override
    public void openCommentsForLink(RedditLink link) {
        mLinksView.showCommentsForLink(link);
    }

    @Override
    public void hide(RedditLink link, boolean toHide) {
        if (toHide) {
            mHiddenLinks.add(link.getId());
            mLinksView.updateAdapter();
        } else {
            mHiddenLinks.remove(link.getId());
            mLinksView.updateAdapter();
        }
    }

    @Override
    public boolean isHidden(RedditLink link) {
        return mHiddenLinks.contains(link.getId());
    }

    private void openCommentsForLink() {
        openCommentsForLink(mLinkSelected);
    }

    private void upvote() {
        int dir = (mLinkSelected.isLiked() == null || !mLinkSelected.isLiked()) ? 1 : 0;
        mBus.post(new VoteEvent(mLinkSelected.getKind(), mLinkSelected.getId(), dir));
    }

    private void downvote() {
        int dir = (mLinkSelected.isLiked() == null || mLinkSelected.isLiked()) ? -1 : 0;
        mBus.post(new VoteEvent(mLinkSelected.getKind(), mLinkSelected.getId(), dir));
    }

    private void saveLink() {
        mBus.post(new SaveEvent(mLinkSelected.getName(), null, true));
    }

    private void unsaveLink() {
        mBus.post(new SaveEvent(mLinkSelected.getName(), null, false));
    }

    private void shareLink() {
        mLinksView.openShareView(mLinkSelected);
    }

    private void openLinkInBrowser() {
        mLinksView.openLinkInBrowser(mLinkSelected);
    }

    private void openCommentsInBrowser() {
        mLinksView.openCommentsInBrowser(mLinkSelected);
    }

    private void hideLink() {
        mBus.post(new HideEvent(mLinkSelected.getName(), true));
    }

    private void unhideLink() {
        mBus.post(new HideEvent(mLinkSelected.getName(), false));
    }

    private void reportLink() {

    }
}
