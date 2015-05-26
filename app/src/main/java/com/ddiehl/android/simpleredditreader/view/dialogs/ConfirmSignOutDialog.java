package com.ddiehl.android.simpleredditreader.view.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.ddiehl.android.simpleredditreader.R;

public class ConfirmSignOutDialog extends DialogFragment {
    private static final String TAG = ConfirmSignOutDialog.class.getSimpleName();

    public interface Callbacks {
        void onSignOutConfirm();
        void onSignOutCancel();
    }

    public ConfirmSignOutDialog() { }

    public static ConfirmSignOutDialog newInstance() {
        Bundle args = new Bundle();
        ConfirmSignOutDialog dialog = new ConfirmSignOutDialog();
        dialog.setArguments(args);
        return dialog;
    }

    @NonNull @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.confirm_sign_out_dialog_title)
                .setMessage(R.string.confirm_sign_out_dialog_message)
                .setPositiveButton(R.string.confirm_sign_out_dialog_ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() instanceof Callbacks) {
                            ((Callbacks) getActivity()).onSignOutConfirm();
                        }
                    }
                })
                .setNegativeButton(R.string.confirm_sign_out_dialog_cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (getActivity() instanceof Callbacks) {
                            ((Callbacks) getActivity()).onSignOutCancel();
                        }
                    }
                })
        .setCancelable(true);

        return builder.create();
    }
}
