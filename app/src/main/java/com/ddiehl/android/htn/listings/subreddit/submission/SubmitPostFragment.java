package com.ddiehl.android.htn.listings.subreddit.submission;

import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;

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
    @BindView(R.id.submission_type_tabs)
    TabLayout mTabLayout;
    @BindView(R.id.submission_url)
    EditText mUrlEditText;
    @BindView(R.id.submission_image)
    ImageView mImageView;
    @BindView(R.id.submission_title)
    EditText mTitleEditText;
    @BindView(R.id.submission_text)
    EditText mTextEditText;
    @BindView(R.id.send_replies_to_inbox)
    CheckBox mSendRepliesToInboxCheckbox;
    @BindView(R.id.submission_submit)
    Button mSubmitButton;

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
