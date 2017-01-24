package com.ddiehl.android.htn.listings.subreddit.submission;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import butterknife.ButterKnife;
import timber.log.Timber;

@FragmentWithArgs
public class SubmitPostFragment extends BaseFragment {

    public static final String TAG = SubmitPostFragment.class.getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.submission_type_tabs)
    TabLayout mSubmissionTypeTabs;
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

    @NonNull @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        ButterKnife.bind(this, view);

        mSubmissionTypeTabs.addOnTabSelectedListener(getOnTabSelectedListener());
        TabLayout.Tab linkTab = addNewTab(mSubmissionTypeTabs, R.string.submission_type_link, "link");
        TabLayout.Tab textTab = addNewTab(mSubmissionTypeTabs, R.string.submission_type_text, "text");

        return view;
    }

    TabLayout.OnTabSelectedListener getOnTabSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override public void onTabSelected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if ("link".equals(tag)) {
                    onLinkTabSelected();
                } else if ("text".equals(tag)) {
                    onTextTabSelected();
                }
            }

            @Override public void onTabUnselected(TabLayout.Tab tab) { /* no-op */ }

            @Override public void onTabReselected(TabLayout.Tab tab) { /* no-op */ }
        };
    }

    TabLayout.Tab addNewTab(@NonNull TabLayout tabLayout, @StringRes int resId, @NonNull String tag) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(resId);
        tab.setTag(tag);
        tabLayout.addTab(tab);
        return tab;
    }

    private void onLinkTabSelected() {
        Timber.d("LINK tab selected");

        ((View) mUrlEditText.getParent()).setVisibility(View.VISIBLE);
        mImageView.setVisibility(View.VISIBLE);
        ((View) mTextEditText.getParent()).setVisibility(View.GONE);
    }

    private void onTextTabSelected() {
        Timber.d("TEXT tab selected");

        ((View) mUrlEditText.getParent()).setVisibility(View.GONE);
        mImageView.setVisibility(View.GONE);
        ((View) mTextEditText.getParent()).setVisibility(View.VISIBLE);
    }
}
