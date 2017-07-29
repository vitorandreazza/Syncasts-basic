package com.alchemist.syncasts.ui.search;

import com.alchemist.syncasts.data.store.DataManager;
import com.alchemist.syncasts.data.model.ItunesResult;
import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.ui.BasePresenter;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@ConfigPersistentScope
public class SearchPresenter extends BasePresenter<SearchMvpView> {

    private final DataManager mDataManager;
    private final CompositeSubscription mCompositeSubscription;

    @Inject
    public SearchPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        super.detachView();
        mCompositeSubscription.clear();
    }

    public void searchFor(String query) {
        checkViewAttached();
        Subscription subscription = mDataManager.searchPodcast(query)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ItunesResult>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the podcasts.");
                    }

                    @Override
                    public void onNext(ItunesResult itunesResult) {
                        getMvpView().setSearchResults(itunesResult.getResults());
                    }
                });
        mCompositeSubscription.add(subscription);
    }
}
