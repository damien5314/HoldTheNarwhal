package com.ddiehl.android.htn.view.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.widget.EditText;

import com.ddiehl.android.htn.R;

import butterknife.ButterKnife;

public class SubredditNavigationDialog extends DialogFragment {

    public static final String TAG = SubredditNavigationDialog.class.getSimpleName();

    public interface Callbacks {

        void onSubredditNavigationConfirmed(String subreddit);

        void onSubredditNavigationCancelled();
    }

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
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.navigate_to_subreddit_edit_text);
        ButterKnife.findById(dialog, R.id.drawer_navigate_to_subreddit_go)
                .setOnClickListener((v) -> {
                    EditText vInput = ButterKnife.findById(dialog,
                            R.id.drawer_navigate_to_subreddit_text);
                    String inputSubreddit = vInput.getText().toString();
                    if (inputSubreddit.equals("")) return;

                    inputSubreddit = inputSubreddit.substring(3);
                    inputSubreddit = inputSubreddit.trim();
                    vInput.setText("");
                    dialog.dismiss();
                    mListener.onSubredditNavigationConfirmed(inputSubreddit);
                });
        return dialog;
    }
}
