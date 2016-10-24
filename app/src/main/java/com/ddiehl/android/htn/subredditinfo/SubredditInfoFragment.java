package com.ddiehl.android.htn.subredditinfo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.widget.TextView;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import rx.functions.Action1;
import rxreddit.model.Subreddit;

@FragmentWithArgs
public class SubredditInfoFragment extends BaseFragment {

    public static final String TAG = SubredditInfoFragment.class.getSimpleName();
    public static final int RESULT_GET_INFO_ERROR = -1000;

    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.create_date) TextView mCreateDate;
    @BindView(R.id.subscriber_count) TextView mSubscriberCount;
    @BindView(R.id.nsfw_icon) TextView mNsfwIcon;
    @BindView(R.id.public_description) TextView mPublicDescription;

    @Arg String mSubreddit;

    @Override
    protected int getLayoutResId() {
        return R.layout.subreddit_info_fragment;
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentArgs.inject(this);

        String title = String.format("/r/%s", mSubreddit);
        setTitle(title);
    }

    @Override
    public void onStart() {
        super.onStart();
        loadSubredditInfo();
    }

    void loadSubredditInfo() {
        mRedditService.getSubredditInfo(mSubreddit)
                .subscribe(onSubredditInfoLoaded(), onSubredditInfoLoadError());
    }

    Action1<Throwable> onSubredditInfoLoadError() {
        return error -> {
            // Pass error back to target fragment or Activity
            if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_GET_INFO_ERROR, null);
            } else {
                getActivity().setResult(RESULT_GET_INFO_ERROR);
                finish();
            }
        };
    }

    Action1<Subreddit> onSubredditInfoLoaded() {
        return this::showSubredditInfo;
    }

    void showSubredditInfo(final @NonNull Subreddit subreddit) {
        // Format and show date created
        String created = formatDate(subreddit.getCreatedUtc());
        String createdText = getString(R.string.created_format, created);
        mCreateDate.setText(createdText);

        // Format and show subscriber count
        String subscribers = NumberFormat.getInstance().format(subreddit.getSubscribers());
        String subscriberText = getContext().getResources().getQuantityString(
                R.plurals.num_subscribers, subreddit.getSubscribers(), subscribers
        );
        mSubscriberCount.setText(subscriberText);

        // Show/Hide NSFW icon
        boolean showNsfw = subreddit.isOver18();
        mNsfwIcon.setVisibility(showNsfw ? View.VISIBLE : View.GONE);

        // Show public description text
        String publicDescription = subreddit.getPublicDescription();
        mPublicDescription.setText(publicDescription);
    }

    String formatDate(final long createdUtc) {
        Date date = new Date(createdUtc * 1000);
        DateFormat format = SimpleDateFormat.getDateInstance();
        return format.format(date);
    }
}
