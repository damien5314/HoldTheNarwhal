package com.ddiehl.android.htn.listings.subreddit.submission;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;

import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.ddiehl.android.htn.utils.AndroidUtils.getTextInputLayout;

@FragmentWithArgs
public class SubmitPostFragment extends BaseFragment
        implements SubmitPostView {

    public static final String TAG = SubmitPostFragment.class.getSimpleName();

    @BindView(R.id.coordinator_layout)
    CoordinatorLayout mCoordinatorLayout;
    @BindView(R.id.submission_type_tabs)
    TabLayout mSubmissionTypeTabs;
    @BindView(R.id.submission_url)
    TextInputEditText mUrlEditText;
    @BindView(R.id.submission_title)
    TextInputEditText mTitleEditText;
    @BindView(R.id.submission_text)
    TextInputEditText mTextEditText;
    @BindView(R.id.send_replies_to_inbox)
    CheckBox mSendRepliesToInboxCheckbox;
    @BindView(R.id.submission_submit)
    Button mSubmitButton;

    SubmitPostPresenter mPresenter;

    @Override
    protected int getLayoutResId() {
        return R.layout.submission_fragment;
    }

    @Override
    protected View getChromeView() {
        return mCoordinatorLayout;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HoldTheNarwhal.getApplicationComponent().inject(this);
        FragmentArgs.inject(this);
    }

    @NonNull
    @Override
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
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if ("link".equals(tag)) {
                    onLinkTabSelected();
                } else if ("text".equals(tag)) {
                    onTextTabSelected();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { /* no-op */ }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { /* no-op */ }
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
        getTextInputLayout(mUrlEditText).setVisibility(View.VISIBLE);
        getTextInputLayout(mTextEditText).setVisibility(View.GONE);
    }

    private void onTextTabSelected() {
        getTextInputLayout(mUrlEditText).setVisibility(View.GONE);
        getTextInputLayout(mTextEditText).setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.submission_submit)
    void onSubmitClicked() {
        mPresenter.submit();
    }

    @Override
    public void dismissAfterConfirmation() {
        getActivity().setResult(Activity.RESULT_OK);
        // TODO
        // Set model data as result so the subreddit Activity
        // can trigger navigation to the post just submitted
        getActivity().finish();
    }

    @Override
    public void dismissAfterCancel() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }
}
