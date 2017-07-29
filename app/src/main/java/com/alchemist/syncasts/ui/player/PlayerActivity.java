package com.alchemist.syncasts.ui.player;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.services.IPlayback;
import com.alchemist.syncasts.services.PlaybackService;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.views.TimeView;
import com.alchemist.syncasts.utils.DialogFactory;
import com.alchemist.syncasts.utils.StringUtils;
import com.squareup.picasso.Picasso;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import timber.log.Timber;

public class PlayerActivity extends BaseActivity implements PlayerMvpView, IPlayback.Callback {

    private static final String EXTRA_EPISODE = "com.alchemist.syncasts.ui.player.PlayerActivity.EXTRA_EPISODE";
    private static final long UPDATE_PROGRESS_INTERVAL = 1000;
    private static final int BUTTON_JUMP_INTERVAL = 10 * 1000;
    private static final int[] STATE_SET_PLAY = {R.attr.state_play, -R.attr.state_pause};
    private static final int[] STATE_SET_PAUSE = {-R.attr.state_play, R.attr.state_pause};

    @BindView(R.id.image_podcast_thumb) ImageView imagePodcastThumb;
    @BindView(R.id.now_playing_play) ImageButton btnPlayToggle;
    @BindView(R.id.text_episode_title) TextView textPodcastTitle;
    @BindView(R.id.text_podcast_author) TextView textPodcastAuthor;
    @BindView(R.id.now_playing_duration) TimeView textDuration;
    @BindView(R.id.now_playing_current_time) TimeView textProgress;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.now_playing_seek_bar) DiscreteSeekBar seekBar;
    @BindView(android.R.id.empty) ProgressBar loading;

    @Inject PlayerPresenter mPresenter;

    private IPlayback mPlayer;
    private Episode mPlayingEpisode;
    private Runnable mProgressCallback;
    private Handler mHandler = new Handler();
    private boolean mUserTouchingProgressBar;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, PlayerActivity.class);
    }

    public static Intent getStartIntent(Context context, Episode episode) {
        Intent intent = new Intent(context, PlayerActivity.class);
        intent.putExtra(EXTRA_EPISODE, (Parcelable) episode);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_player);
        ButterKnife.bind(this);
        mPresenter.attachView(this);


        initViews();

        if (getIntent().getParcelableExtra(EXTRA_EPISODE) != null) {
            mPlayingEpisode = getIntent().getParcelableExtra(EXTRA_EPISODE);
            onPodcastUpdated(mPlayingEpisode);
        }
        mPresenter.setupPlaybackService();

        overridePendingTransition(R.anim.slide_in_from_bottom, R.anim.stay);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Timber.d("isRunning=%b", PlaybackService.isRunning(this));
        Timber.d("player=%s", mPlayer);
        if (mPlayer != null) {
            mPlayer.registerCallback(this);
            if (mPlayer.isPlaying()) {
                mHandler.removeCallbacks(mProgressCallback);
                mHandler.post(mProgressCallback);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPlayer != null) mPlayer.unregisterCallback(this);
        overridePendingTransition(R.anim.stay, R.anim.slide_out_to_bottom);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mHandler.removeCallbacks(mProgressCallback);
    }

    @Override
    protected void onDestroy() {
        mPresenter.unbindPlaybackService();
        mPresenter.detachView();
        super.onDestroy();
    }

    /***** MVP View methods implementation *****/
    @Override
    public void onPlaybackServiceBound(@NonNull PlaybackService playbackService) {
        mPlayer = playbackService;
        mPlayer.registerCallback(this);
    }

    @Override
    public void onPlaybackServiceUnbound() {
        mPlayer.unregisterCallback(this);
        mPlayer = null;
    }

    @Override
    public void onPodcastUpdated(Episode playingEpisode) {
        if (playingEpisode == null) {
            updatePlayToggle(false);
            seekBar.setProgress(1);
            updateProgressTextWithProgress(0);
            mPlayer.seekTo(0);
            mHandler.removeCallbacks(mProgressCallback);
            return;
        }

        textPodcastTitle.setText(playingEpisode.getTitle());
        textPodcastAuthor.setText(playingEpisode.getPodcast().getAuthor());
        textDuration.setText(StringUtils.formatFromMilliseconds(playingEpisode.getDuration()));
        updateProgressTextWithProgress(playingEpisode.getProgress());
        seekBar.setProgress(getSeekBarProgress(playingEpisode.getProgress()));

        int height = getPortraitArtworkHeight();
        int width = getResources().getDisplayMetrics().widthPixels;
        String imgLink = playingEpisode.getPodcast().getImageLink();
        Picasso.with(this)
                .load(imgLink)
                .resize(width, height)
                .centerCrop()
                .into(imagePodcastThumb);

        mHandler.removeCallbacks(mProgressCallback);
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mHandler.post(mProgressCallback);
            } else {
                mPlayer.seekTo(playingEpisode.getProgress());
            }
        }
    }

    @Override
    public void bindPlaybackService(ServiceConnection serviceConnection) {
        bindService(PlaybackService.getStartIntent(this), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void unbindPlaybackService(ServiceConnection serviceConnection) {
        unbindService(serviceConnection);
    }

    @Override
    public void playPodcast() {
        if (mPlayingEpisode == null) {
            mPlayingEpisode = mPlayer.getPlayingEpisode();
            onPodcastUpdated(mPlayingEpisode);
        } else if (!mPlayingEpisode.equals(mPlayer.getPlayingEpisode())) {
            onPodcastUpdated(mPlayingEpisode);
            playPodcast(mPlayingEpisode);
        }
        setupSeekbarNumericTransformer();
    }

    @Override
    public void startPlaybackService() {
        if (!PlaybackService.isRunning(this)) {
            startService(PlaybackService.getStartIntent(getApplicationContext()));
        }
    }

    /***** Playback Callbacks *****/
    @Override
    public void onPlayStatusChanged(boolean isPlaying) {
        updatePlayToggle(isPlaying);
        if (isPlaying) {
            mHandler.removeCallbacks(mProgressCallback);
            mHandler.post(mProgressCallback);
        } else {
            mHandler.removeCallbacks(mProgressCallback);
        }
    }

    @Override
    public void onMediaPlayerPrepared() {
        if (mPlayer.isPlaying()) {
            mHandler.post(mProgressCallback);
        }
        loading.setVisibility(View.GONE);
        btnPlayToggle.setVisibility(View.VISIBLE);
    }

    /***** Click Events *****/
    @OnClick(R.id.now_playing_play)
    public void onPlayToggleAction(View view) {
        if (mPlayer == null) {
            mPresenter.setupPlaybackService();
            return;
        }

        if (mPlayer.isPlaying()) {
            mPlayer.pause();
        } else {
            if (!mPlayer.play()) {
                DialogFactory.createGenericErrorDialog(this, R.string.error_playing_episode);
            }
        }
    }

    @OnClick(R.id.now_playing_jump_foward)
    public void onJumpFowardClicked(View view) {
        int jump = mPlayer.getProgress() + BUTTON_JUMP_INTERVAL;
        mPlayer.seekTo(jump);
        seekBar.setProgress(getSeekBarProgress(jump));
        updateProgressTextWithProgress(jump);
    }

    @OnClick(R.id.now_playing_jump_back)
    public void onJumpBackClicked(View view) {
        int jump = mPlayer.getProgress() - BUTTON_JUMP_INTERVAL;
        mPlayer.seekTo(jump);
        seekBar.setProgress(getSeekBarProgress(jump));
        updateProgressTextWithProgress(jump);
    }

    private void initViews() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setHomeButtonEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            actionBar.setTitle("");
            actionBar.setHomeAsUpIndicator(R.drawable.ic_clear_24dp);
        }

        imagePodcastThumb.getLayoutParams().height = getPortraitArtworkHeight();

        seekBar.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
            @Override
            public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                if (fromUser) {
                    updateProgressTextWithProgress(value);
                    if (!mUserTouchingProgressBar) {
                        onStartTrackingTouch(seekBar);
                        onStopTrackingTouch(seekBar);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(DiscreteSeekBar seekBar) {
                mHandler.removeCallbacks(mProgressCallback);
            }

            @Override
            public void onStopTrackingTouch(DiscreteSeekBar seekBar) {
                mUserTouchingProgressBar = false;

                if (mPlayer != null) {
                    mPlayer.seekTo(getDuration(seekBar.getProgress()));
                    if (mPlayer.isPlaying()) {
                        mHandler.removeCallbacks(mProgressCallback);
                        mHandler.post(mProgressCallback);
                    }
                }
            }
        });

        mProgressCallback = new Runnable() {
            @Override
            public void run() {
                if (mPlayer.isPlaying()) {
                    int progress = getSeekBarProgress(mPlayer.getProgress());
                    updateProgressTextWithProgress(mPlayer.getProgress());
                    seekBar.setProgress(progress);
                    mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL);
                }
            }
        };
    }

    private void playPodcast(Episode episode) {
        loading.setVisibility(View.VISIBLE);
        btnPlayToggle.setVisibility(View.GONE);

        seekBar.setProgress(0);
        if (!mPlayer.play(episode)) {
            DialogFactory.createGenericErrorDialog(this, R.string.error_playing_episode);
        }
    }

    private int getSeekBarProgress(int progress) {
        int seekbarProgress = (int) (seekBar.getMax() * ((float) progress
                / (float) getCurrentEpisodeDurationInMilisecons()));
        if (seekbarProgress >= 1 && seekbarProgress <= seekBar.getMax()) {
            return seekbarProgress;
        }
        return 1;
    }

    private void updatePlayToggle(boolean playing) {
        btnPlayToggle.setImageState(null, true);
        if (playing) {
            btnPlayToggle.setImageState(STATE_SET_PAUSE, true);
        } else {
            btnPlayToggle.setImageState(STATE_SET_PLAY, true);
        }
    }

    private int getPortraitArtworkHeight() {
        int reservedHeight = (int) getResources().getDimension(R.dimen.player_frame_peek);

        // Default to a square view, so set the height equal to the width
        //noinspection SuspiciousNameCombination
        int preferredHeight = getResources().getDisplayMetrics().widthPixels;
        int maxHeight = getResources().getDisplayMetrics().heightPixels - reservedHeight;

        return Math.min(preferredHeight, maxHeight);
    }

    private int getCurrentEpisodeDurationInMilisecons() {
        return mPlayingEpisode != null ? mPlayingEpisode.getDuration() : 0;
    }

    private void updateProgressTextWithProgress(int progress) {
        textProgress.setText(StringUtils.formatFromMilliseconds(progress));
    }

    private int getDuration(int progress) {
        return (int) (getCurrentEpisodeDurationInMilisecons() * ((float) progress / seekBar.getMax()));
    }

    private void setupSeekbarNumericTransformer() {
        seekBar.setNumericTransformer(new DiscreteSeekBar.NumericTransformer() {
            @Override
            public int transform(int value) {
                return 0;
            }

            @Override
            public String transformToString(int value) {
                return StringUtils.formatFromMilliseconds(mPlayer.getProgress());
            }

            @Override
            public boolean useStringTransform() {
                return true;
            }
        });
    }
}
