package com.ddiehl.android.simpleredditreader.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;
import com.ddiehl.android.simpleredditreader.RedditPreferences;
import com.ddiehl.android.simpleredditreader.events.BusProvider;
import com.ddiehl.android.simpleredditreader.events.responses.CommentThreadLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.responses.CommentsLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadCommentsEvent;
import com.ddiehl.android.simpleredditreader.events.requests.LoadMoreChildrenEvent;
import com.ddiehl.android.simpleredditreader.events.responses.MoreChildrenLoadedEvent;
import com.ddiehl.android.simpleredditreader.events.requests.VoteEvent;
import com.ddiehl.android.simpleredditreader.model.listings.AbsRedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.CommentBank;
import com.ddiehl.android.simpleredditreader.model.listings.CommentBankList;
import com.ddiehl.android.simpleredditreader.model.listings.RedditComment;
import com.ddiehl.android.simpleredditreader.model.listings.RedditLink;
import com.ddiehl.android.simpleredditreader.model.listings.RedditMoreComments;
import com.ddiehl.android.simpleredditreader.model.web.ThumbnailCache;
import com.ddiehl.android.simpleredditreader.model.web.ThumbnailDownloader;
import com.ddiehl.android.simpleredditreader.utils.BaseUtils;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import java.util.List;

public class CommentsFragment extends Fragment {
    private static final String TAG = CommentsFragment.class.getSimpleName();

    private static final String ARG_SUBREDDIT = "subreddit";
    private static final String ARG_ARTICLE = "article";

    private static final int REQUEST_CHOOSE_SORT = 0;
    private static final String DIALOG_CHOOSE_SORT = "dialog_choose_sort";

    private Bus mBus = BusProvider.getInstance();
    private ThumbnailDownloader<ImageView> mThumbnailThread;
    private ThumbnailCache mThumbnailCache;

    private CommentBank mCommentBank;

    private String mSubreddit;
    private String mArticleId;
    private String mSort;
    private RedditLink mRedditLink;
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

        mSort = RedditPreferences.getInstance(getActivity()).getCommentSort();

        mCommentBank = new CommentBankList();
        mCommentAdapter = new CommentAdapter();

