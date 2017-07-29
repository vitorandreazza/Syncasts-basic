package com.alchemist.syncasts.ui.main.discover;

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
public class DiscoverPresenter extends BasePresenter<DiscoverMvpView> {

    private final DataManager mDataManager;
    private final CompositeSubscription mCompositeSubscription;

    @Inject
    public DiscoverPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        super.detachView();
        mCompositeSubscription.clear();
    }

    public void loadTopPodcasts(String country) {
        checkViewAttached();
        Subscription subscription = mDataManager.getTopPodcasts(country)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<ItunesResult>() {
                    @Override
                    public void onCompleted() {
                        getMvpView().setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e, "There was an error loading the podcasts.");
                        getMvpView().showError();
                    }

                    @Override
                    public void onNext(ItunesResult itunesResult) {
                        getMvpView().setAdapterItems(itunesResult.getResults());
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    public String getPreferenceCountry() {
        return mDataManager.getPreferencesHelper().getCountry();
    }
}
