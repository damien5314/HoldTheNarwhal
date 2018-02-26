package com.ddiehl.android.htn.notifications

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import rxreddit.api.RedditService
import rxreddit.model.ListingResponse

/**
 * TODO documentation
 */
class UnreadInboxChecker(
        private val redditService: RedditService
) {

    fun check(): Single<ListingResponse> {
        val isLoggedIn = redditService.isUserAuthorized
        return if (isLoggedIn) {
            redditService.getInbox("unread", null, null)
                    .flatMap(this::checkNullResponse)
                    .singleOrError()
        } else {
            Single.error(IllegalStateException("User not logged in"))
        }
    }

    private fun checkNullResponse(listingResponse: ListingResponse): ObservableSource<ListingResponse> {
        return if (listingResponse.data.children != null) {
            Observable.just(listingResponse)
        } else {
            Observable.error(NullPointerException("null children in response"))
        }
    }
}
