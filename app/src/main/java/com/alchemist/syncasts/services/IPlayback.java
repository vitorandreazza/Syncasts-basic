package com.alchemist.syncasts.services;

import com.alchemist.syncasts.data.model.Episode;


public interface IPlayback {

    boolean play();

    boolean play(Episode episode);

    boolean pause();

    boolean isPlaying();

    boolean isPreparingAsync();

    boolean isPaused();

    int getProgress();

    int getDuration();

    Episode getPlayingEpisode();

    boolean seekTo(int progress);

    void registerCallback(Callback callback);

    void unregisterCallback(Callback callback);

    void removeCallbacks();

    void releasePlayer();

    interface Callback {

        void onPlayStatusChanged(boolean isPlaying);

        void onMediaPlayerPrepared();
    }
}
