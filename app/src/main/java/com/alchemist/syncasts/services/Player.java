package com.alchemist.syncasts.services;

import android.media.MediaPlayer;

import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.store.DataManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class Player implements IPlayback, MediaPlayer.OnPreparedListener {

    private Episode mEpisode;
    private MediaPlayer mMediaPlayer;
    private List<Callback> mCallbacks = new ArrayList<>(2);
    private boolean mIsPaused;
    private boolean mIsPreparingAsync;
    private DataManager mDataManager;

    @Inject
    public Player(DataManager dataManager) {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnPreparedListener(this);
        mDataManager = dataManager;
    }

    @Override
    public boolean play() {
        if (mIsPaused) {
            mMediaPlayer.start();
            notifyPlayStatusChanged(true);
            return true;
        }
        if (mEpisode != null) {
            if (!mEpisode.isPlayed()) {
                mEpisode.setPlayed(true);
                mDataManager.updateEpisode(mEpisode);
            }
            try {
                mMediaPlayer.reset();
                if (mEpisode.getLocalDir() != null && new File(mEpisode.getLocalDir()).exists()) {
                    mMediaPlayer.setDataSource(mEpisode.getLocalDir());
                } else {
                    mMediaPlayer.setDataSource(mEpisode.getMediaUrl());
                }
                mMediaPlayer.prepareAsync();
                mIsPreparingAsync = true;
                notifyPlayStatusChanged(true);
            } catch (IOException e) {
                Timber.e("Data source couldn't be set: " + e);
                notifyPlayStatusChanged(false);
                return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean play(Episode episode) {
        if (episode == null) return false;

        mIsPaused = false;
        mEpisode = episode;
        return play();
    }

    @Override
    public boolean pause() {
        Episode episode = getPlayingEpisode();
        if (episode != null) {
            episode.setProgress(getProgress());
            mDataManager.updateEpisode(episode);
        }

        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
            mIsPaused = true;
            notifyPlayStatusChanged(false);
            return true;
        }
        return false;
    }

    @Override
    public boolean isPlaying() {
        return mMediaPlayer.isPlaying();
    }

    @Override
    public boolean isPreparingAsync() {
        return mIsPreparingAsync;
    }

    @Override
    public boolean isPaused() {
        return mIsPaused;
    }

    @Override
    public int getProgress() {
        return mMediaPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return mMediaPlayer.getDuration();
    }

    @Override
    public Episode getPlayingEpisode() {
        return mEpisode;
    }

    @Override
    public boolean seekTo(int progress) {
        if (mEpisode == null) return false;
        if (progress <= mMediaPlayer.getDuration()) {
            mMediaPlayer.seekTo(progress);
        }
        return true;
    }

    @Override
    public void releasePlayer() {
        mEpisode = null;
        mMediaPlayer.reset();
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    /***** Listeners *****/
    @Override
    public void onPrepared(MediaPlayer mp) {
        mIsPreparingAsync = false;
        mp.start();
        int progress = getPlayingEpisode().getProgress();
        if (progress != 0) {
            seekTo(progress);
        }
        notifyOnPreparedListener();
    }

    /***** Callbacks *****/
    @Override
    public void registerCallback(Callback callback) {
        mCallbacks.add(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mCallbacks.remove(callback);
    }

    @Override
    public void removeCallbacks() {
        mCallbacks.clear();
    }

    private void notifyPlayStatusChanged(boolean isPlaying) {
        for (Callback callback : mCallbacks) {
            callback.onPlayStatusChanged(isPlaying);
        }
    }

    private void notifyOnPreparedListener() {
        for (Callback callback : mCallbacks) {
            callback.onMediaPlayerPrepared();
        }
    }
}