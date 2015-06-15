package com.ddiehl.android.simpleredditreader.view.viewholders;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.ddiehl.android.simpleredditreader.R;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class AdViewHolder extends RecyclerView.ViewHolder {

    private Context mContext;

    @InjectView(R.id.listings_banner_ad) AdView mAdView;

    public AdViewHolder(View v) {
        super(v);
        mContext = v.getContext();
        ButterKnife.inject(this, v);
    }

    public void loadAd() {
        AdRequest req = new AdRequest.Builder()
                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("3F86D6D7140CD05AF144D7983696327F")
                .build();
        mAdView.loadAd(req);
    }

}
