package com.alchemist.syncasts.ui.feedviewer;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.ui.MvpView;

import java.util.List;

public interface FeedViewerMvpView extends MvpView {

    void showEpisodeTimeSize(int duration, long size);

    void setEpisodes(List<Episode> episodes);

    void setLoadingIndicator(boolean active);
}
