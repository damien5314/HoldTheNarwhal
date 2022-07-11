package com.ddiehl.android.htn.navigation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.inputmethod.EditorInfo;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseDaggerDialogFragment;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class SubredditNavigationDialog extends BaseDaggerDialogFragment {

    public static final String TAG = SubredditNavigationDialog.class.getSimpleName();

    public interface Callbacks {
        void onSubredditNavigationConfirmed(String subreddit);

        void onSubredditNavigationCancelled();
    }

    private SubredditEditText editText;
    private View submitButton;

    private Callbacks listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass().getSimpleName()
                    + " must implement SubredditNavigationDialog.Callbacks");
        }
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("Showing subreddit navigation dialog");

        Dialog dialog = new Dialog(getActivity());

        // Inflate layout and bind views
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.navigate_to_subreddit_edit_text);

        editText = dialog.findViewById(R.id.drawer_navigate_to_subreddit_text);
        submitButton = dialog.findViewById(R.id.drawer_navigate_to_subreddit_go);

        // Detect taps on the submit button
        submitButton.setOnClickListener((v) -> {
            String input = editText.getInput();

            if (!input.equals("")) {
                input = input.trim();
                editText.setText("");

                dialog.dismiss();
                listener.onSubredditNavigationConfirmed(input);
            }
        });

        // Detect software keyboard "done" event
        editText.setOnEditorActionListener((v, actionId, event) -> {
            if (enterKeyPressed(actionId, event)) {
                submitButton.callOnClick();
            }
            return false;
        });

        // Detect hardware keyboard "enter" key press
        editText.setOnKeyListener((v, keyCode, event) -> {
            if (enterKeyPressed(keyCode, event)) {
                submitButton.callOnClick();
            }
            return false;
        });

        return dialog;
    }

    boolean enterKeyPressed(int keyCode, KeyEvent event) {
        // keyCode can either be an EditorInfo actionId or a KeyEvent keyCode,
        // just consolidating the logic a tad.
        return keyCode == EditorInfo.IME_ACTION_DONE ||
                (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER);
    }
}
