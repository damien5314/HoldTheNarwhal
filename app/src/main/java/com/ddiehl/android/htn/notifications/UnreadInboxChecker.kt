package com.ddiehl.android.htn.notifications

import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableSource
import io.reactivex.rxjava3.core.Single
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
