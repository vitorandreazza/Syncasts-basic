package com.alchemist.syncasts.data.inject.component;

import android.app.Application;
import android.content.Context;

import com.alchemist.syncasts.data.inject.ApplicationContext;
import com.alchemist.syncasts.data.inject.module.ApplicationModule;
import com.alchemist.syncasts.data.store.DataManager;
import com.alchemist.syncasts.data.store.remote.ItunesService;
import com.alchemist.syncasts.services.PlaybackService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = ApplicationModule.class)
public interface ApplicationComponent {

    void inject(PlaybackService playbackService);

    @ApplicationContext Context context();
    Application application();
    ItunesService itunesService();
    DataManager dataManager();
}
