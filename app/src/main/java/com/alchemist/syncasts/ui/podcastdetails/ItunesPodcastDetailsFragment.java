package com.alchemist.syncasts.ui.podcastdetails;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.feedviewer.FeedViewerActivity;
import com.alchemist.syncasts.ui.search.SearchActivity;
import com.alchemist.syncasts.ui.views.ProgressFloatingActionButton;
import com.alchemist.syncasts.utils.ViewUtils;
import com.squareup.picasso.Picasso;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class ItunesPodcastDetailsFragment extends Fragment implements ItunesPodcastDetailsMvpView {

    @BindView(R.id.image_podcast_thumb) ImageView imageThumb;
    @BindView(R.id.text_podcast_subtitle) TextView podcastSubtitle;
    @BindView(R.id.text_podcast_author) TextView podcastAuthor;
    @BindView(R.id.text_podcast_summary) TextView podcastSummary;
    @BindView(R.id.floating_podcast_subscribed) FloatingActionButton floatingPodcastSubscribed;
    @BindView(R.id.progress_subscribing) ProgressBar progressSubscribing;
    @BindView(R.id.container_podcast_subscribed) ProgressFloatingActionButton containerPodcastSubscribed;
    @BindView(R.id.drag_panel) CoordinatorLayout dragPanel;

    @Inject ItunesPodcastDetailsPresenter mPresenter;

    private Unbinder mUnbinder;
    private OnSubscribedPodcastListener mCallback;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_podcast_details, container, false);
        mPresenter.attachView(this);
        mUnbinder = ButterKnife.bind(this, root);

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof SearchActivity) {
            // This makes sure that the container activity has implemented
            // the callback interface. If not, it throws an exception
            try {
                mCallback = (OnSubscribedPodcastListener) activity;
            } catch (ClassCastException e) {
                throw new ClassCastException(activity.toString()
                        + " must implement OnSubscribedPodcastListener");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    @Override
    public void setLoading(boolean visible) {
        if (visible) {
            floatingPodcastSubscribed.setVisibility(View.GONE);
            progressSubscribing.setVisibility(View.VISIBLE);
        } else {
            floatingPodcastSubscribed.setVisibility(View.VISIBLE);
            progressSubscribing.setVisibility(View.GONE);
        }
    }

    @Override
    public void updatePodcastStatus(final Podcast podcast) {
        progressSubscribing.setVisibility(View.GONE);
        floatingPodcastSubscribed.setImageResource(R.drawable.ic_check_24dp);
        containerPodcastSubscribed.setOnClickListener(v -> {
            startActivity(FeedViewerActivity.getStartIntent(getContext(), podcast.getFeedUrl()));
        });
    }

    @Override
    public void setupSubscribed(final Podcast podcast) {
        floatingPodcastSubscribed.setImageResource(R.drawable.ic_check_24dp);
        if (mCallback != null) {
            containerPodcastSubscribed.setOnClickListener(v -> mCallback.onSubscribedSelected(podcast));
        } else {
            containerPodcastSubscribed.setOnClickListener(v -> {
                startActivity(FeedViewerActivity.getStartIntent(getContext(), podcast.getFeedUrl()));
            });
        }
    }

    @Override
    public void setupUnsubscribed(final ItunesPodcast itunesPodcast) {
        floatingPodcastSubscribed.setImageResource(R.drawable.ic_add_24dp);
        containerPodcastSubscribed.setOnClickListener(v -> {
            progressSubscribing.setVisibility(View.VISIBLE);
            mPresenter.subscribePodcast(itunesPodcast, 0);
        });
    }

    public void setItunesPodcast(ItunesPodcast itunesPodcast) {
        Picasso.with(getContext())
                .load(itunesPodcast.getImgUrl())
                .resize(ViewUtils.dpToPx(95), ViewUtils.dpToPx(95))
                .centerCrop()
                .into(imageThumb);

        podcastSubtitle.setText(itunesPodcast.getSubtitle());
        podcastAuthor.setText(itunesPodcast.getAuthor());
        podcastSummary.setText(itunesPodcast.getSummary());

        setLoading(true);
        mPresenter.initializeSubscriptionBtn(itunesPodcast);
    }

    public interface OnSubscribedPodcastListener {

        void onSubscribedSelected(Podcast podcast);
    }
}