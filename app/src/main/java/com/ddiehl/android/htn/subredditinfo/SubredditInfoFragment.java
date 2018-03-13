package com.ddiehl.android.htn.subredditinfo;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerPresenter;
import com.ddiehl.android.htn.view.BaseFragment;
import com.ddiehl.android.htn.view.markdown.HtmlParser;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import rxreddit.model.Subreddit;
import rxreddit.model.SubredditRule;
import rxreddit.model.SubredditRules;
import timber.log.Timber;

@FragmentWithArgs
public class SubredditInfoFragment extends BaseFragment {

    public static final String TAG = SubredditInfoFragment.class.getSimpleName();
    public static final int RESULT_GET_INFO_ERROR = -1000;

    @Inject HtmlParser htmlParser;

    @BindView(R.id.coordinator_layout) CoordinatorLayout coordinatorLayout;
    @BindView(R.id.subreddit_info_parent) View parentViewGroup;
    @BindView(R.id.subreddit_name) TextView subredditName;
    @BindView(R.id.subscribe_button) ViewGroup subscribeButtonLayout;
    @BindView(R.id.subscribe_button_text) TextView subscribeButtonText;
    @BindView(R.id.subscribe_button_icon) ImageView subscribeButtonIcon;
    @BindView(R.id.subscribe_button_progress) ProgressBar subscribeButtonProgressBar;
    @BindView(R.id.create_date) TextView createDate;
    @BindView(R.id.subscriber_count) TextView subscriberCount;
    @BindView(R.id.public_description) TextView publicDescription;
    @BindView(R.id.rules_layout) LinearLayout rulesLayout;

    @Arg String subreddit;

    SubscriptionManagerPresenter subscriptionManagerPresenter;
    InfoTuple subredditInfo;
    final CompositeDisposable compositeDisposable = new CompositeDisposable();

    @Override
    protected int getLayoutResId() {
        return R.layout.subreddit_info_fragment;
    }

    @NotNull @Override
    protected View getChromeView() {
        return coordinatorLayout;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Timber.i("Showing subreddit info: %s", subreddit);

        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);

        subscriptionManagerPresenter = new SubscriptionManagerPresenter();

