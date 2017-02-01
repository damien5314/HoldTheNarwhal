package com.ddiehl.android.htn.listings.subreddit.submission;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.ddiehl.android.htn.HoldTheNarwhal;
import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseFragment;
import com.hannesdorfmann.fragmentargs.FragmentArgs;
import com.hannesdorfmann.fragmentargs.annotation.Arg;
import com.hannesdorfmann.fragmentargs.annotation.FragmentWithArgs;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rxreddit.model.SubmitPostResponse;

import static com.ddiehl.android.htn.utils.AndroidUtils.getTextInputLayout;

@FragmentWithArgs
public class SubmitPostFragment extends BaseFragment
        implements SubmitPostView {

    public static final String TAG = SubmitPostFragment.class.getSimpleName();

    public static final String EXTRA_SUBMIT_SUBREDDIT = "EXTRA_SUBMIT_SUBREDDIT";
    public static final String EXTRA_SUBMIT_ID = "EXTRA_SUBMIT_ID";
    public static final String EXTRA_SUBMIT_ERRORS = "EXTRA_SUBMIT_ERRORS";
    public static final int RESULT_SUBMIT_SUCCESS = Activity.RESULT_OK;
    public static final int RESULT_SUBMIT_ERROR = -10;

    @Arg String mSubreddit;

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
        mPresenter = new SubmitPostPresenter(this);
    }

    @NonNull
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle state) {
        View view = super.onCreateView(inflater, container, state);
        ButterKnife.bind(this, view);

        mSubmissionTypeTabs.addOnTabSelectedListener(getOnTabSelectedListener());
        TabLayout.Tab linkTab = addNewTab(mSubmissionTypeTabs, R.string.submission_type_link, "link");
        TabLayout.Tab textTab = addNewTab(mSubmissionTypeTabs, R.string.submission_type_text, "self");

        return view;
    }

    TabLayout.OnTabSelectedListener getOnTabSelectedListener() {
        return new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if ("link".equals(tag)) {
                    onLinkTabSelected();
                } else if ("self".equals(tag)) {
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
        int position = mSubmissionTypeTabs.getSelectedTabPosition();
        TabLayout.Tab selected = mSubmissionTypeTabs.getTabAt(position);

        if (selected == null) {
            throw new RuntimeException("Invalid selected tab position");
        }
        if ("link".equals(selected.getTag())) {
            mPresenter.submit("link");
        } else {
            mPresenter.submit("self");
        }
    }

    @Override
    public String getSubreddit() {
        return mSubreddit;
    }

    @Override
    public String getTitle() {
        return mTitleEditText.getText().toString();
    }

    @Override
    public String getUrl() {
        return mUrlEditText.getText().toString();
    }

    @Override
    public String getText() {
        return mTextEditText.getText().toString();
    }

    @Override
    public boolean getSendReplies() {
        return mSendRepliesToInboxCheckbox.isChecked();
    }

    @Override
    public void onPostSubmitted(SubmitPostResponse result) {
        Intent intent = new Intent();
        // Success
        if (result.getErrors().size() == 0) {
            intent.putExtra(EXTRA_SUBMIT_SUBREDDIT, mSubreddit);
            intent.putExtra(EXTRA_SUBMIT_ID, result.getId());
            finish(RESULT_SUBMIT_SUCCESS, intent);
        }
        // Errors present
        else {
            for (String error : result.getErrors()) {
                Crashlytics.logException(new RuntimeException(error));
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onSubmitError(Throwable error) {
        Snackbar.make(getChromeView(), R.string.submission_submit_error, Snackbar.LENGTH_LONG);
    }

    @Override
    public void dismissAfterCancel() {
        getActivity().setResult(Activity.RESULT_CANCELED);
        getActivity().finish();
    }
}
