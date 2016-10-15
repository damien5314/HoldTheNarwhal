package com.ddiehl.android.htn.subscriptions;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ddiehl.android.htn.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rxreddit.model.Subreddit;

public class SubscriptionManagerAdapter extends RecyclerView.Adapter<SubscriptionManagerAdapter.VH> {

    private final List<Subreddit> mData = new ArrayList<>();

    public void add(Subreddit subscription, Subreddit... others) {
        int previousSize = mData.size();
        mData.add(subscription);
        mData.addAll(Arrays.asList(others));
        notifyItemRangeInserted(previousSize, mData.size() - previousSize);
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
        return new VH(itemView);
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

    static class VH extends RecyclerView.ViewHolder {

        @BindView(R.id.name) TextView mName;
        @BindView(R.id.num_subscribers) TextView mNumSubscribers;

        public VH(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bind(Subreddit subreddit) {
            // Set name
            mName.setText(subreddit.getDisplayName());

            // Set subscriber count
            Integer subscribers = subreddit.getSubscribers();
            String subscribersText = itemView.getContext().getResources()
                    .getQuantityString(R.plurals.num_subscribers, subscribers, NumberFormat.getInstance().format(subscribers));
            mNumSubscribers.setText(subscribersText);
        }
    }
}
