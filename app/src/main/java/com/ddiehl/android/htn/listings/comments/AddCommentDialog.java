package com.ddiehl.android.htn.listings.comments;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import com.ddiehl.android.htn.R;
import com.ddiehl.android.htn.view.BaseDaggerDialogFragment;

import org.jetbrains.annotations.NotNull;

public class AddCommentDialog extends BaseDaggerDialogFragment {

    public static final String TAG = AddCommentDialog.class.getSimpleName();

    public static final String EXTRA_PARENT_ID = "EXTRA_PARENT_ID";
    public static final String EXTRA_COMMENT_TEXT = "EXTRA_COMMENT_TEXT";
    public static final String REQUEST_KEY = "AddCommentDialog/request_key";
    public static final String BUNDLE_KEY_RESULT_CODE = "AddCommentDialog/result_code";

    private EditText mCommentEditText;
    private Button commentSubmit;

    private String parentFullName;

    public static AddCommentDialog newInstance(@NotNull String parentFullname) {
        AddCommentDialog dialog = new AddCommentDialog();
        Bundle args = new Bundle();
        args.putString(EXTRA_PARENT_ID, parentFullname);
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        parentFullName = getArguments().getString(EXTRA_PARENT_ID);
    }

    @NotNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.add_comment_dialog);
        mCommentEditText = dialog.findViewById(R.id.comment_edit_text);
        commentSubmit = dialog.findViewById(R.id.comment_submit);
        init();
        return dialog;
    }

    private void init() {
        commentSubmit.setOnClickListener(view -> {
            String commentText = mCommentEditText.getText().toString();
            Bundle result = new Bundle();
            result.putInt(BUNDLE_KEY_RESULT_CODE, Activity.RESULT_OK);
            result.putString(EXTRA_PARENT_ID, parentFullName);
            result.putString(EXTRA_COMMENT_TEXT, commentText);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, result);
            dismiss();
        });
    }
}
