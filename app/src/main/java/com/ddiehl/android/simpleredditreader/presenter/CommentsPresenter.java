package com.ddiehl.android.simpleredditreader.presenter;

import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;

public interface CommentsPresenter {

    public void getComments();
    public void getMoreChildren(RedditMoreComments comment);
    public void updateSort(String sort);
    public void upvoteLink();
    public void downvoteLink();
    public void onContextItemSelected(int id);
    public RedditLink getRedditLink();
    public AbsRedditComment getCommentAtPosition(int position);
    public void toggleThreadVisible(AbsRedditComment comment);
    public int getNumComments();

}
