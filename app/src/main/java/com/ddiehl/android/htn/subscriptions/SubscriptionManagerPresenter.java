package com.ddiehl.android.htn.subscriptions;

import java.util.Collections;
import java.util.List;

import rx.Observable;
import rxreddit.model.Subreddit;

public class SubscriptionManagerPresenter {

  public Observable<List<Subreddit>> getSubscriptions() {
    return Observable.defer(() -> {
      return Observable.just(Collections.singletonList(new Subreddit()));
    });
  }
}
