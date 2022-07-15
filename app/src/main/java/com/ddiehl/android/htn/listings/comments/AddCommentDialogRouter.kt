package com.ddiehl.android.htn.listings.comments

import android.app.Activity
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

class AddCommentDialogRouter @Inject constructor(
    private val fragment: Fragment,
) {

    sealed class Result {

        data class Success(
            val parentFullName: String,
            val commentText: String,
        ) : Result()

        object Canceled : Result()
    }

    private val resultDispatcher = PublishSubject.create<Result>()

    fun openReplyDialog(listingFullName: String) {
        val fm = fragment.parentFragmentManager
        fm.setFragmentResultListener(AddCommentDialog.REQUEST_KEY, fragment) { requestKey, result ->
            when (requestKey) {
                AddCommentDialog.REQUEST_KEY -> {
                    when (result.getInt(AddCommentDialog.BUNDLE_KEY_RESULT_CODE, -100)) {
                        Activity.RESULT_OK -> {
                            val parentFullName = result.getString(AddCommentDialog.EXTRA_PARENT_ID)
                                ?: throw IllegalStateException("Result argument expected for AddCommentDialog: ${AddCommentDialog.EXTRA_PARENT_ID}")
                            val commentText = result.getString(AddCommentDialog.EXTRA_COMMENT_TEXT)
                                ?: throw IllegalStateException("Result argument expected for AddCommentDialog: ${AddCommentDialog.EXTRA_COMMENT_TEXT}")
                            resultDispatcher.onNext(Result.Success(parentFullName, commentText))
                        }
                        Activity.RESULT_CANCELED -> resultDispatcher.onNext(Result.Canceled)
                    }
                }
            }
        }

        AddCommentDialog.newInstance(listingFullName)
            .show(fm, AddCommentDialog.TAG)
    }

    fun observeResults(): Observable<Result> = resultDispatcher
}
