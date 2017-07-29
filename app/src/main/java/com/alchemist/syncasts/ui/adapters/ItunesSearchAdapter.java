package com.alchemist.syncasts.ui.adapters;

import android.app.SharedElementCallback;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ItunesSearchAdapter extends RecyclerView.Adapter<ItunesSearchAdapter.ItunesSearchHolder> {

    private List<ItunesPodcast> mItunesPodcasts;
    private Callback mCallback;

    @Inject
    public ItunesSearchAdapter() {
        mItunesPodcasts = new ArrayList<>();
    }

    @Override
    public ItunesSearchHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_itunes, parent, false);
        return new ItunesSearchHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ItunesSearchHolder holder, int position) {
        ItunesPodcast itunesPodcast = mItunesPodcasts.get(position);
        holder.setItunesPodcast(itunesPodcast);

        holder.podcastTitle.setText(itunesPodcast.getSubtitle());
        holder.podcastOwner.setText(itunesPodcast.getAuthor());

        int squareSide = ViewUtils.dpToPx(50);

        Picasso.with(holder.itemView.getContext())
                .load(itunesPodcast.getImgUrl())
                .resize(squareSide, squareSide)
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

    public void clear() {
        mItunesPodcasts.clear();
        notifyDataSetChanged();
    }

    public static SharedElementCallback createSharedElementReenterCallback(@NonNull Context context) {
        final String shotTransitionName = context.getString(R.string.transition_shot);
        final String shotBackgroundTransitionName =
                context.getString(R.string.transition_shot_background);
        return new SharedElementCallback() {

            /**
             * We're performing a slightly unusual shared element transition i.e. from one view
             * (image in the grid) to two views (the image & also the background of the details
             * view, to produce the expanded effect). After changing orientation, the transition
             * system seems unable to map both shared elements (only seems to map the shot, not
             * the background) so in this situation we manually map the background to the
             * same view.
             */
            @Override
            public void onMapSharedElements(List<String> names, Map<String, View> sharedElements) {
                if (sharedElements.size() != names.size()) {
                    final View sharedShot = sharedElements.get(shotTransitionName);
                    if (sharedShot != null) {
                        sharedElements.put(shotBackgroundTransitionName, sharedShot);
                    }
                }
            }
        };
    }

    class ItunesSearchHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_podcast_thumb) ImageView podcastThumb;
        @BindView(R.id.text_episode_title) TextView podcastTitle;
        @BindView(R.id.text_podcast_owner) TextView podcastOwner;

        private ItunesPodcast mItunesPodcast;

        public ItunesSearchHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.relativelayout)
        void onItemClicked() {
            if (mCallback != null) mCallback.onItunesPodcastClicked(mItunesPodcast);
        }

        public void setItunesPodcast(ItunesPodcast itunesPodcast) {
            this.mItunesPodcast = itunesPodcast;
        }
    }

    public interface Callback {

        void onItunesPodcastClicked(ItunesPodcast itunesPodcast);
    }
}
