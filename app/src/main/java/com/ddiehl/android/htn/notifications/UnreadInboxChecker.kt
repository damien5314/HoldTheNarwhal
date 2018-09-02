package com.ddiehl.android.htn.notifications

import io.reactivex.Observable
import io.reactivex.ObservableSource
import io.reactivex.Single
import rxreddit.api.RedditService
import rxreddit.model.ListingResponse

/**
 * Class which uses [RedditService] to check whether a user has unread
 * inbox messages.
 */
class UnreadInboxChecker(
        private val redditService: RedditService
) {

    fun check(): Single<ListingResponse> {
        return redditService.getInbox("unread", false, null, null)
            .flatMap(this::checkNullResponse)
            .singleOrError()
    }

    private fun checkNullResponse(listingResponse: ListingResponse): ObservableSource<ListingResponse> {
        return if (listingResponse.data.children != null) {
            Observable.just(listingResponse)
        } else {
            Observable.error(NullPointerException("null children in response"))
        }
    }
}
