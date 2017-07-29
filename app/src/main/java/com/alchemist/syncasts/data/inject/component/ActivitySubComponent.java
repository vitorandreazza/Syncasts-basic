package com.alchemist.syncasts.data.inject.component;

import android.preference.PreferenceFragment;

import com.alchemist.syncasts.data.inject.PerActivityScope;
import com.alchemist.syncasts.data.inject.module.ActivityModule;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.feedviewer.FeedViewerActivity;
import com.alchemist.syncasts.ui.main.MainActivity;
import com.alchemist.syncasts.ui.main.discover.DiscoverFragment;
import com.alchemist.syncasts.ui.main.subscriptions.SubscriptionsFragment;
import com.alchemist.syncasts.ui.player.PlayerActivity;
import com.alchemist.syncasts.ui.podcastdetails.ItunesPodcastDetailsFragment;
import com.alchemist.syncasts.ui.search.SearchActivity;

import dagger.Subcomponent;

@PerActivityScope
@Subcomponent(modules = ActivityModule.class)
public interface ActivitySubComponent {

    void injectBaseActivity(BaseActivity baseActivity);

    void inject(MainActivity mainActivity);
    void inject(FeedViewerActivity feedViewerActivity);
    void inject(PlayerActivity playerActivity);
    void inject(SearchActivity searchActivity);

    void inject(DiscoverFragment discoverFragment);
    void inject(SubscriptionsFragment subscriptionsFragment);
    void inject(ItunesPodcastDetailsFragment itunesPodcastDetailsFragment);
    void inject(PreferenceFragment preferenceFragment);
}
