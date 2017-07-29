package com.alchemist.syncasts.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.Episode;
import com.alchemist.syncasts.utils.StringUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import eu.davidea.flexibleadapter.FlexibleAdapter;
import eu.davidea.flexibleadapter.items.AbstractSectionableItem;
import eu.davidea.flexibleadapter.items.IHeader;
import eu.davidea.flexibleadapter.items.IHolder;
import eu.davidea.flexibleadapter.utils.DrawableUtils;
import eu.davidea.viewholders.FlexibleViewHolder;

public class EpisodeItem extends AbstractSectionableItem<EpisodeItem.EpisodeViewHolder, IHeader>
        implements IHolder<Episode>, Comparable<EpisodeItem> {

    private Episode mEpisode;
    private IHeader mHeader;

    public EpisodeItem(Episode episode, IHeader header) {
        super(header);
        mEpisode = episode;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof EpisodeItem) {
            EpisodeItem episodeItem = (EpisodeItem) o;
            return mEpisode.equals(episodeItem.getModel());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mEpisode.hashCode();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.item_episode;
    }

    @Override
    public EpisodeViewHolder createViewHolder(View view, FlexibleAdapter adapter) {
        return new EpisodeViewHolder(view, (EpisodeFlexibleAdapter) adapter);
    }

    @Override
    public void bindViewHolder(FlexibleAdapter adapter, EpisodeViewHolder holder, int position, List payloads) {
        String formatedPubDate = StringUtils.formatPubDate(mEpisode.getPubDate(), true);
        String duration = StringUtils.formatDurationInMinutes(mEpisode.getDuration());
        holder.episodeTitle.setText(mEpisode.getTitle());
        holder.episodeDate.setText(formatedPubDate);
        holder.episodeDuration.setText(duration);

        if (mEpisode.getLocalDir() != null && new File(mEpisode.getLocalDir()).exists()) {
            holder.imgButtonPlay.setImageResource(
                    R.drawable.ic_play_circle_filled_30dp);
        } else {
            holder.imgButtonPlay.setImageResource(
                    R.drawable.ic_play_circle_unfilled_30dp);
        }

        holder.itemView.setActivated(adapter.isSelected(position));
        Context context = holder.itemView.getContext();
        int background = context.getResources().getColor(R.color.background);
        int backgroundSelected = context.getResources().getColor(R.color.background_selected);
        Drawable drawable = DrawableUtils.getSelectableBackgroundCompat(background, backgroundSelected, Color.WHITE);
        DrawableUtils.setBackgroundCompat(holder.itemView, drawable);
    }

    @Override
    public Episode getModel() {
        return mEpisode;
    }

    @Override
    public IHeader getHeader() {
        return mHeader;
    }

    @Override
    public void setHeader(IHeader header) {
        mHeader = header;
    }

    @Override
    public int compareTo(@NonNull EpisodeItem o) {
        return o.getModel().getPubDate().compareTo(getModel().getPubDate());
    }

    public class EpisodeViewHolder extends FlexibleViewHolder {

        @BindView(R.id.text_episode_title)TextView episodeTitle;
        @BindView(R.id.text_episode_date) TextView episodeDate;
        @BindView(R.id.text_episode_duration) TextView episodeDuration;
        @BindView(R.id.button_play) ImageView imgButtonPlay;
        @BindView(R.id.linear_play_group) LinearLayout linearPlayGroup;

        private EpisodeFlexibleAdapter mAdapter;

        public EpisodeViewHolder(View view, EpisodeFlexibleAdapter adapter) {
            super(view, adapter);
            ButterKnife.bind(this, itemView);
            mAdapter = adapter;
        }

        @OnClick(R.id.linear_play_group)
        public void onClickButtonPlay(View view) {
            mAdapter.mPlayClickListener.onPlayClick(getFlexibleAdapterPosition());
        }

        public void setDownloading(final long downloadId) {
            imgButtonPlay.setImageResource(R.drawable.ic_cancel_24dp);
            linearPlayGroup.setOnClickListener(v -> {
                if (mAdapter.mDownloadClickListener != null) {
                    mAdapter.mDownloadClickListener.onCancelDownloadClick(downloadId);
                }
            });
            linearPlayGroup.setOnLongClickListener(null);
        }

        public void setDownloadProgress(long megabytesSoFar, long totalMegabytes) {
            if (megabytesSoFar == 0 || megabytesSoFar <= totalMegabytes) {
                String progress = megabytesSoFar + "/" + totalMegabytes + "MB";
                episodeDuration.setText(progress);
            }
        }

        public void setDownloadCompleted(Episode episode) {
            String duration = StringUtils.formatDurationInMinutes(episode.getDuration());
            episodeDuration.setText(duration);

            if (episode.getLocalDir() != null && new File(episode.getLocalDir()).exists()) {
                imgButtonPlay.setImageResource(R.drawable.ic_play_circle_filled_30dp);
            } else {
                imgButtonPlay.setImageResource(R.drawable.ic_play_circle_unfilled_30dp);
            }

            linearPlayGroup.setOnClickListener(this::onClickButtonPlay);
        }

        public ImageView getImgButtonPlay() {
            return imgButtonPlay;
        }
    }
}
