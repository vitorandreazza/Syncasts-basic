package com.alchemist.syncasts.services;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.NotificationCompat;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.SyncastsApplication;
import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.ui.player.PlayerActivity;
import com.alchemist.syncasts.utils.Utils;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import javax.inject.Inject;

import timber.log.Timber;

public class PlaybackService extends Service implements IPlayback, IPlayback.Callback {

    private static final String ACTION_PLAY_TOGGLE = "com.alchemist.syncasts.action.ACTION_PLAY_TOGGLE";
    private static final String ACTION_STOP_SERVICE = "com.alchemist.syncasts.action.ACTION_STOP_SERVICE";
    private static final int NOTIFICATION_ID = 1;

    @Inject Player mPlayer;

    private final Binder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        public PlaybackService getService()  {
            return PlaybackService.this;
        }
    }

    public static Intent getStartIntent(Context context) {
        return new Intent(context, PlaybackService.class);
    }

    public static boolean isRunning(Context context) {
        return Utils.isServiceRunning(context, PlaybackService.class);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SyncastsApplication.get(this).getComponent().inject(this);
        registerCallback(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_PLAY_TOGGLE.equals(action)) {
                if (isPlaying()) {
                    pause();
                } else {
                    play();
                }
            } else if (ACTION_STOP_SERVICE.equals(action)) {
                if (isPlaying()) pause();
                stopForeground(true);
            }
        }
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (isPlaying()) pause();
        NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mgr.cancel(NOTIFICATION_ID);
        stopForeground(true);
        unregisterCallback(this);
        stopSelf();
    }

    @Override
    public boolean stopService(Intent name) {
        stopForeground(true);
        unregisterCallback(this);
        return super.stopService(name);
    }

    @Override
    public void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    @Override
    public boolean play() {
        return mPlayer.play();
    }

    @Override
    public boolean play(Episode episode) {
        return mPlayer.play(episode);
    }

    @Override
    public boolean pause() {
        return mPlayer.pause();
    }

    @Override
    public boolean isPlaying() {
        return mPlayer.isPlaying();
    }

    @Override
    public boolean isPreparingAsync() {
        return mPlayer.isPreparingAsync();
    }

    @Override
    public boolean isPaused() {
        return mPlayer.isPaused();
    }

    @Override
    public int getProgress() {
        return mPlayer.getProgress();
    }

    @Override
    public int getDuration() {
        return mPlayer.getDuration();
    }

    @Override
    public Episode getPlayingEpisode() {
        return mPlayer.getPlayingEpisode();
    }

    @Override
    public boolean seekTo(int progress) {
        return mPlayer.seekTo(progress);
    }

    @Override
    public void registerCallback(Callback callback) {
        mPlayer.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(Callback callback) {
        mPlayer.unregisterCallback(callback);
    }

    @Override
    public void removeCallbacks() {
        mPlayer.removeCallbacks();
    }

    @Override
    public void releasePlayer() {
        mPlayer.releasePlayer();
        super.onDestroy();
    }

    /**
     * Playback Callbacks
     */
    @Override
    public void onPlayStatusChanged(boolean isPlaying) {
        showNotification();
    }

    @Override
    public void onMediaPlayerPrepared() {
    }

    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {
        final NotificationCompat.Builder builder = getBuilder();

        setupNotificationActions(builder);

        builder.setSmallIcon(getNotificationIcon())
                .setDeleteIntent(getActionPendingIntent(ACTION_STOP_SERVICE))
                .setStyle(new NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0, 1, 2)
                        .setShowCancelButton(true)
                        .setCancelButtonIntent(getActionPendingIntent(ACTION_STOP_SERVICE)));

        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.setLargeIcon(bitmap);
                showNotification(builder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
                Timber.e("onBitmapFailed");
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Timber.i("onPrepareLoad");
            }
        };

        Picasso.with(this)
                .load(getPlayingEpisode().getPodcast().getImageLink())
                .into(target);

        showNotification(builder.build());
    }

    private NotificationCompat.Builder getBuilder() {
        Episode currentEpisode = getPlayingEpisode();
        PendingIntent contentIntent = PendingIntent.getActivity(
                this, 0, PlayerActivity.getStartIntent(this),
                PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent deleteIntent = getActionPendingIntent(ACTION_STOP_SERVICE);

        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setContentTitle(currentEpisode.getTitle())
                .setContentText(currentEpisode.getPodcast().getAuthor())
                .setContentIntent(contentIntent)
                .setDeleteIntent(deleteIntent)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setWhen(System.currentTimeMillis())
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setShowWhen(false);

        return builder;
    }

    private void setupNotificationActions(NotificationCompat.Builder builder) {
        addNotificationAction(builder, R.drawable.ic_skip_previous_24dp,
                R.string.action_previous, "");

        if (mPlayer.isPlaying() || mPlayer.isPreparingAsync()) {
            addNotificationAction(builder, R.drawable.ic_pause_24dp,
                    R.string.action_pause, ACTION_PLAY_TOGGLE);
        } else {
            addNotificationAction(builder, R.drawable.ic_play_arrow_24dp,
                    R.string.action_play, ACTION_PLAY_TOGGLE);
        }

        addNotificationAction(builder, R.drawable.ic_skip_next_24dp,
                R.string.action_skip, "");
    }

    private void addNotificationAction(NotificationCompat.Builder builder,
                                       @DrawableRes int icon, @StringRes int string,
                                       String action) {
        PendingIntent intent = getActionPendingIntent(action);
        builder.addAction(new NotificationCompat.Action(icon, getString(string), intent));
    }

    private void showNotification(Notification notification) {
        if (!mPlayer.isPlaying()) {
            stopForeground(false);

            NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            mgr.notify(NOTIFICATION_ID, notification);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    @DrawableRes
    private int getNotificationIcon() {
        return mPlayer.isPlaying() ? R.drawable.ic_play_arrow_24dp : R.drawable.ic_pause_24dp;
    }

    private PendingIntent getActionPendingIntent(String action) {
        Intent intent = getStartIntent(this);
        intent.setAction(action);
        return PendingIntent.getService(this, 0, intent, 0);
    }
}
