package com.ddiehl.android.htn.listings.subreddit;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import timber.log.Timber;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class NsfwWarningDialog extends DialogFragment {

    public static final String TAG = NsfwWarningDialog.class.getSimpleName();

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Timber.i("Showing NSFW warning dialog");
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_nsfw_title)
                .setMessage(R.string.dialog_nsfw_message)
                .setPositiveButton(R.string.dialog_nsfw_confirm, onConfirm())
                .setNegativeButton(R.string.dialog_nsfw_decline, (dialog, which) -> onCancel(dialog))
                .create();
    }

    DialogInterface.OnClickListener onConfirm() {
        return (dialog, which) ->
                getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, null);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CANCELED, null);
    }
}
