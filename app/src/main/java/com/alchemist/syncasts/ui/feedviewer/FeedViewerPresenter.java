package com.alchemist.syncasts.ui.feedviewer;

import com.alchemist.syncasts.data.inject.ConfigPersistentScope;
import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.data.store.DataManager;
import com.alchemist.syncasts.ui.BasePresenter;
import com.alchemist.syncasts.utils.PodcastUtils;

import java.util.List;

import javax.inject.Inject;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

@ConfigPersistentScope
public class FeedViewerPresenter extends BasePresenter<FeedViewerMvpView> {

    private final DataManager mDataManager;
    private final CompositeSubscription mCompositeSubscription;

    @Inject
    public FeedViewerPresenter(DataManager dataManager) {
        mDataManager = dataManager;
        mCompositeSubscription = new CompositeSubscription();
    }

    @Override
    public void detachView() {
        super.detachView();
        mCompositeSubscription.clear();
    }

    public void getEpisodeTimeSize(final int duration, final String url) {
        checkViewAttached();
        Subscription subscription = mDataManager.getFileSize(url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(size -> getMvpView().showEpisodeTimeSize(duration, size), Timber::e);
        mCompositeSubscription.add(subscription);
    }

    public List<Episode> getEpisodes(Podcast podcast) {
        return mDataManager.getEpisodes(podcast);
    }

    public void parseAndLoadEpisodes(String feedLink, int numEpisodes) {
        checkViewAttached();
        Subscription subscription =
                PodcastUtils.loadFeedAndSavePodcast(mDataManager, feedLink, numEpisodes)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Podcast>() {
                    @Override
                    public void onCompleted() {
                        getMvpView().setLoadingIndicator(false);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e(e);
                    }

                    @Override
                    public void onNext(Podcast podcast) {
                        getMvpView().setEpisodes(podcast.getEpisodes());
                    }
                });
        mCompositeSubscription.add(subscription);
    }

    public void updateEpisode(Episode episode) {
        mDataManager.updateEpisode(episode);
    }

    public Podcast getPodcast(String feedUrl) {
        return mDataManager.getPodcast(feedUrl);
    }
}
