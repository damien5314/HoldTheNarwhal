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
import com.ddiehl.android.simpleredditreader.events.responses.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.HideSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.SaveSubmittedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.exceptions.UserRequiredException;
import com.ddiehl.android.simpleredditreader.view.LinksView;
import com.ddiehl.reddit.Savable;
import com.ddiehl.reddit.Sort;
import com.ddiehl.reddit.TimeSpan;
import com.ddiehl.reddit.Votable;
import com.ddiehl.reddit.listings.RedditLink;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class LinksPresenterImpl implements LinksPresenter {
    private static final String TAG = LinksPresenterImpl.class.getSimpleName();

    private Context mContext;
    private Bus mBus;
    private List<RedditLink> mLinks;
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
        mBus = BusProvider.getInstance();

        mSubreddit = subreddit;
        mSort = Sort.HOT;
        mTimeSpan = TimeSpan.ALL;
    }

    @Override
    public void getLinks() {
        mLinks.clear();
        mLinksView.getListAdapter().notifyDataSetChanged();
        getMoreLinks();
    }

    @Override
    public void getMoreLinks() {
        if (mLinksRequested)
            return;

        mLinksRequested = true;
        mLinksView.showSpinner(R.string.spinner_getting_submissions);
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

        mSort = sort;
        getLinks();
    }

    @Override
    public void updateTimeSpan(String timespan) {
        if (mTimeSpan.equals(timespan)) {
            return;
        }

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
        mLinksView.getListAdapter().notifyDataSetChanged();
        updateTitle();
        mLinksRequested = false;
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        mLinksView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        RedditLink link = event.getLink();
        mLinks.clear();
        mLinks.add(link);
        mLinksView.setTitle(link.getTitle());
    }

    @Subscribe
    public void onVoteSubmitted(VoteSubmittedEvent event) {
        if (event.isFailed()) {
            mLinksView.showToast(R.string.vote_failed);
            return;
        }

        Votable listing = event.getListing();
        if (listing instanceof RedditLink) {
            listing.applyVote(event.getDirection());
            mLinksView.getListAdapter().notifyItemChanged(mLinks.indexOf(listing));
        }
    }

    @Subscribe
    public void onLinkSaved(SaveSubmittedEvent event) {
        if (event.isFailed()) {
            mLinksView.showToast(R.string.save_failed);
            return;
        }

        Savable listing = event.getListing();
        if (listing instanceof RedditLink) {
            listing.isSaved(event.isToSave());
            mLinksView.getListAdapter().notifyItemChanged(mLinks.indexOf(listing));
        }
    }

    @Subscribe
    public void onLinkHidden(HideSubmittedEvent event) {
        if (event.isFailed()) {
            mLinksView.showToast(R.string.hide_failed);
            return;
        }

        int pos = mLinks.indexOf(event.getLink());
        if (event.isToHide()) {
            mLinksView.showToast(R.string.link_hidden);
            mLinks.remove(pos);
            mLinksView.getListAdapter().notifyItemRemoved(mLinks.indexOf(event.getLink()));
        } else {
            mLinksView.getListAdapter().notifyItemChanged(mLinks.indexOf(event.getLink()));
        }
    }

    @Subscribe
    public void onUserRequiredError(UserRequiredException e) {
        mLinksView.showToast(R.string.user_required);
    }

    @Override
    public void showContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo, RedditLink link) {
        mLinkSelected = link;
        mLinksView.showLinkContextMenu(menu, v, menuInfo);
        menu.findItem(R.id.action_link_save).setVisible(!link.isSaved());
        menu.findItem(R.id.action_link_unsave).setVisible(link.isSaved());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_link_upvote:
                upvote(mLinkSelected);
                return true;
            case R.id.action_link_downvote:
                downvote(mLinkSelected);
                return true;
            case R.id.action_link_show_comments:
                openCommentsForLink(mLinkSelected);
                return true;
            case R.id.action_link_save:
                saveLink(mLinkSelected);
                return true;
            case R.id.action_link_unsave:
                unsaveLink(mLinkSelected);
                return true;
            case R.id.action_link_share:
                shareLink(mLinkSelected);
                return true;
            case R.id.action_link_open_in_browser:
                openLinkInBrowser(mLinkSelected);
                return true;
            case R.id.action_link_open_comments_in_browser:
                openCommentsInBrowser(mLinkSelected);
                return true;
            case R.id.action_link_hide:
                hideLink(mLinkSelected);
                return true;
            case R.id.action_link_report:
                reportLink(mLinkSelected);
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

    private void upvote(RedditLink link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        mBus.post(new VoteEvent(link, "t3", dir));
    }

    private void downvote(RedditLink link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        mBus.post(new VoteEvent(link, "t3", dir));
    }

    private void saveLink(RedditLink link) {
        mBus.post(new SaveEvent(link, null, true));
    }

    private void unsaveLink(RedditLink link) {
        mBus.post(new SaveEvent(link, null, false));
    }

    private void shareLink(RedditLink link) {
        mLinksView.openShareView(link);
    }

    private void openLinkInBrowser(RedditLink link) {
        mLinksView.openLinkInBrowser(link);
    }

    private void openCommentsInBrowser(RedditLink link) {
        mLinksView.openCommentsInBrowser(link);
    }

    private void hideLink(RedditLink link) {
        mBus.post(new HideEvent(link, true));
    }

    private void unhideLink(RedditLink link) {
        mBus.post(new HideEvent(link, false));
    }

    private void reportLink(RedditLink link) {

    }
}
