package com.ddiehl.android.simpleredditreader.view;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.Utils;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.VoteEvent;
import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
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

public class CommentsFragment extends Fragment {
    private static final String TAG = CommentsFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_ARTICLE = "article";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";

    private Bus mBus;
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private String mSubreddit;
    private String mArticleId;
    private String mSort;
    private RedditLink mLink;
    private List<Listing> mData;
    private CommentAdapter mCommentAdapter;
    private boolean mCommentsRetrieved = false;

    private TextView mSelfText;
    private TextView mLinkScore, mLinkTitle, mLinkAuthor, mLinkTimestamp, mLinkSubreddit, mLinkDomain;
    private ProgressDialog mProgressBar;

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

        mSort = RedditPreferences.getCommentSort(getActivity());

        mData = new ArrayList<>();
        mCommentAdapter = new CommentAdapter();

        getActivity().setTitle(getString(R.string.comments_fragment_default_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comments_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mCommentAdapter);

        mSelfText = (TextView) v.findViewById(R.id.link_self_text);
        mLinkScore = (TextView) v.findViewById(R.id.link_score);
        mLinkTitle = (TextView) v.findViewById(R.id.link_title);
        mLinkAuthor = (TextView) v.findViewById(R.id.link_author);
        mLinkTimestamp = (TextView) v.findViewById(R.id.link_timestamp);
        mLinkSubreddit = (TextView) v.findViewById(R.id.link_subreddit);
        mLinkDomain = (TextView) v.findViewById(R.id.link_domain);

        if (mLink != null) {
            populateLinkData();
            getActivity().setTitle(mLink.getTitle());
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        getBus().register(this);

        if (!mCommentsRetrieved) {
            getComments();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        getBus().unregister(this);
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        dismissSpinner();
        if (event.isFailed()) {
            Log.e(TAG, "Error loading links", event.getError());
            return;
        }

        mCommentsRetrieved = true;

        mLink = event.getLink();
        populateLinkData();
        getActivity().setTitle(mLink.getTitle());

        mData.clear();
        mData.addAll(event.getComments());
        syncVisibleData();
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

        if (mLink.isSelf()) {
            mSelfText.setText(mLink.getSelftext());
        } else {
            mSelfText.setVisibility(View.GONE);
        }
    }

    public void updateSort(String sort) {
        mData.clear();
        mSort = sort;
        getComments();
    }

    private void getComments() {
        showSpinner();
        mCommentsRetrieved = true;
        getBus().post(new LoadCommentsEvent(mSubreddit, mArticleId, mSort));
    }

    @TargetApi(11)
    private void openLinkContextMenu() {
        getActivity().startActionMode(new ActionMode.Callback() {
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mode.getMenuInflater().inflate(R.menu.link_context_menu, menu);
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

    private void upvote(RedditLink link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        getBus().post(new VoteEvent(link.getId(), dir));
    }

    private void downvote(RedditLink link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        getBus().post(new VoteEvent(link.getId(), dir));
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        getActivity().getMenuInflater().inflate(R.menu.link_context_menu, menu);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CHOOSE_SORT:
                if (resultCode == Activity.RESULT_OK) {
                    String selectedSort = data.getStringExtra(ChooseLinkSortDialog.EXTRA_SORT);
                    if (!mSort.equals(selectedSort)) {
                        mSort = selectedSort;
                        RedditPreferences.saveCommentSort(getActivity(), selectedSort);
                        mData.clear();
                        getComments();
                    }
                }
                getActivity().supportInvalidateOptionsMenu();
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.comments_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        switch (item.getItemId()) {
            case R.id.action_change_sort:
                ChooseCommentSortDialog chooseCommentSortDialog = ChooseCommentSortDialog.newInstance(mSort);
                chooseCommentSortDialog.setTargetFragment(this, REQUEST_CHOOSE_SORT);
                chooseCommentSortDialog.show(fm, DIALOG_CHOOSE_SORT);
                return true;
            case R.id.action_refresh:
                mData.clear();
                getComments();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSpinner() {
        if (mProgressBar == null) {
            mProgressBar = new ProgressDialog(getActivity(), R.style.ProgressDialog);
            mProgressBar.setCancelable(false);
            mProgressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        }
        mProgressBar.show();
    }

    private void dismissSpinner() {
        mProgressBar.dismiss();
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

    private List<AbsRedditComment> mDataDisplayed = new ArrayList<>();

    private void syncVisibleData() {
        mDataDisplayed.clear();
        for (Listing comment : mData) {
            if (((AbsRedditComment) comment).isVisible()) {
                mDataDisplayed.add((AbsRedditComment) comment);
            }
        }
    }

    private class CommentAdapter extends RecyclerView.Adapter<CommentHolder> {
        @Override
        public CommentHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.reddit_comment_item, parent, false);
            return new CommentHolder(view);
        }

        @Override
        public void onBindViewHolder(CommentHolder holder, int position) {
            AbsRedditComment comment = mDataDisplayed.get(position);
            holder.bindComment(comment);
        }

        @Override
        public int getItemCount() {
            return mDataDisplayed.size();
        }
    }

    private class CommentHolder extends RecyclerView.ViewHolder {
        private AbsRedditComment mRedditComment;
        private ViewGroup mIndentationWrapper;
        private ViewGroup mExpanderView;
        private ImageView mExpanderIcon;
        private TextView mAuthorView;
        private TextView mScoreView;
        private TextView mTimestampView;
        private TextView mMoreCommentsView;
        private TextView mBodyView;

        public CommentHolder(View view) {
            super(view);
            mIndentationWrapper = (ViewGroup) view.findViewById(R.id.indentation_wrapper);
            mExpanderView = (ViewGroup) view.findViewById(R.id.comment_expander_view);
            mExpanderIcon = (ImageView) view.findViewById(R.id.comment_expander_icon);
            mAuthorView = (TextView) view.findViewById(R.id.comment_author);
            mScoreView = (TextView) view.findViewById(R.id.comment_score);
            mTimestampView = (TextView) view.findViewById(R.id.comment_timestamp);
            mMoreCommentsView = (TextView) view.findViewById(R.id.comment_more);
            mBodyView = (TextView) view.findViewById(R.id.comment_body);
        }

        public void bindComment(final AbsRedditComment comment) {
            mRedditComment = comment;

            // Add padding views to indentation_wrapper based on depth of comment
            LayoutInflater inflater = getActivity().getLayoutInflater();
            mIndentationWrapper.removeAllViews(); // Reset padding for recycled comment views
            int depth = comment.getDepth();
            for (int i = 0; i < depth - 1; i++) {
                View paddingView = inflater.inflate(R.layout.comment_padding_view, mIndentationWrapper, false);
                // Set background color for padding view
                int[] colors = getResources().getIntArray(R.array.indentation_colors);
                if (i == depth-2)
                    paddingView.setBackgroundColor(colors[i % colors.length]);
                mIndentationWrapper.addView(paddingView);
            }

            // Populate attributes of comment in layout
            if (comment instanceof RedditComment) {
                mAuthorView.setVisibility(View.VISIBLE);
                mScoreView.setVisibility(View.VISIBLE);
                mTimestampView.setVisibility(View.VISIBLE);
                mBodyView.setVisibility(View.VISIBLE);
                mMoreCommentsView.setVisibility(View.GONE);
                mAuthorView.setText(((RedditComment) comment).getAuthor());
                mScoreView.setText("• " + ((RedditComment) comment).getScore() + " •");
                mTimestampView.setText(Utils.getFormattedDateStringFromUtc(((RedditComment) comment).getCreateUtc().longValue()));
                mBodyView.setText(((RedditComment) comment).getBody());
                if (comment.isCollapsed()) {
                    mBodyView.setVisibility(View.GONE);
                    mExpanderIcon.setImageResource(R.drawable.ic_thread_expand);
                } else {
                    mBodyView.setVisibility(View.VISIBLE);
                    mExpanderIcon.setImageResource(R.drawable.ic_thread_collapse);
                }
            } else {
                mExpanderIcon.setImageResource(R.drawable.ic_thread_expand);
                mAuthorView.setVisibility(View.GONE);
                mScoreView.setVisibility(View.GONE);
                mTimestampView.setVisibility(View.GONE);
                mBodyView.setVisibility(View.GONE);
                mMoreCommentsView.setVisibility(View.VISIBLE);
                int count = ((RedditMoreComments) comment).getCount();
                if (count == 0) { // continue thread
                    mMoreCommentsView.setText(getString(R.string.continue_thread));
                } else { // more comments in current thread
                    mMoreCommentsView.setText(getString(R.string.more_comments) + " (" + count + ")");
                }
            }

            if (comment instanceof RedditComment) {
                mExpanderView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        setThreadVisible(mData.indexOf(comment), comment.isCollapsed());
                        mCommentAdapter.notifyDataSetChanged();
                    }
                });
            }
        }
    }

    private void setThreadVisible(int position, boolean visible) {
        int totalItemCount = mData.size();
        RedditComment parentComment = (RedditComment) mData.get(position);
        int parentCommentDepth = parentComment.getDepth();
        parentComment.setCollapsed(!visible);
        ArrayList<Integer> collapsedCommentLevels = new ArrayList<>();
        if (parentComment.isCollapsed()) {
            collapsedCommentLevels.add(parentCommentDepth);
        }

        // Check to make sure the next comment isn't out of range of the full list
        if (position + 1 < totalItemCount) {
            // Retrieve first child comment
            int currentChildCommentPosition = position + 1;
            AbsRedditComment currentChildComment = (AbsRedditComment) mData.get(currentChildCommentPosition);
            int currentChildCommentDepth = currentChildComment.getDepth();
            // Loop through remaining comments until we reach another comment at same depth
            // as the parent, or we reach the end of the comment list
            while (currentChildCommentDepth > parentCommentDepth
                    && currentChildCommentPosition < totalItemCount) {
                // Loop through list of collapsed depths
                // If the current comment is less than a depth, remove that depth from the list
                for (int i = 0; i < collapsedCommentLevels.size(); i++) {
                    if (currentChildCommentDepth <= collapsedCommentLevels.get(i)) {
                        collapsedCommentLevels.remove(i);
                    }
                }
                // If the comment is collapsed, add it to the list of collapsed depths
                if (currentChildComment.isCollapsed()) {
                    collapsedCommentLevels.add(currentChildCommentDepth);
                }
                // Loop through collapsed depth list from parent depth until child depth
                // If any depth is collapsed less than child depth, set child to invisible
                boolean collapsedFromParent = false;
                for (int i = parentCommentDepth; i < currentChildCommentDepth; i++) {
                    if (collapsedCommentLevels.contains(i)) {
                        currentChildComment.setVisible(false);
                        collapsedFromParent = true;
                    }
                }
                // If comment was not collapsed from any collapsed parent, set visibility
                if (!collapsedFromParent) {
                    currentChildComment.setVisible(visible);
                }
                // Increment position
                currentChildCommentPosition++;
                // Retrieve comment at current position
                if (currentChildCommentPosition < totalItemCount) {
                    currentChildComment = (AbsRedditComment) mData.get(currentChildCommentPosition);
                    currentChildCommentDepth = currentChildComment.getDepth();
                }
            }
        }

        syncVisibleData();
    }
}
