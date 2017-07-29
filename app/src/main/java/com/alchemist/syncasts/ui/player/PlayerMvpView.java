package com.alchemist.syncasts.ui.player;

import android.content.ServiceConnection;
import android.support.annotation.NonNull;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.services.PlaybackService;
import com.alchemist.syncasts.ui.MvpView;

public interface PlayerMvpView extends MvpView {

    void onPlaybackServiceBound(@NonNull PlaybackService playbackService);

    void onPodcastUpdated(Episode playingEpisode);

    void onPlaybackServiceUnbound();

    void bindPlaybackService(ServiceConnection serviceConnection);

    void unbindPlaybackService(ServiceConnection serviceConnection);

    void playPodcast();

    void startPlaybackService();

    void onPlayStatusChanged(boolean isPlaying);
}
