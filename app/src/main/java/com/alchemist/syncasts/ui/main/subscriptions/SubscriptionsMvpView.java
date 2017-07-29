package com.alchemist.syncasts.ui.main.subscriptions;

import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.ui.MvpView;

import java.util.List;

public interface SubscriptionsMvpView extends MvpView {

    void setPodcasts(List<Podcast> podcasts);
}
