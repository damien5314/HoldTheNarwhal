package com.ddiehl.android.simpleredditreader.presenter;

import android.content.Context;

import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.responses.CommentThreadLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.CommentBank;
import com.ddiehl.android.simpleredditreader.model.listings.CommentBankList;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.view.CommentsView;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

public class CommentsPresenterImpl implements CommentsPresenter {

    private Context mContext;

    private CommentsView mCommentsView;
    private RedditLink mRedditLink;
    private CommentBank mCommentBank;
    private Bus mBus;
    private RedditPreferences mPreferences;

    private String mSubreddit;
    private String mArticleId;
    private String mSort;

    public CommentsPresenterImpl(Context context, CommentsView commentsView, String subreddit, String articleId) {
        mContext = context.getApplicationContext();
        mCommentsView = commentsView;
        mCommentBank = new CommentBankList();
        mBus = BusProvider.getInstance();

        mPreferences = RedditPreferences.getInstance(mContext);

        mSubreddit = subreddit;
        mArticleId = articleId;
        mSort = mPreferences.getCommentSort();
    }

    @Override
    public void getComments() {
        mCommentsView.showSpinner(null);
        mBus.post(new LoadCommentsEvent(mSubreddit, mArticleId, mSort));
    }

    @Override
    public RedditLink getLink() {
        return mRedditLink;
    }

    @Override
    public void setLink(RedditLink link) {
        mRedditLink = link;
    }

    @Override
    public void getMoreChildren(RedditMoreComments comment) {
        mCommentsView.showSpinner(null);
        List<String> children = comment.getChildren();
        mBus.post(new LoadMoreChildrenEvent(mRedditLink, comment, children, mSort));
    }

    @Override
    public void updateSort(String sort) {
        if (!mSort.equals(sort)) {
            mSort = sort;
            mPreferences.saveCommentSort(mSort);
            getComments();
        }
    }

    @Override
    public AbsRedditComment getCommentAtPosition(int position) {
        return mCommentBank.getVisibleComment(position);
    }

    @Override
    public void toggleThreadVisible(AbsRedditComment comment) {
        mCommentBank.toggleThreadVisible(comment);
        mCommentsView.updateAdapter();
    }

    @Override
    public int getNumComments() {
        return mCommentBank.getNumVisible();
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        mCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        mRedditLink = event.getLink();
        mCommentsView.setTitle(mRedditLink.getTitle());

        List<AbsRedditComment> comments = event.getComments();
        AbsRedditComment.flattenCommentList(comments);
        mCommentBank.setData(comments);
        mCommentsView.updateAdapter();
    }

    @Subscribe
    public void onMoreChildrenLoaded(MoreChildrenLoadedEvent event) {
        mCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        RedditMoreComments parentStub = event.getParentStub();
        List<AbsRedditComment> comments = event.getComments();

        if (comments.size() == 0) {
            mCommentBank.remove(parentStub);
        } else {
            AbsRedditComment.setDepthForCommentsList(comments, parentStub.getDepth());

            for (int i = 0; i < mCommentBank.size(); i++) {
                AbsRedditComment comment = mCommentBank.get(i);
                if (comment instanceof RedditMoreComments) {
                    String id = ((RedditMoreComments) comment).getId();
                    if (id.equals(parentStub.getId())) { // Found the base comment
                        mCommentBank.remove(i);
                        mCommentBank.addAll(i, comments);
                        break;
                    }
                }
            }
        }

        mCommentsView.updateAdapter();
    }

    @Subscribe
    public void onCommentThreadLoaded(CommentThreadLoadedEvent event) {
        mCommentsView.dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        List<AbsRedditComment> comments = event.getComments();
        AbsRedditComment.flattenCommentList(comments);

        if (comments.size() == 0)
            return;

        // Increase each comment by the parent depth
        for (AbsRedditComment comment : comments) {
            comment.setDepth(comment.getDepth() + event.getParentDepth() - 1);
        }

        // Iterate through the existing data list to find where the base comment lies
        RedditComment targetComment = (RedditComment) comments.get(0);
        for (int i = 0; i < mCommentBank.size(); i++) {
            AbsRedditComment comment = mCommentBank.get(i);
            if (comment instanceof RedditMoreComments) {
                String id = ((RedditMoreComments) comment).getId();
                if (id.equals(targetComment.getId())) { // Found the base comment
                    mCommentBank.remove(i);
                    mCommentBank.addAll(i, comments);
                    break;
                }
            }
        }

        mCommentsView.updateAdapter();
    }
}
