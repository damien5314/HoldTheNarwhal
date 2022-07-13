package com.ddiehl.android.htn.listings.comments

import android.app.Activity
import androidx.fragment.app.Fragment
import com.ddiehl.android.htn.listings.report.ReportView
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

class AddCommentDialogRouter @Inject constructor(
    private val fragment: Fragment,
) {

    enum class Result {
        SUCCESS,
        CANCELED,
    }

    private val resultDispatcher = PublishSubject.create<Result>()

//    fun openReplyView(listing: Listing) {
//    }

    fun openReplyDialog(listingFullName: String) {
        val fm = fragment.parentFragmentManager
        fm.setFragmentResultListener(AddCommentDialog.REQUEST_KEY, fragment) { requestKey, result ->
            when (requestKey) {
                ReportView.REQUEST_KEY -> {
                    when (result.getInt(AddCommentDialog.BUNDLE_KEY_RESULT_CODE, -100)) {
                        Activity.RESULT_OK -> resultDispatcher.onNext(Result.SUCCESS)
                        Activity.RESULT_CANCELED -> resultDispatcher.onNext(Result.CANCELED)
                    }
                }
            }
        }

        AddCommentDialog.newInstance(listingFullName)
            .show(fm, AddCommentDialog.TAG)
    }

    fun observeReportResults(): Observable<Result> = resultDispatcher
}
