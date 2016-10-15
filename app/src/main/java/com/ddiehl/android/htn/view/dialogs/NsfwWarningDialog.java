package com.ddiehl.android.htn.view.dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.ddiehl.android.htn.R;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

public class NsfwWarningDialog extends DialogFragment {

    public static final String TAG = NsfwWarningDialog.class.getSimpleName();

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.dialog_nsfw_title)
                .setMessage(R.string.dialog_nsfw_message)
                .setPositiveButton(R.string.dialog_nsfw_confirm, (dialog, which) ->
                        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_OK, null))
                .setNegativeButton(R.string.dialog_nsfw_decline, (dialog, which) -> onCancel(dialog))
                .create();
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        getTargetFragment().onActivityResult(getTargetRequestCode(), RESULT_CANCELED, null);
    }
}