        getActivity().setTitle(getString(R.string.comments_fragment_default_title));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.comments_fragment, container, false);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(mCommentAdapter);

        if (mRedditLink != null) {
            getActivity().setTitle(mRedditLink.getTitle());
        }

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        mBus.register(this);

        if (!mCommentsRetrieved) {
            getComments();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mBus.unregister(this);
    }

    @Subscribe
    public void onCommentsLoaded(CommentsLoadedEvent event) {
        ((MainActivity) getActivity()).dismissSpinner();
        if (event.isFailed()) {
            return;
        }

        mCommentsRetrieved = true;

        mRedditLink = event.getLink();
        getActivity().setTitle(mRedditLink.getTitle());

        List<AbsRedditComment> comments = event.getComments();
        AbsRedditComment.flattenCommentList(comments);
        mCommentBank.setData(event.getComments());
        mCommentAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onMoreChildrenLoaded(MoreChildrenLoadedEvent event) {
        ((MainActivity) getActivity()).dismissSpinner();
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

        mCommentAdapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onCommentThreadLoaded(CommentThreadLoadedEvent event) {
        ((MainActivity) getActivity()).dismissSpinner();
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

        mCommentAdapter.notifyDataSetChanged();
    }

    public void updateSort(String sort) {
        mCommentBank.clear();
        mSort = sort;
        RedditPreferences.getInstance(getActivity()).saveCommentSort(sort);
        getComments();
    }

    private void getComments() {
        ((MainActivity) getActivity()).showSpinner(null);
        mCommentsRetrieved = true;
        mBus.post(new LoadCommentsEvent(mSubreddit, mArticleId, mSort));
    }

    private void getMoreChildren(RedditMoreComments comment) {
        ((MainActivity) getActivity()).showSpinner(null);
        List<String> children = comment.getChildren();
        mBus.post(new LoadMoreChildrenEvent(mRedditLink, comment, children, mSort));
    }

    private void upvote(RedditLink link) {
        int dir = (link.isLiked() == null || !link.isLiked()) ? 1 : 0;
        mBus.post(new VoteEvent(link.getKind(), link.getId(), dir));
    }

    private void downvote(RedditLink link) {
        int dir = (link.isLiked() == null || link.isLiked()) ? -1 : 0;
        mBus.post(new VoteEvent(link.getKind(), link.getId(), dir));
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
                        updateSort(selectedSort);
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
                mCommentBank.clear();
                getComments();
                return true;
            case R.id.action_settings:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class CommentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            switch (viewType) {
                case TYPE_HEADER:
                    View view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.link_item, parent, false);
                    return new LinkHolder(view);
                case TYPE_ITEM:
                    view = LayoutInflater.from(parent.getContext())
                            .inflate(R.layout.comment_item, parent, false);
                    return new CommentHolder(view);
                default:
                    throw new RuntimeException("Unexpected ViewHolder type: " + viewType);
            }
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof CommentHolder) {
                AbsRedditComment comment = mCommentBank.getVisibleComment(position - 1);
                ((CommentHolder) holder).bindComment(comment);
            } else if (holder instanceof LinkHolder) {
                ((LinkHolder) holder).bindLink(mRedditLink);
            }
        }

        @Override
        public int getItemCount() {
            // Add 1 for each header and footer view
            return mCommentBank.getNumVisible() + 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0)
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private class LinkHolder extends RecyclerView.ViewHolder {
            private View mLinkView;
            private TextView mLinkTitle, mLinkDomain, mLinkScore, mLinkAuthor, mLinkTimestamp,
                    mLinkSubreddit, mLinkComments, mSelfText;
            private ImageView mLinkThumbnail;

            public LinkHolder(View v) {
                super(v);
                mLinkView = v.findViewById(R.id.link_view);
                mLinkTitle = (TextView) v.findViewById(R.id.link_title);
                mLinkDomain = (TextView) v.findViewById(R.id.link_domain);
                mLinkScore = (TextView) v.findViewById(R.id.link_score);
                mLinkAuthor = (TextView) v.findViewById(R.id.link_author);
                mLinkTimestamp = (TextView) v.findViewById(R.id.link_timestamp);
                mLinkSubreddit = (TextView) v.findViewById(R.id.link_subreddit);
                mLinkComments = (TextView) v.findViewById(R.id.link_comment_count);
                mLinkThumbnail = (ImageView) v.findViewById(R.id.link_thumbnail);
                mSelfText = (TextView) v.findViewById(R.id.link_self_text);
            }

            public void bindLink(RedditLink link) {
                if (link == null) {
                    mLinkView.setVisibility(View.GONE);
                    mSelfText.setVisibility(View.GONE);
                    return;
                }

                mLinkView.setVisibility(View.VISIBLE);
                if (link.getSelftext() != null && !link.getSelftext().equals("")) {
                    mSelfText.setText(link.getSelftext());
                    mSelfText.setVisibility(View.VISIBLE);
                } else {
                    mSelfText.setVisibility(View.GONE);
                }

                String createDateFormatted = BaseUtils.getFormattedDateStringFromUtc(link.getCreatedUtc().longValue());

                // Set content for each TextView
                mLinkScore.setText(String.valueOf(link.getScore()) + " points");
                mLinkTitle.setText(link.getTitle());
                mLinkAuthor.setText("/u/" + link.getAuthor());
                mLinkTimestamp.setText(createDateFormatted);
                mLinkSubreddit.setText("/r/" + link.getSubreddit());
                mLinkDomain.setText("(" + link.getDomain() + ")");
                mLinkComments.setText(link.getNumComments() + " comments");

                // Queue thumbnail to be downloaded, if one exists
                mLinkThumbnail.setImageResource(R.drawable.ic_thumbnail_placeholder);

                String thumbnailUrl = link.getThumbnail();
                if (thumbnailUrl.equals("nsfw")) {
                    mLinkThumbnail.setImageResource(R.drawable.ic_nsfw);
                } else if (!thumbnailUrl.equals("")
                        && !thumbnailUrl.equals("default")
                        && !thumbnailUrl.equals("self")) {
                    Bitmap thumbnail = mThumbnailCache.getThumbnail(thumbnailUrl);
                    if (thumbnail == null) {
                        mThumbnailThread.queueThumbnail(mLinkThumbnail, thumbnailUrl);
                    } else {
                        mLinkThumbnail.setImageBitmap(thumbnail);
                    }
                    mLinkThumbnail.setVisibility(View.VISIBLE);
                } else {
                    mLinkThumbnail.setVisibility(View.GONE);
                }

                // Set background tint based on isLiked
                if (link.isLiked() == null) {
                    mLinkView.setBackgroundResource(R.drawable.link_card_background);
                } else if (link.isLiked()) {
                    mLinkView.setBackgroundResource(R.drawable.link_card_background_upvoted);
                } else {
                    mLinkView.setBackgroundResource(R.drawable.link_card_background_downvoted);
                }
            }
        }

        private class CommentHolder extends RecyclerView.ViewHolder {
            private View mView;
            private View mIndentation;
            private ViewGroup mExpanderView;
            private ImageView mExpanderIcon;
            private TextView mAuthorView;
            private TextView mScoreView;
            private TextView mTimestampView;
            private TextView mMoreCommentsView;
            private TextView mBodyView;

            public CommentHolder(View view) {
                super(view);
                mView = view;
                mIndentation = view.findViewById(R.id.indentation);
                mExpanderView = (ViewGroup) view.findViewById(R.id.comment_expander_view);
                mExpanderIcon = (ImageView) view.findViewById(R.id.comment_expander_icon);
                mAuthorView = (TextView) view.findViewById(R.id.comment_author);
                mScoreView = (TextView) view.findViewById(R.id.comment_score);
                mTimestampView = (TextView) view.findViewById(R.id.comment_timestamp);
                mMoreCommentsView = (TextView) view.findViewById(R.id.comment_more);
                mBodyView = (TextView) view.findViewById(R.id.comment_body);
            }

            public void bindComment(final AbsRedditComment comment) {
                // Add padding views to indentation_wrapper based on depth of comment
                int viewMargin = (comment.getDepth() - 2) * (int) getResources().getDimension(R.dimen.comment_indentation_margin);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                params.setMargins(viewMargin, 0, 0, 0);
                mView.setLayoutParams(params);

                if (comment.getDepth() == 0) {
                    mIndentation.setVisibility(View.GONE);
                } else {
                    mIndentation.setVisibility(View.VISIBLE);
                }

                // Populate attributes of comment in layout
                if (comment instanceof RedditComment) {
                    mAuthorView.setVisibility(View.VISIBLE);
                    // Set background to blue if author is OP
                    if (((RedditComment) comment).getAuthor().equals(mRedditLink.getAuthor())) {
                        mAuthorView.setBackgroundResource(R.drawable.original_poster_background);
                        mAuthorView.setTextColor(getResources().getColor(R.color.op_text));
                    } else { // Else, set it to transparent
                        mAuthorView.setBackgroundDrawable(null);
                        mAuthorView.setTextColor(getResources().getColor(R.color.secondary_text));
                    }
                    mScoreView.setVisibility(View.VISIBLE);
                    mTimestampView.setVisibility(View.VISIBLE);
                    mBodyView.setVisibility(View.VISIBLE);
                    mMoreCommentsView.setVisibility(View.GONE);
                    mAuthorView.setText(((RedditComment) comment).getAuthor());
                    mScoreView.setText("• " + ((RedditComment) comment).getScore() + " •");
                    mTimestampView.setText(BaseUtils.getFormattedDateStringFromUtc(((RedditComment) comment).getCreateUtc().longValue()));
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
                    mMoreCommentsView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getMoreChildren((RedditMoreComments) comment);
                        }
                    });
                }

                if (comment instanceof RedditComment) {
                    mExpanderView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mCommentBank.toggleThreadVisible(comment);
                            mCommentAdapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        }
    }
}
