package com.alchemist.syncasts.ui.search;

import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.ui.MvpView;

import java.util.List;

public interface SearchMvpView extends MvpView {

    void setSearchResults(List<ItunesPodcast> podcasts);
}
