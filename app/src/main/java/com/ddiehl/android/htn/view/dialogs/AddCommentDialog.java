package com.ddiehl.android.htn.view.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.ddiehl.android.htn.R;

import butterknife.Bind;
import butterknife.ButterKnife;

public class AddCommentDialog extends DialogFragment {
  public static final String EXTRA_PARENT_ID = "extra_parent_id";
  public static final String EXTRA_COMMENT_TEXT = "extra_comment_text";

  @Bind(R.id.comment_edit_text) EditText mCommentEditText;
  @Bind(R.id.comment_submit) Button mCommentSubmit;

  private String mParentFullName;

  public AddCommentDialog() {
  }

  public static AddCommentDialog newInstance(@NonNull String parentFullname) {
    AddCommentDialog dialog = new AddCommentDialog();
    Bundle args = new Bundle();
    args.putString(EXTRA_PARENT_ID, parentFullname);
    dialog.setArguments(args);
    return dialog;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mParentFullName = getArguments().getString(EXTRA_PARENT_ID);
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
      String commentText = mCommentEditText.getText().toString();
      Intent data = new Intent();
      data.putExtra(EXTRA_PARENT_ID, mParentFullName);
      data.putExtra(EXTRA_COMMENT_TEXT, commentText);
      dismiss();
      getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, data);
    });
  }
}
