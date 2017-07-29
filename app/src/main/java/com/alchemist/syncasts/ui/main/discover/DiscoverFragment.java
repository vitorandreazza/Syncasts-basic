package com.alchemist.syncasts.ui.main.discover;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.adapters.ItunesPodcastGridAdapter;
import com.alchemist.syncasts.ui.views.GridItemDecoration;
import com.alchemist.syncasts.ui.views.ListItemDecoration;
import com.alchemist.syncasts.utils.Utils;
import com.alchemist.syncasts.utils.ViewUtils;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindColor;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class DiscoverFragment extends Fragment implements DiscoverMvpView, ItunesPodcastGridAdapter.Callback {

    @BindView(R.id.recyclerview) RecyclerView recyclerView;
    @BindView(android.R.id.empty) ProgressBar progressBar;
    @BindView(R.id.text_error) TextView textError;
    @BindView(R.id.button_try_connect) Button btnTryConnect;

    @BindInt(R.integer.num_columns) int columns;

    @BindColor(R.color.secondary_text) int secondaryText;

    @ColorInt int accent;
    @Inject DiscoverPresenter mPresenter;

    private ItunesPodcastGridAdapter mPodcastAdapter;
    private Unbinder mUnbinder;
    private ListItemDecoration mListItemDecoration;
    private GridItemDecoration mGriItemDecoration;
    private OnItunesPodcastSelectedListener mCallback;

    public static DiscoverFragment newInstance() {
        Bundle args = new Bundle();
        DiscoverFragment fragment = new DiscoverFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        ((BaseActivity) getActivity()).activityComponent().inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_discover, container, false);
        mUnbinder = ButterKnife.bind(this, root);
        mPresenter.attachView(this);

        mPodcastAdapter = new ItunesPodcastGridAdapter(columns);
        mPodcastAdapter.setCallback(this);

        initViews();
        loadTopPodcasts();

        return root;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mCallback = (OnItunesPodcastSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnItunesPodcastSelectedListener");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mPresenter.detachView();
        mUnbinder.unbind();
    }

    /***** MVP View methods implementation *****/
    @Override
    public void setAdapterItems(List<ItunesPodcast> podcasts) {
        mPodcastAdapter.setItunesPodcasts(podcasts);
    }

    @Override
    public void showError() {
        if (!Utils.isNetworkConnected(getContext())) {
            setLoadingIndicator(false);
            setErrorViews(getString(R.string.error_no_network), View.VISIBLE);
        } else {
            setErrorViews(null, View.GONE);
            setLoadingIndicator(true);
            loadTopPodcasts();
        }
    }

    @Override
    public void setLoadingIndicator(boolean visible) {
        if (visible) {
            progressBar.setVisibility(View.VISIBLE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    /***** Adapter Callback *****/
    @Override
    public void onItunesPodcastClicked(ItunesPodcast itunesPodcast) {
        mCallback.onItunesPodcastSelected(itunesPodcast);
    }

    @OnClick(R.id.button_try_connect)
    public void retryConnection(View v) {
        showError();
    }

    public void loadTopPodcasts() {
        String country = mPresenter.getPreferenceCountry();
        mPresenter.loadTopPodcasts(country);
    }

    private void initViews() {
        accent = ViewUtils.getAttribute(getContext(), R.attr.colorAccent);

        mListItemDecoration = new ListItemDecoration(getActivity(), false);
        mGriItemDecoration = new GridItemDecoration(getContext(), R.dimen.grid_space);

        recyclerView.removeItemDecoration(mListItemDecoration);
        recyclerView.removeItemDecoration(mGriItemDecoration);
        recyclerView.setAdapter(mPodcastAdapter);
        GridLayoutManager layoutManager = new GridLayoutManager(getContext(), columns);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(mGriItemDecoration);
    }

    private void setErrorViews(String message, int visibility) {
        textError.setText(message);
        textError.setVisibility(visibility);
        btnTryConnect.setVisibility(visibility);
    }

    public interface OnItunesPodcastSelectedListener {

        void onItunesPodcastSelected(ItunesPodcast itunesPodcast);
    }
}
