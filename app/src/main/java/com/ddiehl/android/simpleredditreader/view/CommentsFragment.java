package com.ddiehl.android.simpleredditreader.view;

import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotCommentsEvent;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends ListFragment {
    private static final String TAG = CommentsFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_ARTICLE = "article";

    private Bus mBus;
    private String mSubreddit;
    private String mArticleId;

    private List<Listing> mData;
    private CommentAdapter mCommentAdapter;
    private boolean mCommentsRetrieved = false;

    public CommentsFragment() { /* Default constructor */ }

    public static CommentsFragment newInstance(String subreddit, String article) {
        Bundle args = new Bundle();
        args.putString(ARG_SUBREDDIT, subreddit);
        args.putString(ARG_ARTICLE, article);
        CommentsFragment fragment = new CommentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle args = getArguments();
        mSubreddit = args.getString(ARG_SUBREDDIT);
        mArticleId = args.getString(ARG_ARTICLE);

        mData = new ArrayList<>();
        mCommentAdapter = new CommentAdapter(mData);
        setListAdapter(mCommentAdapter);

        getActivity().setTitle(mArticleId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comments_fragment, null);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        if (!mCommentsRetrieved) {
            getBus().post(new LoadHotCommentsEvent(mSubreddit, mArticleId));
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        Log.d(TAG, "Comments loaded for: " + mArticleId);
        mCommentsRetrieved = true;

        mData.clear();
        mData.addAll(event.getComments());
        mCommentAdapter.notifyDataSetChanged();
    }

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }

    private class CommentAdapter extends ArrayAdapter<Listing> {
        public CommentAdapter(List<Listing> data) {
            super(getActivity(), 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null) {
                view = getActivity().getLayoutInflater().inflate(R.layout.comments_list_item, null);
            }

            Listing comment = mData.get(position);
            if (comment instanceof RedditComment) {
                TextView vAuthor = (TextView) view.findViewById(R.id.comment_author);
                vAuthor.setText("/u/" + ((RedditComment) comment).getAuthor());
                TextView vScore = (TextView) view.findViewById(R.id.comment_score);
                vScore.setText("[" + ((RedditComment) comment).getScore() + "]");
                TextView vTimestamp = (TextView) view.findViewById(R.id.comment_timestamp);
                vTimestamp.setText(Utils.getFormattedDateStringFromUtc(((RedditComment) comment).getCreateUtc().longValue()));
                TextView vBody = (TextView) view.findViewById(R.id.comment_body);
                vBody.setText(((RedditComment) comment).getBody());
            } else {
                TextView vBody = (TextView) view.findViewById(R.id.comment_body);
                vBody.setText("More comments (" + ((RedditMoreComments) comment).getCount() + ")");
            }

            return view;
        }
    }
}
