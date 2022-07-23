package com.ddiehl.android.htn.listings.report

import android.app.Activity
import androidx.fragment.app.Fragment
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.subjects.PublishSubject
import javax.inject.Inject

class ReportViewRouter @Inject constructor(
    private val fragment: Fragment,
) {

    enum class ReportResult {
        SUCCESS,
        CANCELED,
    }

    private val reportResultDispatcher = PublishSubject.create<ReportResult>()

    fun openReportView(listingFullName: String) {
        val fm = fragment.parentFragmentManager
        fm.setFragmentResultListener(ReportView.REQUEST_KEY, fragment) { requestKey, result ->
            when (requestKey) {
                ReportView.REQUEST_KEY -> {
                    when (result.getInt(ReportView.BUNDLE_KEY_RESULT_CODE, -100)) {
                        Activity.RESULT_OK -> reportResultDispatcher.onNext(ReportResult.SUCCESS)
                        Activity.RESULT_CANCELED -> reportResultDispatcher.onNext(ReportResult.CANCELED)
                    }
                }
            }
        }

        ReportView.newInstance(listingFullName, null)
            .show(fm, ReportView.TAG)
    }

    fun observeReportResults(): Observable<ReportResult> = reportResultDispatcher
}
