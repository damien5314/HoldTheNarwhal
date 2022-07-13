package com.ddiehl.android.htn.listings.report

import androidx.fragment.app.Fragment
import javax.inject.Inject

class ReportViewRouter @Inject constructor(
    private val fragment: Fragment,
) {

    fun openReportView(listingFullName: String) {
//        fragment.parentFragmentManager.setFragmentResultListener(
//            BaseListingsFragment.REQUEST_REPORT_LISTING.toString(),
//            fragment
//        ) { requestKey, result ->
//            when (requestKey) {
//                BaseListingsFragment.REQUEST_REPORT_LISTING.toString() -> {
//                    when (result.getInt(ReportView.BUNDLE_KEY_RESULT_CODE, -100)) {
//                        Activity.RESULT_OK -> {
//                            Snackbar.make(fragment.requireView(), R.string.report_successful, Snackbar.LENGTH_LONG)
//                                .show()
//                        }
//                        Activity.RESULT_CANCELED -> {
//                            Snackbar.make(fragment.requireView(), R.string.report_error, Snackbar.LENGTH_LONG)
//                                .show()
//                        }
//                    }
//                }
//            }
//        }

        ReportView.newInstance(listingFullName, null)
            .show(fragment.parentFragmentManager, ReportView.TAG)
    }
}
