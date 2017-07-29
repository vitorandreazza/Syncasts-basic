package com.alchemist.syncasts.ui.adapters;

import android.support.annotation.Nullable;

import java.util.List;

import eu.davidea.flexibleadapter.FlexibleAdapter;

public class EpisodeFlexibleAdapter extends FlexibleAdapter<ExpandableHeaderItem> {

    public final OnPlayClickListener mPlayClickListener;
    public OnDownloadClickListener mDownloadClickListener;

    public EpisodeFlexibleAdapter(@Nullable List<ExpandableHeaderItem> items,
                                  @Nullable Object listeners,
                                  boolean stableIds,
                                  OnPlayClickListener playClickListener) {
        super(items, listeners, stableIds);
        mPlayClickListener = playClickListener;
    }

    public EpisodeFlexibleAdapter(@Nullable List<ExpandableHeaderItem> items,
                                  @Nullable Object listeners,
                                  boolean stableIds,
                                  OnPlayClickListener playClickListener,
                                  OnDownloadClickListener downloadClickListener) {
        this(items, listeners, stableIds, playClickListener);
        mDownloadClickListener = downloadClickListener;
    }

    public void setDownloadClickListener(OnDownloadClickListener downloadClickListener) {
        mDownloadClickListener = downloadClickListener;
    }

    public interface OnPlayClickListener {

        void onPlayClick(int position);
    }

    public interface OnDownloadClickListener {

        void onCancelDownloadClick(long downloadId);
    }
}
