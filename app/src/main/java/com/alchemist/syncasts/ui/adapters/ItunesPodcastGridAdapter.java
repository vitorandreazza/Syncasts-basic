package com.alchemist.syncasts.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ItunesPodcastGridAdapter extends RecyclerView.Adapter<ItunesPodcastGridAdapter.ItunesPodcastHolder> {

    private List<ItunesPodcast> mItunesPodcasts;
    private Callback mCallback;
    private int mSquareSide, mNumColumns;

    @Inject
    public ItunesPodcastGridAdapter(int numColumns) {
        mItunesPodcasts = new ArrayList<>();
        mNumColumns = numColumns;
    }

    @Override
    public ItunesPodcastHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_podcast, parent, false);
        return new ItunesPodcastHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItunesPodcastHolder holder, int position) {
        if (mSquareSide == 0) {
            int width = ViewUtils.screenWidthInPx(holder.itemView.getContext());
            mSquareSide = Math.round(width / mNumColumns);
        }

        ItunesPodcast itunesPodcast = mItunesPodcasts.get(position);
        holder.setItunesPodcast(itunesPodcast);
        Picasso.with(holder.itemView.getContext())
                .load(itunesPodcast.getImgUrl())
                .resize(mSquareSide, mSquareSide)
                .centerCrop()
                .into(holder.podcastThumb);
        holder.podcastThumb.setContentDescription(itunesPodcast.getSubtitle());
    }

    @Override
    public int getItemCount() {
        return mItunesPodcasts.size();
    }

    public void setCallback(Callback callback) {
        mCallback = callback;
    }

    public void setItunesPodcasts(List<ItunesPodcast> podcasts) {
        mItunesPodcasts = podcasts;
        notifyDataSetChanged();
    }

    class ItunesPodcastHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_podcast_thumb) ImageView podcastThumb;
        private ItunesPodcast mItunesPodcast;

        ItunesPodcastHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.image_podcast_thumb)
        void onItemClicked() {
            if (mCallback != null) mCallback.onItunesPodcastClicked(mItunesPodcast);
        }

        void setItunesPodcast(ItunesPodcast itunesPodcast) {
            this.mItunesPodcast = itunesPodcast;
        }
    }

    public interface Callback {

        void onItunesPodcastClicked(ItunesPodcast itunesPodcast);
    }
}
