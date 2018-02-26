package com.ddiehl.android.htn.di;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.listings.BaseListingsFragment;
import com.ddiehl.android.htn.listings.BaseListingsPresenter;
import com.ddiehl.android.htn.listings.comments.LinkCommentsFragment;
import com.ddiehl.android.htn.listings.comments.ListingsCommentViewHolder;
import com.ddiehl.android.htn.listings.comments.ThreadCommentViewHolder;
import com.ddiehl.android.htn.listings.comments.ThreadStubViewHolder;
import com.ddiehl.android.htn.listings.inbox.ListingsMessageViewHolder;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageActivity;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageAdapter;
import com.ddiehl.android.htn.listings.inbox.PrivateMessageFragment;
import com.ddiehl.android.htn.listings.links.BaseLinkViewHolder;
import com.ddiehl.android.htn.listings.profile.UserProfileFragment;
import com.ddiehl.android.htn.listings.profile.UserProfilePresenter;
import com.ddiehl.android.htn.listings.report.ReportActivity;
import com.ddiehl.android.htn.listings.subreddit.SubredditFragment;
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostFragment;
import com.ddiehl.android.htn.listings.subreddit.submission.SubmitPostPresenter;
import com.ddiehl.android.htn.navigation.WebViewFragment;
import com.ddiehl.android.htn.notifications.NotificationCheckJobService;
import com.ddiehl.android.htn.settings.SettingsActivity;
import com.ddiehl.android.htn.settings.SettingsFragment;
import com.ddiehl.android.htn.settings.SettingsPresenter;
import com.ddiehl.android.htn.subredditinfo.SubredditInfoFragment;
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerAdapter;
import com.ddiehl.android.htn.subscriptions.SubscriptionManagerPresenter;
import com.ddiehl.android.htn.view.BaseActivity;
import com.ddiehl.android.htn.view.BaseFragment;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
        ApplicationModule.class,
})
public interface ApplicationComponent {

    // Activities
    void inject(BaseActivity activity);
    void inject(PrivateMessageActivity activity);
    void inject(SettingsActivity activity);
    void inject(ReportActivity activity);

    // Fragments
    void inject(BaseFragment fragment);
    void inject(BaseListingsFragment fragment);
    void inject(SubredditFragment fragment);
    void inject(UserProfileFragment fragment);
    void inject(PrivateMessageFragment fragment);
    void inject(SettingsFragment fragment);
    void inject(WebViewFragment fragment);
    void inject(LinkCommentsFragment fragment);
    void inject(SubredditInfoFragment fragment);
    void inject(SubmitPostFragment fragment);

    // ViewHolders
    void inject(ThreadCommentViewHolder vh);
    void inject(ThreadStubViewHolder vh);
    void inject(ListingsMessageViewHolder vh);
    void inject(ListingsCommentViewHolder vh);
    void inject(PrivateMessageAdapter.VH vh);
    void inject(SubscriptionManagerAdapter.VH vh);
    void inject(BaseLinkViewHolder vh);

    // Presenters
    void inject(BaseListingsPresenter presenter);
    void inject(UserProfilePresenter presenter);
    void inject(SettingsPresenter presenter);
    void inject(SubscriptionManagerPresenter presenter);
    void inject(SubmitPostPresenter presenter);

    // Services
    void inject(NotificationCheckJobService service);

    //TODO: Delete
    void inject(HoldTheNarwhal application);
}
