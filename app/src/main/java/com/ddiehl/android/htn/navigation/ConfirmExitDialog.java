package com.ddiehl.android.htn.navigation;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import com.ddiehl.android.htn.R;

import org.jetbrains.annotations.NotNull;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class ConfirmExitDialog extends DialogFragment {

    public static final String TAG = ConfirmExitDialog.class.getSimpleName();

    public interface Callbacks {

        void onConfirmExit();

        void onCancelExit();
    }

    private Callbacks listener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (!(context instanceof Callbacks)) {
            throw new ClassCastException(context + " must implement ConfirmExitDialogs.Callbacks");
        }
        listener = (Callbacks) context;
    }

    @Override
    public void onDetach() {
        listener = null;
        super.onDetach();
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new AlertDialog.Builder(getContext())
                .setMessage(R.string.confirm_exit_dialog_title)
                .setPositiveButton(R.string.confirm_exit_dialog_confirm,
                        (dialogInterface, which) -> listener.onConfirmExit())
                .setNegativeButton(R.string.confirm_exit_dialog_cancel,
                        (dialogInterface, which) -> listener.onCancelExit())
                .create();
    }
}
