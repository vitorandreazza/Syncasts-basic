package com.alchemist.syncasts.ui.main.subscriptions;

import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.data.store.DataManager;
import com.alchemist.syncasts.ui.BasePresenter;

import javax.inject.Inject;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@ConfigPersistentScope
public class SubscriptionsPresenter extends BasePresenter<SubscriptionsMvpView> {
    private final DataManager mDataManager;
    private final CompositeSubscription mCompositeSubscription;

    @Inject
    public SubscriptionsPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        super.detachView();
        mCompositeSubscription.clear();
    }

    public void loadSubscriptions() {
        checkViewAttached();
        Subscription subscription = mDataManager.getPodcasts()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMvpView()::setPodcasts, Timber::e);
        mCompositeSubscription.add(subscription);
    }

    public int countUnplayedEpisodes(Podcast podcast) {
        return mDataManager.countUnplayedEpisodes(podcast);
    }
}
