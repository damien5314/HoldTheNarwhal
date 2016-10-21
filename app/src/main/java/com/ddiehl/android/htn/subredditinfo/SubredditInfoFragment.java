package com.ddiehl.android.htn.subredditinfo;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.fragments.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import butterknife.BindView;
import rx.functions.Action1;
import rxreddit.model.Subreddit;

@FragmentWithArgs
public class SubredditInfoFragment extends BaseFragment {

    public static final String TAG = SubredditInfoFragment.class.getSimpleName();
    public static final int RESULT_GET_INFO_ERROR = -1000;

    @BindView(R.id.coordinator_layout) CoordinatorLayout mCoordinatorLayout;

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

    void showSubredditInfo(Subreddit subreddit) {
        // TODO
    }
}
