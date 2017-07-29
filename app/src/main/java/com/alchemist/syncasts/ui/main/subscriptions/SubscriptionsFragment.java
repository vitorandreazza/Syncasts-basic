package com.alchemist.syncasts.ui.main.subscriptions;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.ui.adapters.SubscriptionsAdapter;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.feedviewer.FeedViewerActivity;
import com.alchemist.syncasts.ui.views.GridItemDecoration;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SubscriptionsFragment extends Fragment implements SubscriptionsMvpView, SubscriptionsAdapter.Callback {

    @BindView(R.id.recyclerview) RecyclerView recyclerView;

    @BindInt(R.integer.num_columns) int columns;

    @Inject SubscriptionsPresenter mPresenter;

    private Unbinder mUnbinder;
    private SubscriptionsAdapter mAdapter;

    public SubscriptionsFragment() {
    }

    public static SubscriptionsFragment newInstance() {
        SubscriptionsFragment fragment = new SubscriptionsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        mUnbinder = ButterKnife.bind(this, root);
        mPresenter.attachView(this);

        mAdapter = new SubscriptionsAdapter(columns);
        mAdapter.setCallback(this);

        recyclerView.addItemDecoration(new GridItemDecoration(getContext(), R.dimen.grid_space));
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void onStart() {
        super.onStart();
        mPresenter.loadSubscriptions();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    /***** MVP View methods implementation *****/
    @Override
    public void setPodcasts(List<Podcast> podcasts) {
        mAdapter.setPodcasts(podcasts);
    }

    /***** Adapter Callback *****/
    @Override
    public void onPodcastClicked(Podcast podcast) {
        startActivity(FeedViewerActivity.getStartIntent(getContext(), podcast.getFeedUrl()));
    }

    @Override
    public int countUnplayedEpisodes(Podcast podcast) {
        return mPresenter.countUnplayedEpisodes(podcast);
    }
}
