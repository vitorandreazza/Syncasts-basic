package com.alchemist.syncasts.data.inject.module;

import android.app.Application;
import android.content.Context;

import com.alchemist.syncasts.data.inject.ApplicationContext;
import com.alchemist.syncasts.data.model.ItunesResult;
import com.alchemist.syncasts.data.store.local.PreferencesHelper;
import com.alchemist.syncasts.data.store.local.PresetThemeStore;
import com.alchemist.syncasts.data.store.local.ThemeStore;
import com.alchemist.syncasts.data.store.remote.ItunesDeserializer;
import com.alchemist.syncasts.data.store.remote.ItunesService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.Cache;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

@Module
public class ApplicationModule {

    protected final Application mApplication;

    public ApplicationModule(Application mApplication) {
        this.mApplication = mApplication;
    }

    @Provides
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @ApplicationContext
    Context provideContext() {
        return mApplication;
    }

    @Provides
    @Singleton
    ItunesService provideItunesService(OkHttpClient okHttpClient) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ItunesResult.class, new ItunesDeserializer())
                .create();
        Retrofit retrofit = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(ItunesService.ENDPOINT)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build();
        return retrofit.create(ItunesService.class);
    }

    @Provides
    @Singleton
    OkHttpClient provideOkhttpclient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);
        File cacheFile = new File(mApplication.getCacheDir(), "HttpResponseCache");
        return new OkHttpClient.Builder()
                .addInterceptor(logging)
                .cache(new Cache(cacheFile, 10 * 1024 * 1024))
                .build();
    }

    @Provides
    @Singleton
    public ThemeStore provideThemeStore(@ApplicationContext Context context, PreferencesHelper preferencesHelper) {
        return new PresetThemeStore(context, preferencesHelper);
    }
}
