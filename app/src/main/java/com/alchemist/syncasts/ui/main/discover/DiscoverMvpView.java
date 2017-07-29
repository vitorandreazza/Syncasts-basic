package com.alchemist.syncasts.ui.main.discover;

import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.ui.MvpView;

import java.util.List;

public interface DiscoverMvpView extends MvpView {

    void setAdapterItems(List<ItunesPodcast> podcasts);

    void showError();

    void setLoadingIndicator(boolean visible);
}
