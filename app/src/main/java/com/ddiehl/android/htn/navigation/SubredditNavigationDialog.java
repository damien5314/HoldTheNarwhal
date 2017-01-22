package com.ddiehl.android.htn.navigation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.Window;

import com.ddiehl.android.htn.R;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

public class SubredditNavigationDialog extends DialogFragment {

    public static final String TAG = SubredditNavigationDialog.class.getSimpleName();

    public interface Callbacks {
        void onSubredditNavigationConfirmed(String subreddit);
        void onSubredditNavigationCancelled();
    }

    @BindView(R.id.drawer_navigate_to_subreddit_text)
    SubredditEditText mEditText;

    @BindView(R.id.drawer_navigate_to_subreddit_go)
    View mSubmitButton;

    private Callbacks mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement AnalyticsDialog.Callbacks");
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("Showing subreddit navigation dialog");

        Dialog dialog = new Dialog(getActivity());

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.navigate_to_subreddit_edit_text);
        ButterKnife.bind(this, dialog);

        mSubmitButton.setOnClickListener((v) -> {
            String input = mEditText.getInput();

            if (!input.equals("")) {
                input = input.trim();
                mEditText.setText("");

                dialog.dismiss();
                mListener.onSubredditNavigationConfirmed(input);
            }
        });

        return dialog;
    }
}
