package com.ddiehl.android.htn.listings.subreddit.submission;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import butterknife.BindView;

@FragmentWithArgs
public class SubmitPostFragment extends BaseFragment {

    public static final String TAG = SubmitPostFragment.class.getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;

    @Override protected int getLayoutResId() {
        return R.layout.submission_fragment;
    }

    @Override protected View getChromeView() {
        return mCoordinatorLayout;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
    }
}
