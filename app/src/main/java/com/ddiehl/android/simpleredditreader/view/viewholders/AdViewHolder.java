package com.ddiehl.android.simpleredditreader.view.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ddiehl.android.simpleredditreader.R;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AdViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;

//    @InjectView(R.id.listings_banner_ad) AdView mAdView;
    @InjectView(R.id.ad_icon) ImageView mIconView;
    @InjectView(R.id.ad_title) TextView mTitleView;
    @InjectView(R.id.ad_text) TextView mTextView;
    @InjectView(R.id.ad_cta) TextView mCtaView;

    public AdViewHolder(View v) {
        super(v);
        mContext = v.getContext();
        ButterKnife.inject(this, v);
    }

    public void showAd() {
//        AdRequest req = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
//                .addTestDevice("3F86D6D7140CD05AF144D7983696327F") // Nexus 5 physical
//                .addTestDevice("6F5873E9AD39AE3A3A20C2E7E5DD2B76") // Nexus 4 emulator - MacBook
//                .build();
//        mAdView.loadAd(req);


    }

}
