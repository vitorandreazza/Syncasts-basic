package com.alchemist.syncasts.utils;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.data.store.DataManager;
import com.einmalfel.earl.EarlParser;
import com.einmalfel.earl.Feed;
import com.einmalfel.earl.RSSFeed;
import com.einmalfel.earl.RSSItem;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import rx.Observable;
import timber.log.Timber;

public final class PodcastUtils {

    public static Observable<Podcast> loadFeedAndSavePodcast(DataManager dataManager,
                                                             String feedUrl,
                                                             int numEpisodes) {
        return parseFeedAndSavePodcast(Observable.just(feedUrl), dataManager, numEpisodes);
    }

    public static Observable<Podcast> loadFeedAndSavePodcast(DataManager dataManager,
                                                             ItunesPodcast itunesPodcast,
                                                             int numEpisodes) {
        final String feedUrl = itunesPodcast.getFeedUrl();
        Observable<String> feedLinkObservable;
        if (feedUrl == null) {
            feedLinkObservable = getItunesPodcastFeedUrl(dataManager, itunesPodcast);
        } else {
            feedLinkObservable = Observable.just(feedUrl);
        }
        return parseFeedAndSavePodcast(feedLinkObservable, dataManager, numEpisodes);
    }

    public static Observable<String> getItunesPodcastFeedUrl(
            DataManager dataManager, ItunesPodcast itunesPodcast) {
        if (itunesPodcast.getFeedUrl() != null) return Observable.just(itunesPodcast.getFeedUrl());
        return dataManager.getFeedUrl(itunesPodcast.getId())
                .map(itunesResult -> itunesResult.getResults().get(0).getFeedUrl());
    }

    private static Observable<Podcast> parseFeedAndSavePodcast(Observable<String> feedUrlObservable,
                                                               final DataManager dataManager,
                                                               final int numEpisodes) {
        return feedUrlObservable.flatMap(dataManager::requestUrl)
                .flatMap(responseBody -> Observable.create((Observable.OnSubscribe<Feed>) subscriber -> {
                    try {
                        Feed feed = EarlParser.parseOrThrow(responseBody.byteStream(), numEpisodes);
                        Timber.i("Feed Type = %s", feed.getClass());
                        subscriber.onNext(feed);
                        subscriber.onCompleted();
                    } catch (XmlPullParserException | IOException | DataFormatException e) {
                        subscriber.onError(e);
                    }
                }))
                .zipWith(feedUrlObservable, (feed, feedUrl) -> {
                    if (feed instanceof RSSFeed) {
                        RSSFeed rssFeed = (RSSFeed) feed;
                        if (rssFeed.itunes != null) {
                            Podcast podcast = new Podcast.Builder(feedUrl)
                                    .setTitle(rssFeed.title)
                                    .setAuthor(rssFeed.itunes.author)
                                    .setImageLink(rssFeed.itunes.image.toString())
                                    .build();
                            List<Episode> episodes = new ArrayList<>(rssFeed.items.size());
                            for (RSSItem rssItem : rssFeed.items) {
                                if (rssItem.itunes != null) {
                                    Episode episode = new Episode.Builder(podcast)
                                            .setTitle(rssItem.title)
                                            .setDescription(rssItem.description)
                                            .setMediaUrl(rssItem.enclosures.get(0).url.toString())
                                            .setPubDate(rssItem.pubDate)
                                            .setDuration(rssItem.itunes.duration == null ?
                                                    0 : rssItem.itunes.duration * 1000)
                                            .build();
                                    episodes.add(episode);
                                }
                            }
                            podcast.setEpisodes(episodes);
                            return podcast;
                        }
                    }
                    return null;
                }).doOnNext(dataManager::insertPodcastAndEpisodes);
    }
}
