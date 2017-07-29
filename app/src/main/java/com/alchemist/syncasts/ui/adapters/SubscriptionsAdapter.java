package com.alchemist.syncasts.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SubscriptionsAdapter extends RecyclerView.Adapter<SubscriptionsAdapter.SubscriptionsHolder> {

    private List<Podcast> mList;
    private SubscriptionsAdapter.Callback mCallback;
    private int mSquareSide, mNumColumns;

    @Inject
    public SubscriptionsAdapter(int numColumns) {
        mList = new ArrayList<>();
        mNumColumns = numColumns;
    }

    @Override
    public SubscriptionsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_podcast, parent, false);
        return new SubscriptionsHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final SubscriptionsHolder holder, int position) {
        if (mSquareSide == 0) {
            int width = ViewUtils.screenWidthInPx(holder.itemView.getContext());
            mSquareSide = Math.round(width / mNumColumns);
        }
        final Podcast podcast = mList.get(position);
        holder.setPodcast(podcast);

        int numUnplayedEpisodes = mCallback.countUnplayedEpisodes(podcast);
        holder.showUnplayedEpisodesFlag(numUnplayedEpisodes);

        Picasso.with(holder.itemView.getContext())
                .load(podcast.getImageLink())
                .resize(mSquareSide, mSquareSide)
                .centerCrop()
                .into(holder.imageThumb);
        holder.imageThumb.setContentDescription(podcast.getTitle());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setCallback(SubscriptionsAdapter.Callback callback) {
        mCallback = callback;
    }

    public void setPodcasts(List<Podcast> podcasts) {
        mList = podcasts;
        notifyDataSetChanged();
    }

    class SubscriptionsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_podcast_thumb) ImageView imageThumb;
        @BindView(R.id.triangle_unplayed) TextView triangle;
        @BindView(R.id.text_num_unplayed) TextView numUnplayed;
        private Podcast mPodcast;

        public SubscriptionsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.image_podcast_thumb)
        public void onItemClick(View v) {
            mCallback.onPodcastClicked(mPodcast);
        }

        public void setPodcast(Podcast podcast) {
            mPodcast = podcast;
        }

        private void showUnplayedEpisodesFlag(int numUnplayedEpisodes) {
            if (numUnplayedEpisodes > 0) {
                triangle.setVisibility(View.VISIBLE);
                numUnplayed.setText(String.valueOf(numUnplayedEpisodes));
                numUnplayed.setVisibility(View.VISIBLE);
            } else {
                triangle.setVisibility(View.GONE);
//                numUnplayed.setText("");
                numUnplayed.setVisibility(View.GONE);
            }
        }
    }

    public interface Callback {

        void onPodcastClicked(Podcast podcast);

        int countUnplayedEpisodes(Podcast podcast);
    }
}
