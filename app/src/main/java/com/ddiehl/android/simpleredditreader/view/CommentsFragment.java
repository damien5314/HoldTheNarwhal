package com.ddiehl.android.simpleredditreader.view;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadHotCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.model.listings.Listing;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.web.ThumbnailCache;
import com.ddiehl.android.simpleredditreader.web.ThumbnailDownloader;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;
import java.util.List;

public class CommentsFragment extends ListFragment {
    private static final String TAG = CommentsFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_ARTICLE = "article";

    private Bus mBus;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private String mSubreddit;
    private String mArticleId;
    private RedditLink mLink;
    private List<Listing> mData;
    private CommentAdapter mCommentAdapter;
    private boolean mCommentsRetrieved = false;

    private TextView mLinkScore, mLinkTitle, mLinkAuthor, mLinkTimestamp, mLinkSubreddit, mLinkDomain;
    private ImageView mThumbnailView;

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
        setHasOptionsMenu(true);

        mThumbnailThread = new ThumbnailDownloader<>(new Handler());
        mThumbnailThread.setListener(new ThumbnailDownloader.Listener<ImageView>() {
            @Override
            public void onThumbnailDownloaded(ImageView imageView, String url, Bitmap thumbnail) {
                if (isVisible()) {
                    mThumbnailCache.addThumbnail(url, thumbnail);
                    imageView.setImageBitmap(thumbnail);
                }
            }
        });
        mThumbnailThread.start();
        mThumbnailThread.getLooper();

        mThumbnailCache = ThumbnailCache.getInstance();

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

        mLinkScore = (TextView) v.findViewById(R.id.link_score);
        mLinkTitle = (TextView) v.findViewById(R.id.link_title);
        mLinkAuthor = (TextView) v.findViewById(R.id.link_author);
        mLinkTimestamp = (TextView) v.findViewById(R.id.link_timestamp);
        mLinkSubreddit = (TextView) v.findViewById(R.id.link_subreddit);
        mLinkDomain = (TextView) v.findViewById(R.id.link_domain);
        mThumbnailView = (ImageView) v.findViewById(R.id.link_thumbnail);

        if (mLink != null) {
            populateLinkData();
        }
        
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
        mCommentsRetrieved = true;

        mLink = event.getLink();
        populateLinkData();

        mData.clear();
        mData.addAll(event.getComments());
        mCommentAdapter.notifyDataSetChanged();
    }

    private void populateLinkData() {
        if (mLink == null)
            return;

        String createDateFormatted = Utils.getFormattedDateStringFromUtc(mLink.getCreatedUtc().longValue());

        // Set content for each TextView
        mLinkScore.setText(String.valueOf(mLink.getScore()));
        mLinkTitle.setText(mLink.getTitle());
        mLinkAuthor.setText("/u/" + mLink.getAuthor());
        mLinkTimestamp.setText(createDateFormatted);
        mLinkSubreddit.setText("/r/" + mLink.getSubreddit());
        mLinkDomain.setText("(" + mLink.getDomain() + ")");

        // Register context menu to click event for score view
        mLinkScore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {

                } else { // Activate contextual action bar
                    openLinkContextMenu();
                }
            }
        });

        // Queue thumbnail to be downloaded, if one exists
        String thumbnailUrl = mLink.getThumbnail();
        if (thumbnailUrl.equals("nsfw")) {
            mThumbnailView.setImageResource(R.drawable.ic_nsfw);
        } else if (!thumbnailUrl.equals("")
                && !thumbnailUrl.equals("default")
                && !thumbnailUrl.equals("self")) {
            Bitmap thumbnail = mThumbnailCache.getThumbnail(thumbnailUrl);
            if (thumbnail == null) {
                mThumbnailThread.queueThumbnail(mThumbnailView, thumbnailUrl);
            } else {
                mThumbnailView.setImageBitmap(thumbnail);
            }
            mThumbnailView.setVisibility(View.VISIBLE);
        } else {
            mThumbnailView.setVisibility(View.GONE);
        }
    }

    @TargetApi(11)
    private void openLinkContextMenu() {
        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.menu_link_context, menu);
                menu.findItem(R.id.action_show_comments).setVisible(false); // Already on comments page
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_upvote:
                        mode.finish();
                        upvote(mLink);
                        return true;
                    case R.id.action_downvote:
                        mode.finish();
                        downvote(mLink);
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {

            }
        });
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.menu_link_context, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;

        switch (item.getItemId()) {
            // Set actions for each menu item
        }

        return super.onContextItemSelected(item);
    }

    private void upvote(RedditLink link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        getBus().post(new VoteEvent(link.getId(), dir));
    }

    private void downvote(RedditLink link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        getBus().post(new VoteEvent(link.getId(), dir));
    }

    private class CommentAdapter extends ArrayAdapter<Listing> {
        public CommentAdapter(List<Listing> data) {
            super(getActivity(), 0, data);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            LayoutInflater inflater = getActivity().getLayoutInflater();
            if (view == null) {
                view = inflater.inflate(R.layout.reddit_comment_item, null);
            }

            Listing comment = mData.get(position);

            // Add padding views to indentation_wrapper based on depth of comment
            ViewGroup indentationWrapper = (ViewGroup) view.findViewById(R.id.indentation_wrapper);
            indentationWrapper.removeAllViews(); // Reset padding for recycled comment views
            int depth = comment instanceof RedditComment ?
                    ((RedditComment) comment).getDepth() : ((RedditMoreComments) comment).getDepth();
            for (int i = 0; i < depth - 1; i++) {
                View paddingView = inflater.inflate(R.layout.comment_padding_view, indentationWrapper);
                // Set background color for padding view
//                int[] colors = getResources().getIntArray(R.array.indentation_colors);
//                int color = Math.min(i, colors.length - 1);
//                paddingView.setBackgroundColor(colors[color]);
            }

            for (int j = 0; j < indentationWrapper.getChildCount(); j++) {
                // Set background color for padding view
                int[] colors = getResources().getIntArray(R.array.indentation_colors);
                int color = Math.min(j, colors.length - 1);
                indentationWrapper.getChildAt(j).setBackgroundColor(colors[color]);
            }

            // Populate attributes of comment in layout
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

    private Bus getBus() {
        if (mBus == null) {
            mBus = BusProvider.getInstance();
        }
        return mBus;
    }
}
