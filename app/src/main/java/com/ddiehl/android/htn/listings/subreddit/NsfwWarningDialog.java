package com.ddiehl.android.htn.listings.subreddit;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseDaggerDialogFragment;

import org.jetbrains.annotations.NotNull;

import timber.log.Timber;

public class NsfwWarningDialog extends BaseDaggerDialogFragment {

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
