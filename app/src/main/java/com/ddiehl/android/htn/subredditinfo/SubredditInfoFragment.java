package com.ddiehl.android.htn.subredditinfo;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rxreddit.model.Subreddit;
import rxreddit.model.SubredditRules;
import timber.log.Timber;

@FragmentWithArgs
public class SubredditInfoFragment extends BaseFragment {

    public static final String TAG = SubredditInfoFragment.class.getSimpleName();
    public static final int RESULT_GET_INFO_ERROR = -1000;

    @Inject Bypass mBypass;

    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.subreddit_info_parent) View mParentViewGroup;
    @BindView(R.id.subreddit_name) TextView mSubredditName;
    @BindView(R.id.create_date) TextView mCreateDate;
    @BindView(R.id.subscriber_count) TextView mSubscriberCount;
    @BindView(R.id.nsfw_icon) TextView mNsfwIcon;
    @BindView(R.id.public_description) TextView mPublicDescription;
    @BindView(R.id.rules_layout) LinearLayout mRulesLayout;

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
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
        setTitle("");
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set name field
        String name = String.format("/r/%s", mSubreddit);
        mSubredditName.setText(name);

        // Load info
        mParentViewGroup.setVisibility(View.GONE);
        loadSubredditInfo();
    }

    void loadSubredditInfo() {
        Observable.combineLatest(
                mRedditService.getSubredditInfo(mSubreddit),
                mRedditService.getSubredditRules(mSubreddit),
//                mRedditService.getSubredditSidebar(mSubreddit),
                (subreddit, rules) -> {
                    InfoTuple tuple = new InfoTuple();
                    tuple.subreddit = subreddit;
                    tuple.rules = rules;
                    return tuple;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(this::showSpinner)
                .doOnUnsubscribe(this::dismissSpinner)
                .subscribe(onSubredditInfoLoaded(), onSubredditInfoLoadError());
    }

    Action1<Throwable> onSubredditInfoLoadError() {
        return error -> {
            Timber.e("Error loading subreddit info", error);

            // Pass error back to target fragment or Activity
            if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_GET_INFO_ERROR, null);
            } else {
                getActivity().setResult(RESULT_GET_INFO_ERROR);
                finish();
            }
        };
    }

    Action1<InfoTuple> onSubredditInfoLoaded() {
        return tuple -> {
            mParentViewGroup.setVisibility(View.VISIBLE);

            showSubredditInfo(tuple.subreddit);
            showSubredditRules(tuple.rules);
        };
    }

    void showSubredditInfo(final @NonNull Subreddit subreddit) {
        // Format and show date created
        Long created = subreddit.getCreatedUtc();
        String createdText = formatDate(created);
        mCreateDate.setText(createdText);

        // Format and show subscriber count
        Integer subscribers = subreddit.getSubscribers();
        String subscriberText = NumberFormat.getInstance().format(subscribers);
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

    void showSubredditRules(SubredditRules rules) {
        for (SubredditRules.Rule rule : rules.getRules()) {
            addToRules(rule);
        }
    }

    void addToRules(SubredditRules.Rule rule) {
        // Inflate layout and bind views
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.subreddit_rule, mRulesLayout, false);
        TextView shortNameView = ButterKnife.findById(view, R.id.short_name);
        TextView categoryView = ButterKnife.findById(view, R.id.category);
        TextView descriptionView = ButterKnife.findById(view, R.id.description);

        // Bind data
        String shortName = rule.getShortName();
        shortNameView.setText(shortName);

        String category = rule.getKind();
        categoryView.setText(getTextForCategory(category));

        String description = rule.getDescription().trim();
        CharSequence descriptionText = mBypass.markdownToSpannable(description);
        descriptionView.setText(descriptionText);

        mRulesLayout.addView(view);
    }

    String getTextForCategory(String category) {
        switch (category) {
            case "link":
                return getString(R.string.subreddit_category_link);
            case "comment":
                return getString(R.string.subreddit_category_comment);
            case "all": default:
                return getString(R.string.subreddit_category_all);
        }
    }

    static class InfoTuple {
        Subreddit subreddit;
        SubredditRules rules;
    }
}
