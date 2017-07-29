package com.alchemist.syncasts.ui.podcastdetails;

import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.ui.MvpView;

public interface ItunesPodcastDetailsMvpView extends MvpView {

    void updatePodcastStatus(Podcast podcast);

    void setupSubscribed(Podcast podcast);

    void setupUnsubscribed(ItunesPodcast itunesPodcast);

    void setLoading(boolean visible);
}
