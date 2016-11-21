package com.ddiehl.android.htn.subscriptions;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import in.uncod.android.bypass.Bypass;
import rxreddit.model.Subreddit;

public class SubscriptionManagerAdapter extends RecyclerView.Adapter<SubscriptionManagerAdapter.VH>
        implements ItemTouchHelperAdapter {

    final SubscriptionManagerView mSubscriptionManagerView;
    final SubscriptionManagerPresenter mPresenter;
    final List<Subreddit> mData = new ArrayList<>();

    public SubscriptionManagerAdapter(
            @NonNull SubscriptionManagerView view, @NonNull SubscriptionManagerPresenter presenter) {
        mSubscriptionManagerView = view;
        mPresenter = presenter;
    }

    public void add(Subreddit subscription, Subreddit... others) {
        int previousSize = mData.size();
        mData.add(subscription);
        mData.addAll(Arrays.asList(others));
        notifyItemRangeInserted(previousSize, mData.size() - previousSize);
    }

    public void add(final int position, final Subreddit subreddit) {
        mData.add(position, subreddit);
        notifyItemInserted(position);
    }

    public void addAll(List<Subreddit> subreddits) {
        int previousSize = mData.size();
        mData.addAll(subreddits);
        notifyItemRangeInserted(previousSize, mData.size() - previousSize);
    }

    public void remove(Subreddit subreddit) {
        int previousIndex = mData.indexOf(subreddit);
        mData.remove(subreddit);
        notifyItemRemoved(previousIndex);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subscription_manager_item, parent, false);
        return new VH(itemView, mSubscriptionManagerView);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {
        Subreddit subreddit = mData.get(position);
        holder.bind(subreddit);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public boolean hasData() {
        return mData.size() > 0;
    }

    public void clearData() {
        int itemCount = mData.size();
        mData.clear();
        notifyItemRangeRemoved(0, itemCount);
    }

    @Override
    public void onItemMove(int fromPosition, int toPosition) {
        // Dragging is disabled for this view
    }

    @Override
    public void onItemDismiss(int position) {
        Subreddit subreddit = mData.get(position);
        mSubscriptionManagerView.onSubredditDismissed(subreddit, position);
    }

    public static class VH extends RecyclerView.ViewHolder {

        @Inject @Nullable Bypass mBypass;

        @BindView(R.id.name) TextView mName;
        @BindView(R.id.public_description) TextView mPublicDescription;
        @BindView(R.id.subscription_icon) ImageView mSubscriptionIcon;

        SubscriptionManagerView mSubscriptionManagerView;

        public VH(View itemView, SubscriptionManagerView subscriptionManagerView) {
            super(itemView);

            HoldTheNarwhal.getApplicationComponent().inject(this);
            ButterKnife.bind(this, itemView);

            mSubscriptionManagerView = subscriptionManagerView;
        }

        public void bind(Subreddit subreddit) {
            // Set name
            mName.setText(subreddit.getDisplayName());

            // Set public description
            String publicDescription = subreddit.getPublicDescription();
            if (mBypass != null) {
                mPublicDescription.setText(
                        mBypass.markdownToSpannable(publicDescription)
                );
            } else {
                mPublicDescription.setText(publicDescription);
            }

            // Set subreddit icon
            String iconUrl = subreddit.getIconImg();
            if (!TextUtils.isEmpty(iconUrl)) {
                Picasso.with(mSubscriptionIcon.getContext())
                        .load(iconUrl)
                        .resizeDimen(R.dimen.subscription_icon_width, R.dimen.subscription_icon_height)
                        .centerInside()
                        .into(mSubscriptionIcon);
            } else {
                mSubscriptionIcon.setImageDrawable(null);
            }

            // Set onClick listener
            itemView.setOnClickListener(
                    view -> mSubscriptionManagerView.onSubredditClicked(subreddit, getAdapterPosition())
            );
        }
    }
}
