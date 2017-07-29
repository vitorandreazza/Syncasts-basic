package com.alchemist.syncasts.data.store.remote;

import com.alchemist.syncasts.data.model.ItunesResult;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import rx.Observable;

public interface ItunesService {

    String ENDPOINT = "https://itunes.apple.com/";

    @GET("search?media=podcast")
    Observable<ItunesResult> searchPodcast(@Query("term") String query);

    @GET("{country}/rss/toppodcasts/limit=48/explicit=true/json")
    Observable<ItunesResult> getTopPodcasts(@Path("country") String country);

    @GET("lookup")
    Observable<ItunesResult> getFeedUrl(@Query("id") Integer id);

    @GET
    Observable<ResponseBody> requestUrl(@Url String url);
}
