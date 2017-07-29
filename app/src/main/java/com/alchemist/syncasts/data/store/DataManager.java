package com.alchemist.syncasts.data.store;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.model.ItunesResult;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.data.store.local.DatabaseHelper;
import com.alchemist.syncasts.data.store.local.PreferencesHelper;
import com.alchemist.syncasts.data.store.local.ThemeStore;
import com.alchemist.syncasts.data.store.remote.ItunesService;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.internal.huc.OkHttpURLConnection;
import rx.Observable;

@Singleton
public class DataManager {

    private final ItunesService mItunesService;
    private final DatabaseHelper mDatabaseHelper;
    private final PreferencesHelper mPreferencesHelper;
    private final OkHttpClient mOkHttpClient;
    private final ThemeStore mThemeStore;

    @Inject
    public DataManager(ItunesService itunesService,
                       DatabaseHelper databaseHelper,
                       PreferencesHelper preferencesHelper,
                       OkHttpClient okHttpClient,
                       ThemeStore themeStore) {
        mItunesService = itunesService;
        mDatabaseHelper = databaseHelper;
        mPreferencesHelper = preferencesHelper;
        mOkHttpClient = okHttpClient;
        mThemeStore = themeStore;
    }

    /** PreferencesHelper **/
    public PreferencesHelper getPreferencesHelper() {
        return mPreferencesHelper;
    }

    @AppCompatDelegate.NightMode
    public int getNightMode() {
        return mThemeStore.getNightMode();
    }

    public void setTheme(AppCompatActivity activity) {
        mThemeStore.setTheme(activity);
    }

    /** ItunesService **/
    public Observable<ItunesResult> getTopPodcasts(String country) {
        return mItunesService.getTopPodcasts(country);
    }

    public Observable<ItunesResult> getFeedUrl(Integer id) {
        return mItunesService.getFeedUrl(id);
    }

    public Observable<ItunesResult> searchPodcast(String query) {
        return mItunesService.searchPodcast(query);
    }

    public Observable<ResponseBody> requestUrl(String url) {
        return mItunesService.requestUrl(url);
    }

    /** OkHttpClient **/
    public Observable<Integer> getFileSize(final String url) {
        return Observable.create(subscriber -> {
            OkHttpURLConnection connection = null;
            InputStream is = null;
            try {
                connection = new OkHttpURLConnection(new URL(url), mOkHttpClient);
                connection.connect();
                subscriber.onNext(connection.getContentLength());
                is = connection.getInputStream();
                subscriber.onCompleted();
            } catch (IOException e) {
                subscriber.onError(e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        subscriber.onError(e);
                    }
                }
                if (connection != null) connection.disconnect();
            }
        });
    }

    /** DatabaseHelper **/
    public List<Episode> getEpisodes(Podcast podcast) {
        return mDatabaseHelper.getEpisodes(podcast);
    }

    public void updateEpisode(Episode episode) {
        mDatabaseHelper.updateEpisode(episode);
    }

    public Podcast getPodcast(String feedUrl) {
        return mDatabaseHelper.getPodcast(feedUrl);
    }

    public Observable<List<Podcast>> getPodcasts() {
        return mDatabaseHelper.getPodcasts();
    }

    public void insertPodcastAndEpisodes(Podcast podcast) {
        mDatabaseHelper.insertPodcastAndEpisodes(podcast);
    }

    public int countUnplayedEpisodes(Podcast podcast) {
        return mDatabaseHelper.countUnplayedEpsiodes(podcast);
    }
}
