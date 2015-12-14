package com.ddiehl.android.htn.view.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Window;
import android.widget.Button;

import com.ddiehl.android.htn.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddCommentDialog extends DialogFragment {
  @Bind(R.id.comment_submit)
  Button mCommentSubmit;
  private Callbacks mCallbacks;

  public interface Callbacks {
    void onCommentSubmitted(@NonNull String text);
  }

  @Override
  public Dialog onCreateDialog(Bundle savedInstanceState) {
    Dialog dialog = new Dialog(getActivity());
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
    dialog.setContentView(R.layout.add_comment_dialog);
    ButterKnife.bind(this, dialog);
    init();
    return dialog;
  }

  private void init() {
    mCommentSubmit.setOnClickListener(view -> {

    });
  }
}
