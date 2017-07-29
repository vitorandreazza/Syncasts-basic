package com.alchemist.syncasts.ui.podcastdetails;

import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.data.store.DataManager;
import com.alchemist.syncasts.ui.BasePresenter;
import com.alchemist.syncasts.utils.PodcastUtils;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@ConfigPersistentScope
public class ItunesPodcastDetailsPresenter extends BasePresenter<ItunesPodcastDetailsMvpView> {

    private final DataManager mDataManager;
    private final CompositeSubscription mCompositeSubscription;

    @Inject
    public ItunesPodcastDetailsPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        super.detachView();
        mCompositeSubscription.clear();
    }

    public void initializeSubscriptionBtn(ItunesPodcast itunesPodcast) {
        checkViewAttached();
        Observable<String>  observablefeedUrl;
        if (itunesPodcast.getFeedUrl() == null) {
            observablefeedUrl = PodcastUtils.getItunesPodcastFeedUrl(mDataManager, itunesPodcast);
        } else {
            observablefeedUrl = Observable.just(itunesPodcast.getFeedUrl());
        }

        Subscription subscription = observablefeedUrl
                        .subscribeOn(Schedulers.io())
                        .map(mDataManager::getPodcast)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Podcast>() {
                            @Override
                            public void onCompleted() {
                                getMvpView().setLoading(false);
                            }

                            @Override
                            public void onError(Throwable e) {
                                Timber.e(e);
                            }

                            @Override
                            public void onNext(Podcast podcast) {
                                if (podcast == null) {
                                    getMvpView().setupUnsubscribed(itunesPodcast);
                                } else {
                                    getMvpView().setupSubscribed(podcast);
                                }
                            }
                        });
        mCompositeSubscription.add(subscription);
    }

    public void subscribePodcast(ItunesPodcast itunesPodcast, int numEpisodes) {
        checkViewAttached();
        Subscription subscription = PodcastUtils.loadFeedAndSavePodcast(mDataManager, itunesPodcast, numEpisodes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getMvpView()::updatePodcastStatus, Timber::e);
        mCompositeSubscription.add(subscription);
    }
}
