package com.ddiehl.android.htn.subscriptions;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.glide.GlideApp;
import com.ddiehl.android.htn.view.markdown.HtmlParser;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import rxreddit.model.Subreddit;

public class SubscriptionManagerAdapter extends RecyclerView.Adapter<SubscriptionManagerAdapter.VH>
        implements ItemTouchHelperAdapter {

    final SubscriptionManagerView subscriptionManagerView;
    final SubscriptionManagerPresenter presenter;
    final List<Subreddit> data = new ArrayList<>();

    public SubscriptionManagerAdapter(
            @NotNull SubscriptionManagerView view, @NotNull SubscriptionManagerPresenter presenter) {
        this.subscriptionManagerView = view;
        this.presenter = presenter;
    }

    public void add(Subreddit subscription, Subreddit... others) {
        int previousSize = data.size();
        data.add(subscription);
        data.addAll(Arrays.asList(others));
        notifyItemRangeInserted(previousSize, data.size() - previousSize);
    }

    public void add(final int position, final Subreddit subreddit) {
        data.add(position, subreddit);
        notifyItemInserted(position);
    }

    public void addAll(List<Subreddit> subreddits) {
        int previousSize = data.size();
        data.addAll(subreddits);
        notifyItemRangeInserted(previousSize, data.size() - previousSize);
    }

    public void remove(Subreddit subreddit) {
        int previousIndex = data.indexOf(subreddit);
        data.remove(subreddit);
        notifyItemRemoved(previousIndex);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscription_manager_item, parent, false);
        return new VH(itemView, subscriptionManagerView);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Subreddit subreddit = data.get(position);
        holder.bind(subreddit);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public boolean hasData() {
        return data.size() > 0;
    }

    public void clearData() {
        int itemCount = data.size();
        data.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Dragging is disabled for this view
    }

    @Override
    public void onItemDismiss(int position) {
        Subreddit subreddit = data.get(position);
        subscriptionManagerView.onSubredditDismissed(subreddit, position);
    }

    public static class VH extends RecyclerView.ViewHolder {

        @Inject HtmlParser htmlParser;

        @BindView(R.id.name) TextView name;
        @BindView(R.id.public_description) TextView publicDescription;
        @BindView(R.id.subscription_icon) ImageView subscriptionIcon;

        SubscriptionManagerView subscriptionManagerView;

        public VH(View itemView, SubscriptionManagerView subscriptionManagerView) {
            super(itemView);

            HoldTheNarwhal.getApplicationComponent().inject(this);
            ButterKnife.bind(this, itemView);

            this.subscriptionManagerView = subscriptionManagerView;
        }

        public void bind(Subreddit subreddit) {
            // Set name
            name.setText(subreddit.getDisplayName());

            // Set public description
            String publicDescription = subreddit.getPublicDescriptionHtml();
            if (publicDescription != null) {
                final Spanned parsedDescription = htmlParser.convert(publicDescription);
                this.publicDescription.setText(parsedDescription);
            } else {
                this.publicDescription.setText(null);
            }

            // Set subreddit icon
            String iconUrl = subreddit.getIconImg();
            if (!TextUtils.isEmpty(iconUrl)) {
                Context context = subscriptionIcon.getContext();
                GlideApp.with(context)
                        .load(iconUrl)
                        .fitCenter()
                        .into(subscriptionIcon);
            } else {
                subscriptionIcon.setImageDrawable(null);
            }

            // Set onClick listener
            itemView.setOnClickListener(
                    view -> subscriptionManagerView.onSubredditClicked(subreddit, getAdapterPosition())
            );
        }
    }
}