        setTitle("");
    }

    @Override
    public void onStart() {
        super.onStart();

        // Set name field
        String name = String.format("/r/%s", subreddit);
        subredditName.setText(name);

        // Load info
        parentViewGroup.setVisibility(View.GONE);
        loadSubredditInfo();
    }

    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    void loadSubredditInfo() {
        if (subredditInfo == null) {
            subscriptionManagerPresenter.getSubredditInfo(subreddit)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(disposable -> showSpinner())
                    .doFinally(this::dismissSpinner)
                    .subscribe(this::onSubredditInfoLoaded, this::onSubredditInfoLoadError);
        } else {
            onSubredditInfoLoaded(subredditInfo);
        }
    }

    void onSubredditInfoLoadError(Throwable error) {
        if (error instanceof IOException) {
            String message = getString(R.string.error_network_unavailable);
            showError(message);
        } else {
            Timber.e(error, "Error loading subreddit info");
        }

        // Pass error back to target fragment or Activity
        if (getTargetFragment() != null) {
            getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_GET_INFO_ERROR, null);
        } else {
            getActivity().setResult(RESULT_GET_INFO_ERROR);
            finish();
        }
    }

    void onSubredditInfoLoaded(InfoTuple tuple) {
        subredditInfo = tuple;
        parentViewGroup.setVisibility(View.VISIBLE);

        bindSubscribeButton(tuple.subreddit);
        showSubredditInfo(tuple.subreddit);
        showSubredditRules(tuple.rules);
    }

    void bindSubscribeButton(final @NotNull Subreddit subreddit) {
        Boolean subscribed = subreddit.getUserIsSubscriber();

        subscribeButtonProgressBar.setVisibility(View.GONE);
        subscribeButtonLayout.setEnabled(true);

        if (subscribed == null || !subscribed) {
            showSubscribeButton(subreddit);
        } else {
            showUnsubscribeButton(subreddit);
        }
    }

    void showSubscribeButton(@NotNull Subreddit subreddit) {
        subscribeButtonText.setText(R.string.subscribe);

        // Removed check icon
        subscribeButtonIcon.setVisibility(View.GONE);

        // Set onClick behavior
        subscribeButtonLayout.setOnClickListener((view) -> {
            // Remove onClick behavior
            subscribeButtonLayout.setOnClickListener(null);

            // Hide icon, show progress bar, and set layout to disabled state
            subscribeButtonIcon.setVisibility(View.GONE);
            subscribeButtonProgressBar.setVisibility(View.VISIBLE);
            subscribeButtonLayout.setEnabled(false);

            // Subscribe to subreddit
            subscriptionManagerPresenter.subscribe(subreddit)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(compositeDisposable::add)
                    .subscribe(
                            () -> onSubredditSubscribed(subreddit),
                            this::onSubredditSubscribeError
                    );
        });
    }

    void showUnsubscribeButton(@NotNull Subreddit subreddit) {
        subscribeButtonText.setText(R.string.subscribed);

        // Add check icon
        subscribeButtonIcon.setVisibility(View.VISIBLE);

        // Set onClick behavior
        subscribeButtonLayout.setOnClickListener((view) -> {
            // Remove onClick behavior
            subscribeButtonLayout.setOnClickListener(null);

            // Hide icon, show progress bar, and set layout to disabled state
            subscribeButtonIcon.setVisibility(View.GONE);
            subscribeButtonProgressBar.setVisibility(View.VISIBLE);
            subscribeButtonLayout.setEnabled(false);

            // Unsubscribe from subreddit
            subscriptionManagerPresenter.unsubscribe(subreddit)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnSubscribe(compositeDisposable::add)
                    .subscribe(
                            () -> onSubredditUnsubscribed(subreddit),
                            this::onSubredditUnsubscribeError
                    );
        });
    }

    void onSubredditSubscribed(@NotNull Subreddit subreddit) {
        subreddit.setUserIsSubscriber(true);
        bindSubscribeButton(subreddit);
    }

    void onSubredditSubscribeError(Throwable error) {
        if (error instanceof IOException) {
            String message = getString(R.string.error_network_unavailable);
            showError(message);
        } else {
            Timber.w(error, "Error subscribing to /r/%s", subreddit);
            String message = getString(R.string.subscribe_error, subreddit);
            showError(message);
        }
    }

    void onSubredditUnsubscribed(@NotNull Subreddit subreddit) {
        subreddit.setUserIsSubscriber(false);
        bindSubscribeButton(subreddit);
    }

    void onSubredditUnsubscribeError(Throwable error) {
        if (error instanceof IOException) {
            String message = getString(R.string.error_network_unavailable);
            showError(message);
        } else {
            Timber.w(error, "Error unsubscribing from /r/%s", subreddit);
            String message = getString(R.string.unsubscribe_error, subreddit);
            showError(message);
        }
    }

    void showSubredditInfo(final @NotNull Subreddit subreddit) {
        // Format and show date created
        Long created = subreddit.getCreatedUtc();
        String createdText = formatDate(created);
        createDate.setText(createdText);

        // Format and show subscriber count
        Integer subscribers = subreddit.getSubscribers();
        String subscriberText = NumberFormat.getInstance().format(subscribers);
        subscriberCount.setText(subscriberText);

        // Show/Hide NSFW text
        if (subreddit.isOver18()) {
            SpannableStringBuilder ssb = new SpannableStringBuilder();

            // Cache subreddit name text
            CharSequence nameText = subredditName.getText();
            ssb.append(nameText);

            // Append nsfw label
            String nsfwText = getString(R.string.nsfw);
            ssb.append("   ").append(nsfwText);

            // Apply smaller size span
            ssb.setSpan(
                    new RelativeSizeSpan(0.5f),
                    nameText.length(),
                    ssb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            // Apply color span
            int nsfwTagColor = ContextCompat.getColor(getContext(), R.color.nsfw_tag_color);
            ForegroundColorSpan colorSpan = new ForegroundColorSpan(nsfwTagColor);
            ssb.setSpan(
                    colorSpan,
                    nameText.length(),
                    ssb.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            subredditName.setText(ssb);
        }


        // Show public description text
        final String publicDescription = subreddit.getPublicDescriptionHtml();
        final Spanned parsedDescription = htmlParser.convert(publicDescription);
        this.publicDescription.setText(parsedDescription);
    }

    String formatDate(final long createdUtc) {
        Date date = new Date(createdUtc * 1000);
        DateFormat format = SimpleDateFormat.getDateInstance();
        return format.format(date);
    }

    void showSubredditRules(SubredditRules rules) {
        for (SubredditRule rule : rules.getRules()) {
            addToRules(rule);
        }
    }

    void addToRules(SubredditRule rule) {
        // Inflate layout and bind views
        View view = LayoutInflater.from(getContext())
                .inflate(R.layout.subreddit_rule, rulesLayout, false);
        TextView shortNameView = ButterKnife.findById(view, R.id.short_name);
        TextView categoryView = ButterKnife.findById(view, R.id.category);
        TextView descriptionView = ButterKnife.findById(view, R.id.description);

        // Bind data
        String shortName = rule.getShortName();
        shortNameView.setText(shortName);

        String category = rule.getKind();
        categoryView.setText(getTextForCategory(category));

        final String descriptionHtml = rule.getDescriptionHtml();
        if (descriptionHtml != null) {
            String description = descriptionHtml.trim();
            final Spanned parsedDescription = htmlParser.convert(description);
            descriptionView.setText(parsedDescription);
        }

        rulesLayout.addView(view);
    }

    String getTextForCategory(String category) {
        switch (category) {
            case "link":
                return getString(R.string.subreddit_category_link);
            case "comment":
                return getString(R.string.subreddit_category_comment);
            case "all":
            default:
                return getString(R.string.subreddit_category_all);
        }
    }
}
