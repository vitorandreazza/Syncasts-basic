package com.alchemist.syncasts.ui.feedviewer;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.util.LongSparseArray;
import android.support.v4.util.Pair;
import android.support.v7.view.ActionMode;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.format.Formatter;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.BasicModel;
import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.ui.adapters.EpisodeFlexibleAdapter;
import com.alchemist.syncasts.ui.adapters.EpisodeItem;
import com.alchemist.syncasts.ui.adapters.ExpandableHeaderItem;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.player.PlayerActivity;
import com.alchemist.syncasts.ui.views.ListItemDecoration;
import com.alchemist.syncasts.utils.StringUtils;
import com.alchemist.syncasts.utils.ViewUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.SelectableAdapter;
import eu.davidea.flexibleadapter.common.SmoothScrollLinearLayoutManager;
import eu.davidea.flexibleadapter.helpers.ActionModeHelper;
import eu.davidea.flexibleadapter.items.IFlexible;
import timber.log.Timber;

@SuppressWarnings("ConstantConditions")
public class FeedViewerActivity extends BaseActivity implements
        FeedViewerMvpView, ActionMode.Callback, FlexibleAdapter.OnItemClickListener,
        FlexibleAdapter.OnItemLongClickListener, EpisodeFlexibleAdapter.OnPlayClickListener,
        EpisodeFlexibleAdapter.OnDownloadClickListener, SlidingUpPanelLayout.PanelSlideListener {

    private static final String EXTRA_FEED_URL = "com.alchemist.syncasts.ui.feedViewer.FeedViewerActivity.EXTRA_FEED_URL";
    private static final String POSITION_KEY = "POSITION_KEY";
    private static final String EPISODE_KEY = "EPISODE_KEY";
    private static final String EPISODES_DOWNLOADING_KEY = "EPISODES_DOWNLOADING_KEY";
    private static final long UPDATE_PROGRESS_INTERVAL = 1000;

    @BindView(R.id.appbar) AppBarLayout appBarLayout;
    @BindView(R.id.collapsing) CollapsingToolbarLayout collapsingToolbarLayout;
    @BindView(R.id.image_podcast_thumb) ImageView podcastThumb;
    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.sliding_toolbar) Toolbar slidingToolbar;
    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(R.id.loading) ProgressBar loading;
    @BindView(R.id.progress_size) ProgressBar loadingSize;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingLayout;
    @BindView(R.id.drag_panel) CoordinatorLayout dragPanel;
    @BindView(R.id.episode_title) TextView episodeTitle;
    @BindView(R.id.text_episode_date) TextView episodeDate;
    @BindView(R.id.text_episode_time_size) TextView episodeTimeSize;
    @BindView(R.id.text_episode_description) TextView episodeDescription;
    @BindView(R.id.download_episode) TextView downloadEpisode;
    @BindView(R.id.floating_play) FloatingActionButton floatingPlay;

    @BindString(R.string.action_played) String strHeaderPlayed;
    @BindString(R.string.action_unplayed) String strHeaderUnplayed;
    @BindString(R.string.label_episodes_count) String strHeaderEpisodeCount;

    @Inject FeedViewerPresenter mPresenter;

    private Podcast mPodcast;
    private Episode mEpisode;
    private ActionModeHelper mActionModeHelper;
    private Handler mHandler = new Handler();
    private DownloadManager mDownloadManager;
    private HashMap<Long, Episode> mEpisodesDownloading = new HashMap<>();
    private LongSparseArray<Pair<EpisodeItem, Runnable>> mDownloadsRunnable = new LongSparseArray<>();
    private BroadcastReceiver mDownloadManagerReceiver = new DownloadManagerReceiver();
    private EpisodeFlexibleAdapter mAdapter;
    private int mPosition, mItemLongClickPosition;
    private ExpandableHeaderItem mHeaderUnplayed, mHeaderPlayed;

    public static Intent getStartIntent(Context context, String feedUrl) {
        Intent intent = new Intent(context, FeedViewerActivity.class);
        intent.putExtra(EXTRA_FEED_URL, feedUrl);
        return intent;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_feed_viewer);
        ButterKnife.bind(this);
        mPresenter.attachView(this);

        initViews();

        mDownloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        String feedUrl = getIntent().getStringExtra(EXTRA_FEED_URL);
        mPodcast = mPresenter.getPodcast(feedUrl);
        initPodcastViews(mPodcast);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mEpisode != null) {
            outState.putInt(POSITION_KEY, mPosition);
            outState.putParcelable(EPISODE_KEY, mEpisode);
        }
        mAdapter.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState != null) {
            mEpisode = savedInstanceState.getParcelable(EPISODE_KEY);
            if (mEpisode != null) {
                mPosition = savedInstanceState.getInt(POSITION_KEY);
                initEpisodeDetails();
            }
            mAdapter.onRestoreInstanceState(savedInstanceState);
            mActionModeHelper.restoreSelection(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        List<Episode> episodes = mPresenter.getEpisodes(mPodcast);
        loading.setVisibility(View.VISIBLE);
        if (episodes.isEmpty()) {
            //Parse feed if doesn't has episodes in database
            mPresenter.parseAndLoadEpisodes(mPodcast.getFeedUrl(), 0);
        } else {
            setEpisodes(episodes);
        }

        for (int i = 0; i < mDownloadsRunnable.size(); i++) {
            mHandler.post(mDownloadsRunnable.valueAt(i).second);
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        intentFilter.addAction(DownloadManager.ACTION_NOTIFICATION_CLICKED);
        registerReceiver(mDownloadManagerReceiver, intentFilter);
    }

    @Override
    protected void onStop() {
        if (!mEpisodesDownloading.isEmpty()) {
            try {
                writeObject(EPISODES_DOWNLOADING_KEY, mEpisodesDownloading);
            } catch (IOException e) {
                Timber.e("Caching error of downloading episodes: " + e);
            }
        }
        for (int i = 0; i < mDownloadsRunnable.size(); i++) {
            mHandler.removeCallbacks(mDownloadsRunnable.valueAt(i).second);
        }
        unregisterReceiver(mDownloadManagerReceiver);
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    /***** MVP View methods implementation *****/
    @Override
    public void showEpisodeTimeSize(int duration, long size) {
        loadingSize.setVisibility(View.GONE);
        episodeTimeSize.setVisibility(View.VISIBLE);
        String formatedSize = Formatter.formatShortFileSize(this, size);
        episodeTimeSize.setText(StringUtils.formatEpisodeTimeSize(this, duration, formatedSize));
    }

    @Override
    public void setEpisodes(List<Episode> episodes) {
        List<ExpandableHeaderItem> headers = createHeaders(episodes);
        initAdapter(headers);
    }

    @Override
    public void setLoadingIndicator(boolean active) {
        if (active) {
            loading.setVisibility(View.VISIBLE);
        } else {
            loading.setVisibility(View.GONE);
        }
    }

    /***** Callbacks *****/
    @Override
    public boolean onItemClick(int position) {
        if (mAdapter.getMode() != SelectableAdapter.Mode.IDLE && mActionModeHelper != null) {
            return mActionModeHelper.onClick(position);
        } else {
            IFlexible flexibleItem = mAdapter.getItem(position);
            if (flexibleItem instanceof EpisodeItem) {
                EpisodeItem episodeItem = (EpisodeItem) flexibleItem;
                mPosition = position;
                mEpisode = episodeItem.getModel();
                initEpisodeDetails();
            }
            return false;
        }
    }

    @Override
    public void onItemLongClick(int position) {
        mItemLongClickPosition = position;
        mActionModeHelper.onLongClick(this, position);
    }

    @Override
    public void onPlayClick(int position) {
        IFlexible flexibleItem = mAdapter.getItem(position);
        if (flexibleItem instanceof EpisodeItem) {
            EpisodeItem episodeItem = (EpisodeItem) flexibleItem;
            Episode episodeClicked = episodeItem.getModel();
            startActivity(PlayerActivity.getStartIntent(this, episodeClicked));
        }
    }

    @Override
    public void onCancelDownloadClick(long downloadId) {
        mDownloadManager.remove(downloadId);
    }

    @Override
    public void onPanelSlide(View panel, float slideOffset) {
    }

    @Override
    public void onPanelStateChanged(View panel,
                                    SlidingUpPanelLayout.PanelState previousState,
                                    SlidingUpPanelLayout.PanelState newState) {
        if (newState == SlidingUpPanelLayout.PanelState.HIDDEN
                || newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            mPosition = 0;
            mEpisode = null;
        }
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuItem itemSelectAll = menu.findItem(R.id.action_select_all);
        if (itemSelectAll != null) {
            Drawable drawable = ViewUtils.tintDrawable(this, itemSelectAll.getIcon(), R.color.primary_text);
            itemSelectAll.setIcon(drawable);
        }

        getWindow().setStatusBarColor(ViewUtils.getAttribute(this, R.attr.colorAccent));

        IFlexible flexibleItem = mAdapter.getItem(mItemLongClickPosition);
        if (flexibleItem instanceof EpisodeItem) {
            EpisodeItem episodeItem = (EpisodeItem) flexibleItem;
            Drawable drawable;
            MenuItem menuItem;
            if (episodeItem.getModel().isPlayed()) {
                menuItem = menu.add(Menu.NONE, R.id.action_mark_unplayed, 1, R.string.action_unplayed);
                drawable = getDrawable(R.drawable.ic_unplayed_24dp);
            } else {
                menuItem = menu.add(Menu.NONE, R.id.action_mark_played, 1, R.string.action_played);
                drawable = getDrawable(R.drawable.ic_checkmark_white);
            }
            Drawable newDrawable = ViewUtils.tintDrawable(this, drawable, R.color.primary_text);
            menuItem.setIcon(newDrawable);
        }
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_all:
                mAdapter.selectAll();
                mActionModeHelper.updateContextTitle(mAdapter.getSelectedItemCount());
                return true;
            case R.id.action_mark_played:
                List<ExpandableHeaderItem> headers = markSelectedEpisodes(true);
                mAdapter.updateDataSet(headers, true);
                return false;
            case R.id.action_mark_unplayed:
                List<ExpandableHeaderItem> headers2 = markSelectedEpisodes(false);
                mAdapter.updateDataSet(headers2, true);
                return false;
            default:
                return false;
        }
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void initAdapter(List<ExpandableHeaderItem> headers) {
        if (mAdapter == null) {
            mAdapter = new EpisodeFlexibleAdapter(headers, this, true, this, this);
            mAdapter.setMode(SelectableAdapter.Mode.IDLE);
            initializeActionModeHelper(SelectableAdapter.Mode.IDLE);
            mAdapter.expandItemsAtStartUp()
                    .setAutoCollapseOnExpand(false)
                    .setAutoScrollOnExpand(true)
                    .setAnimateToLimit(Integer.MAX_VALUE)
                    .setNotifyMoveOfFilteredItems(true)
                    .setAnimationOnReverseScrolling(true);
        } else {
            mAdapter.updateDataSet(headers);
        }
        recyclerView.setAdapter(mAdapter);
        loading.setVisibility(View.GONE);
    }

    private List<ExpandableHeaderItem> createHeaders(List<Episode> episodes) {
        HashMap<Long, Episode> episodesDownloading = null;
        if (mEpisodesDownloading.isEmpty()) {
            episodesDownloading = getDownloadingEpisodesCache();
            if (episodesDownloading != null) {
                mEpisodesDownloading = episodesDownloading;
            }
        }
        List<ExpandableHeaderItem> headers = new ArrayList<>();
        mHeaderUnplayed = new ExpandableHeaderItem();
        int i = 0;
        while (i < episodes.size() && !episodes.get(i).isPlayed()) {
            EpisodeItem item = new EpisodeItem(episodes.get(i), mHeaderUnplayed);
            mHeaderUnplayed.addSubItem(item);
            if (episodesDownloading != null) {
                long downloadId = episodeIsDownloading(episodes.get(i));
                if (downloadId != -1) {
                    Runnable runnable = createProgressRunnable(downloadId, -1, item);
                    mDownloadsRunnable.put(downloadId, new Pair<>(item, runnable));
                }
            }
            i++;
        }
        BasicModel unplayedModel = new BasicModel(strHeaderUnplayed, String.format(strHeaderEpisodeCount, i));
        mHeaderUnplayed.setModel(unplayedModel);

        BasicModel playedModel = new BasicModel(strHeaderPlayed, String.format(strHeaderEpisodeCount, episodes.size() - i));
        mHeaderPlayed = new ExpandableHeaderItem(playedModel);
        while (i < episodes.size()) {
            EpisodeItem item = new EpisodeItem(episodes.get(i), mHeaderPlayed);
            mHeaderPlayed.addSubItem(item);
            if (episodesDownloading != null) {
                long downloadId = episodeIsDownloading(episodes.get(i));
                if (downloadId != -1) {
                    Runnable runnable = createProgressRunnable(downloadId, -1, item);
                    mDownloadsRunnable.put(downloadId, new Pair<>(item, runnable));
                }
            }
            i++;
        }

        if (mHeaderUnplayed.getSubItems() != null) headers.add(mHeaderUnplayed);
        if (mHeaderPlayed.getSubItems() != null) headers.add(mHeaderPlayed);
        return headers;
    }

    private HashMap<Long, Episode> getDownloadingEpisodesCache() {
        try {
            HashMap<Long, Episode> episodesDownloading = (HashMap<Long, Episode>) readObject(EPISODES_DOWNLOADING_KEY);
            if (episodesDownloading != null) {
                File file = new File(getCacheDir(), EPISODES_DOWNLOADING_KEY);
                file.delete();
            }
            return episodesDownloading;
        } catch (IOException | ClassNotFoundException e) {
            Timber.e("Caching error of downloading episodes: " + e);
        }
        return null;
    }

    private void initPodcastViews(final Podcast podcast) {
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(podcast.getTitle());
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    isShow = false;
                }
            }
        });
        slidingToolbar.setTitle(podcast.getTitle());
        podcastThumb.setContentDescription(podcast.getTitle());

        Picasso.with(this)
                .load(mPodcast.getImageLink())
                .fit()
                .centerCrop()
                .into(podcastThumb);
    }

    @NonNull
    private List<ExpandableHeaderItem> markSelectedEpisodes(boolean played) {
        List<Integer> selectedPositions = mAdapter.getSelectedPositions();
        for (int position : selectedPositions) {
            IFlexible flexible = mAdapter.getItem(position);
            if (flexible instanceof EpisodeItem) {
                EpisodeItem episodeItem = (EpisodeItem) flexible;
                Episode episode = episodeItem.getModel();
                if (played) {
                    if (!episode.isPlayed()) {
                        episode.setPlayed(true);
                        mPresenter.updateEpisode(episode);
                        mHeaderUnplayed.removeSubItem(episodeItem);
                        mHeaderPlayed.addSubItem(episodeItem);
                        String strUnplayed = String.format(strHeaderEpisodeCount, mHeaderUnplayed.getSubItems().size());
                        String strPlayed = String.format(strHeaderEpisodeCount, mHeaderPlayed.getSubItems().size());
                        mHeaderUnplayed.getModel().setSubtitle(strUnplayed);
                        mHeaderPlayed.getModel().setSubtitle(strPlayed);
                    }
                } else {
                    if (episode.isPlayed()) {
                        episode.setPlayed(false);
                        mPresenter.updateEpisode(episode);
                        mHeaderPlayed.removeSubItem(episodeItem);
                        mHeaderUnplayed.addSubItem(episodeItem);
                        String strUnplayed = String.format(strHeaderEpisodeCount, mHeaderUnplayed.getSubItems().size());
                        String strPlayed = String.format(strHeaderEpisodeCount, mHeaderPlayed.getSubItems().size());
                        mHeaderUnplayed.getModel().setSubtitle(strUnplayed);
                        mHeaderPlayed.getModel().setSubtitle(strPlayed);
                    }
                }
            }
        }
        List<ExpandableHeaderItem> headers = new ArrayList<>();
        if (!mHeaderUnplayed.getSubItems().isEmpty()) headers.add(mHeaderUnplayed);
        if (!mHeaderPlayed.getSubItems().isEmpty()) headers.add(mHeaderPlayed);
        return headers;
    }

    private void initViews() {
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

            toolbar.setNavigationOnClickListener(view -> onBackPressed());
        }

        recyclerView.setLayoutManager(new SmoothScrollLinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new ListItemDecoration(this, false));

        loadingSize.getIndeterminateDrawable().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN);

        //Set status bar color to transparent
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        getWindow().setStatusBarColor(Color.TRANSPARENT);

        slidingLayout.setFadeOnClickListener(v -> slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN));
        slidingLayout.addPanelSlideListener(this);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        int padding = ViewUtils.getStatusBarHeight(this);
        dragPanel.setPadding(0, padding, 0, 0);
    }

    private void initializeActionModeHelper(@SelectableAdapter.Mode int mode) {
        mActionModeHelper = new ActionModeHelper(mAdapter, R.menu.menu_episode_selection, this) {
            @Override
            public void updateContextTitle(int count) {
                if (mActionMode != null) {
                    mActionMode.setTitle(count == 1 ?
                            getString(R.string.action_selected_one, Integer.toString(count)) :
                            getString(R.string.action_selected_many, Integer.toString(count)));
                }
            }
        }.withDefaultMode(mode);
    }

    private void initEpisodeDetails() {
        episodeTitle.setText(mEpisode.getTitle());
        episodeDate.setText(StringUtils.formatPubDate(mEpisode.getPubDate(), false));

        Pattern pattern = Pattern.compile("<[a-z][\\s\\S]*>");
        Matcher matcher = pattern.matcher(mEpisode.getDescription());
        if (matcher.find()) {
            episodeDescription.setText(Html.fromHtml(mEpisode.getDescription()));
        } else {
            episodeDescription.setText(mEpisode.getDescription());
        }

        setupDownloadButton();

        if (mEpisode.getLocalDir() != null &&
                new File(mEpisode.getLocalDir()).exists()) {
            floatingPlay.setImageResource(R.drawable.ic_play_filled_24dp);
        } else {
            floatingPlay.setImageResource(R.drawable.ic_play_unfilled_24dp);
        }

        floatingPlay.setOnClickListener(v -> {
            startActivity(PlayerActivity.getStartIntent(getBaseContext(), mEpisode));
        });

        int duration = 0;
        if (mEpisode.getDuration() != null) duration = mEpisode.getDuration();
        mPresenter.getEpisodeTimeSize(duration, mEpisode.getMediaUrl());

        episodeTimeSize.setVisibility(View.GONE);
        loadingSize.setVisibility(View.VISIBLE);

        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }


    private void downloadFile() {
        String localFileName = UUID.randomUUID().toString();
        String path = getExternalFilesDir("podcasts").getAbsolutePath();
        mEpisode.setLocalDir(path + "/" + localFileName + ".mp3");


        String podcastTitle = mEpisode.getPodcast().getTitle();
        String episodeTitle = mEpisode.getTitle();
        String mediaUrl = mEpisode.getMediaUrl();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(mediaUrl));
        request.setTitle(podcastTitle);
        request.setDescription(episodeTitle);
        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        if (isExternalStorageWritable()) {
            request.setDestinationInExternalFilesDir(this, "podcasts", localFileName + ".mp3");
        }
        final long downloadId = mDownloadManager.enqueue(request);
        initializeButtonAsCancel(downloadId);
        final EpisodeItem.EpisodeViewHolder holder = (EpisodeItem.EpisodeViewHolder)
                recyclerView.findViewHolderForAdapterPosition(mPosition);
        holder.setDownloading(downloadId);

        Runnable progressRunnable = createProgressRunnable(downloadId, mPosition, null);
        mEpisodesDownloading.put(downloadId, mEpisode);

        IFlexible item = mAdapter.getItem(mPosition);
        EpisodeItem episodeItem = (EpisodeItem) item;
        mDownloadsRunnable.append(downloadId, new Pair<>(episodeItem, progressRunnable));
        mHandler.post(progressRunnable);
    }

    @NonNull
    private Runnable createProgressRunnable(final long downloadId, final int position, final IFlexible flexible) {
        return new Runnable() {
            @Override
            public void run() {
                DownloadManager.Query query = new DownloadManager.Query();
                query.setFilterById(downloadId);

                try (Cursor cursor = mDownloadManager.query(query)) {
                    if (cursor.moveToFirst()) {
                        long bytesSoFar = cursor.getLong(cursor.getColumnIndex(
                                DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
                        long totalBytes = cursor.getLong(cursor.getColumnIndex(
                                DownloadManager.COLUMN_TOTAL_SIZE_BYTES));
                        long megabytesSoFar = (bytesSoFar / (1024 * 1024));
                        long totalMegabytes = (totalBytes / (1024 * 1024));
                        int holderPosition = position;
                        if (flexible != null) {
                            holderPosition = mAdapter.getGlobalPositionOf(flexible);
                        }
                        final EpisodeItem.EpisodeViewHolder holder = (EpisodeItem.EpisodeViewHolder)
                                recyclerView.findViewHolderForAdapterPosition(holderPosition);
                        if (holder != null) {
                            holder.setDownloading(downloadId);
                            holder.setDownloadProgress(megabytesSoFar, totalMegabytes);
                        }
                    }
                }
                mHandler.postDelayed(this, UPDATE_PROGRESS_INTERVAL);
            }
        };
    }

    private void deleteFile() {
        if (mEpisode.getLocalDir() != null) {
            File file = new File(mEpisode.getLocalDir());
            if (file.exists() && file.delete()) {
                Toast.makeText(this, R.string.message_deleted_episode, Toast.LENGTH_SHORT).show();
                final EpisodeItem.EpisodeViewHolder holder = (EpisodeItem.EpisodeViewHolder)
                        recyclerView.findViewHolderForAdapterPosition(mPosition);
                holder.getImgButtonPlay().setImageResource(R.drawable.ic_play_circle_unfilled_30dp);
                floatingPlay.setImageResource(R.drawable.ic_play_unfilled_24dp);
                initializeButtonAsDownload();
            }
        }
    }

    private boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        return Environment.MEDIA_MOUNTED.equals(state);
    }

    private long episodeIsDownloading(Episode episode) {
        for (Map.Entry<Long, Episode> entry : mEpisodesDownloading.entrySet()) {
            if (episode.equals(entry.getValue())) return entry.getKey();
        }
        return -1;
    }

    private void setupDownloadButton() {
        final long downloadId = episodeIsDownloading(mEpisode);
        if (downloadId == -1) {
            if (mEpisode.getLocalDir() != null && new File(mEpisode.getLocalDir()).exists()) {
                initializeButtonAsRemove();
            } else {
                initializeButtonAsDownload();
            }
        } else {
            initializeButtonAsCancel(downloadId);
        }
    }

    private void initializeButtonAsDownload() {
        downloadEpisode.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_file_download_24dp, 0, 0, 0);
        downloadEpisode.setText(R.string.action_download);
        downloadEpisode.setOnClickListener(v -> downloadFile());
    }

    private void initializeButtonAsCancel(final long downloadId) {
        downloadEpisode.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_cancel_24dp, 0, 0, 0);
        downloadEpisode.setText(R.string.action_cancel);
        downloadEpisode.setOnClickListener(v -> {
            mDownloadManager.remove(downloadId);
            initializeButtonAsDownload();
        });
    }

    private void initializeButtonAsRemove()  {
        downloadEpisode.setCompoundDrawablesWithIntrinsicBounds(
                R.drawable.ic_delete_24dp, 0, 0, 0);
        downloadEpisode.setText(R.string.action_delete);
        downloadEpisode.setOnClickListener(v -> deleteFile());
    }

    private void writeObject(String key, Object object) throws IOException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            File file = new File(getCacheDir(), key);
            if (!file.exists()) {
                file.createNewFile();
            }
            fos = new FileOutputStream(file);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(object);
        } finally {
            if (oos != null) oos.close();
            if (fos != null) fos.close();
        }
    }

    private Object readObject(String key) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            File file = new File(getCacheDir(), key);
            if (file.exists()) {
                fis = new FileInputStream(file);
                ois = new ObjectInputStream(fis);
                return ois.readObject();
            }
        } finally {
            if (fis != null) fis.close();
            if (ois != null) ois.close();
        }
        return null;
    }

    public class DownloadManagerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case DownloadManager.ACTION_DOWNLOAD_COMPLETE:
                    long downloadId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                    if (downloadId != -1) {
                        final Episode episode = mEpisodesDownloading.get(downloadId);
                        EpisodeItem item = mDownloadsRunnable.get(downloadId).first;
                        Runnable progressCallback = mDownloadsRunnable.get(downloadId).second;
                        int position = mAdapter.getGlobalPositionOf(item);
                        EpisodeItem.EpisodeViewHolder holder = (EpisodeItem.EpisodeViewHolder)
                                recyclerView.findViewHolderForAdapterPosition(position);
                        holder.setDownloadCompleted(episode);

                        DownloadManager.Query query = new DownloadManager.Query();
                        query.setFilterById(downloadId);

                        try (Cursor cursor = mDownloadManager.query(query)) {
                            if (cursor.moveToFirst()) {
                                int status = cursor.getInt(
                                        cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                                    Timber.d("ep=%s", episode);
                                    mPresenter.updateEpisode(episode);
                                    initializeButtonAsRemove();
                                } else {
                                    initializeButtonAsDownload();
                                }
                            }
                        }

                        mHandler.removeCallbacks(progressCallback);
                        mEpisodesDownloading.remove(downloadId);
                        mDownloadsRunnable.remove(downloadId);
                    } else {
                        Timber.w("Callback to id downloaded doesn't exists.");
                    }
                    break;
            }
        }
    }
}