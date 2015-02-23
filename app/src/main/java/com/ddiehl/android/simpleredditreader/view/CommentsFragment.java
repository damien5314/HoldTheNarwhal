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
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
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

    private TextView mSelfText;
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
        View v = inflater.inflate(R.layout.comments_fragment, container, false);

        ListView listView = (ListView) v.findViewById(android.R.id.list);
        setListViewHeightBasedOnChildren(listView);

        mSelfText = (TextView) v.findViewById(R.id.link_self_text);
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

        if (mLink.isSelf()) {
            mSelfText.setText(mLink.getSelftext());
        } else {
            mSelfText.setVisibility(View.GONE);
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
                view = inflater.inflate(R.layout.reddit_comment_item, parent, false);
            }

            Listing comment = mData.get(position);

            // Add padding views to indentation_wrapper based on depth of comment
            ViewGroup indentationWrapper = (ViewGroup) view.findViewById(R.id.indentation_wrapper);
            indentationWrapper.removeAllViews(); // Reset padding for recycled comment views
            int depth = comment instanceof RedditComment ?
                    ((RedditComment) comment).getDepth() : ((RedditMoreComments) comment).getDepth();
            for (int i = 0; i < depth - 1; i++) {
                View paddingView = inflater.inflate(R.layout.comment_padding_view, indentationWrapper, false);
                // Set background color for padding view
                int[] colors = getResources().getIntArray(R.array.indentation_colors);
                int index = Math.min(i, colors.length - 1);
//                paddingView.setBackgroundColor(colors[index]);
                paddingView.setBackgroundColor(0xFFCCD9FF);
                indentationWrapper.addView(paddingView);
            }

            ImageView vExpander = (ImageView) view.findViewById(R.id.comment_expander);
            TextView vAuthor = (TextView) view.findViewById(R.id.comment_author);
            TextView vScore = (TextView) view.findViewById(R.id.comment_score);
            TextView vTimestamp = (TextView) view.findViewById(R.id.comment_timestamp);
            TextView vMoreComments = (TextView) view.findViewById(R.id.comment_more);
            TextView vBody = (TextView) view.findViewById(R.id.comment_body);

            // Populate attributes of comment in layout
            if (comment instanceof RedditComment) {
                vExpander.setImageResource(R.drawable.ic_thread_contract);
                vAuthor.setVisibility(View.VISIBLE);
                vScore.setVisibility(View.VISIBLE);
                vTimestamp.setVisibility(View.VISIBLE);
                vBody.setVisibility(View.VISIBLE);
                vMoreComments.setVisibility(View.GONE);
                vAuthor.setText(((RedditComment) comment).getAuthor());
                vScore.setText("[" + ((RedditComment) comment).getScore() + "]");
                vTimestamp.setText(Utils.getFormattedDateStringFromUtc(((RedditComment) comment).getCreateUtc().longValue()));
                vBody.setText(((RedditComment) comment).getBody());
            } else {
                vExpander.setImageResource(R.drawable.ic_thread_expand);
                vAuthor.setVisibility(View.GONE);
                vScore.setVisibility(View.GONE);
                vTimestamp.setVisibility(View.GONE);
                vBody.setVisibility(View.GONE);
                vMoreComments.setVisibility(View.VISIBLE);
                int count = ((RedditMoreComments) comment).getCount();
                if (count == 0) { // continue thread
                    vMoreComments.setText(getString(R.string.continue_thread));
                } else { // more comments in current thread
                    vMoreComments.setText(getString(R.string.more_comments) + " (" + count + ")");
                }
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

    /**** Method for Setting the Height of the ListView dynamically.
     **** Hack to fix the issue of not showing all the items of the ListView
     **** when placed inside a ScrollView  ****/
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, AbsListView.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
        listView.requestLayout();
    }
}
