package com.ddiehl.android.simpleredditreader.web;

import android.util.Log;

import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LinksLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.LoadLinksEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.events.VoteSubmittedEvent;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.ListingResponse;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;


public class RedditService {
    private static final String TAG = RedditService.class.getSimpleName();
    private RedditApi mApi;
    private Bus mBus;

    public RedditService(RedditApi api, Bus bus) {
        mApi = api;
        mBus = bus;
    }

    /**
     * Retrieves link listings for subreddit
     */
    @Subscribe
    public void onLoadLinks(LoadLinksEvent event) {
        String subreddit = event.getSubreddit();
        String sort = event.getSort();
        String timespan = event.getTimeSpan();
        String after = event.getAfter();
        Log.i(TAG, "Loading links for /r/" + subreddit + "/" + sort + ".json?t=" + timespan);

        if (subreddit == null) {
            mApi.getDefaultLinks(sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
                }
            });
        } else {
            mApi.getLinks(subreddit, sort, timespan, after, new Callback<ListingResponse>() {
                @Override
                public void success(ListingResponse linksResponse, Response response) {
                    mBus.post(new LinksLoadedEvent(linksResponse));
                }

                @Override
                public void failure(RetrofitError error) {
                    Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
                    mBus.post(new LinksLoadedEvent(error));
                }
            });
        }
    }

    /**
     * Retrieves comment listings for link passed as parameter
     */
    @Subscribe
    public void onLoadComments(LoadHotCommentsEvent event) {
        String subreddit = event.getSubreddit();
        String article = event.getArticle();

        Log.i(TAG, "Loading comments for /r/" + subreddit + "/" + article);
        mApi.getComments(subreddit, article, new Callback<List<ListingResponse>>() {
            @Override
            public void success(List<ListingResponse> listingsList, Response response) {
                Log.i(TAG, "Comments loaded successfully");
                flattenList(listingsList.get(1));
                mBus.post(new CommentsLoadedEvent(listingsList));
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(TAG, "RetrofitError: " + error.getMessage(), error);
            }
        });
    }

    /**
     * Flattens list of comments, marking each comment with depth
     */
    private void flattenList(ListingResponse commentsResponse) {
        List<Listing> commentList = commentsResponse.getData().getChildren();
        int i = 0;
        while (i < commentList.size()) {
            Listing listing = commentList.get(i);
            if (listing instanceof RedditComment) {
                RedditComment comment = (RedditComment) listing;
                ListingResponse replies = comment.getReplies();
                if (replies != null) {
                    flattenList(replies);
                }
                comment.setDepth(comment.getDepth() + 1); // Increase depth by 1
                if (comment.getReplies() != null) {
                    commentList.addAll(i+1, comment.getReplies().getData().getChildren()); // Add all of the replies to commentList
                    comment.setReplies(null); // Remove replies for comment
                }
            } else { // Listing is a RedditMoreComments
                RedditMoreComments moreComments = (RedditMoreComments) listing;
                moreComments.setDepth(moreComments.getDepth() + 1); // Increase depth by 1
//                commentList.add(i+1, moreComments);
            }
            i++;
        }

    }

    /**
     * Submits a vote on a link or comment
     */
    @Subscribe
    public void onVote(VoteEvent event) {
        String id = event.getId();
        int direction = event.getDirection();
        mApi.vote(id, direction, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                mBus.post(new VoteSubmittedEvent(response));
            }

            @Override
            public void failure(RetrofitError error) {
                mBus.post(new VoteSubmittedEvent(error));
            }
        });
    }
}
