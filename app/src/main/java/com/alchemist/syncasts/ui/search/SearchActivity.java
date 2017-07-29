package com.alchemist.syncasts.ui.search;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.TransitionRes;
import android.support.v4.app.SharedElementCallback;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.util.SparseArray;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.data.model.Podcast;
import com.alchemist.syncasts.ui.adapters.ItunesSearchAdapter;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.main.MainActivity;
import com.alchemist.syncasts.ui.podcastdetails.ItunesPodcastDetailsFragment;
import com.alchemist.syncasts.ui.views.CircularReveal;
import com.alchemist.syncasts.ui.views.ListItemDecoration;
import com.alchemist.syncasts.utils.Utils;
import com.alchemist.syncasts.utils.ViewUtils;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindDimen;
import butterknife.BindInt;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchActivity extends BaseActivity implements SearchMvpView, ItunesSearchAdapter.Callback,
        ItunesPodcastDetailsFragment.OnSubscribedPodcastListener {

    @BindView(R.id.searchback) ImageButton searchBack;
    @BindView(R.id.searchback_container) ViewGroup searchBackContainer;
    @BindView(R.id.search_view) SearchView searchView;
    @BindView(R.id.search_background) View searchBackground;
    @BindView(android.R.id.empty) ProgressBar progress;
    @BindView(R.id.search_results) RecyclerView results;
    @BindView(R.id.container_foreground) ViewGroup container;
    @BindView(R.id.search_toolbar) ViewGroup searchToolbar;
    @BindView(R.id.results_container) ViewGroup resultsContainer;
    @BindView(R.id.scrim) View scrim;
    @BindView(R.id.results_scrim) View resultsScrim;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingLayout;

    @BindInt(R.integer.num_columns) int columns;
    @BindDimen(R.dimen.app_bar_elevation) float appBarElevation;

    @Inject SearchPresenter mPresenter;
    @Inject ItunesSearchAdapter adapter;

    private TextView mNoResults;
    private SparseArray<Transition> mTransitions = new SparseArray<>();
    private ItunesPodcastDetailsFragment mItunesPodcastDetailsFragment;

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SearchActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_search);
        ButterKnife.bind(this);
        mPresenter.attachView(this);
        adapter.setCallback(this);

        mItunesPodcastDetailsFragment = (ItunesPodcastDetailsFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_itunes_podcast_details);

        initializeSearchView();
        setExitSharedElementCallback(ItunesSearchAdapter.createSharedElementReenterCallback(this));
        results.setAdapter(adapter);
        results.setLayoutManager(new LinearLayoutManager(this));
        results.addItemDecoration(new ListItemDecoration(this, true));
        results.setHasFixedSize(true);

        slidingLayout.setFadeOnClickListener(v -> slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN));

        setupTransitions();
        onNewIntent(getIntent());
    }

    @Override
    protected void onPause() {
        // needed to suppress the default window animation when closing the activity
        overridePendingTransition(0, 0);
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public void onEnterAnimationComplete() {
        // focus the search view once the enter transition finishes
        searchView.requestFocus();
        Utils.showIme(searchView);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (intent.hasExtra(SearchManager.QUERY)) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            if (!TextUtils.isEmpty(query)) {
                searchView.setQuery(query, false);
                searchFor(query);
            }
        }
    }

    @Override
    public void onBackPressed() {
        dismiss();
    }

    @Override
    public void setSearchResults(List<ItunesPodcast> podcasts) {
        if (podcasts != null && podcasts.size() > 0) {
            if (results.getVisibility() != View.VISIBLE) {
                TransitionManager.beginDelayedTransition(
                        container, getTransition(R.transition.search_show_results));
                progress.setVisibility(View.GONE);
                results.setVisibility(View.VISIBLE);
            }
            adapter.setItunesPodcasts(podcasts);
        } else {
            TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
            progress.setVisibility(View.GONE);
            setNoResultsVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onItunesPodcastClicked(ItunesPodcast itunesPodcast) {
        mItunesPodcastDetailsFragment.setItunesPodcast(itunesPodcast);

        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    @Override
    public void onSubscribedSelected(Podcast podcast) {
        setResult(RESULT_OK, MainActivity.getStartIntent(this, podcast.getFeedUrl()));
        dismiss();
    }

    @OnClick({ R.id.scrim, R.id.searchback })
    public void dismiss() {
        searchBack.setBackground(null);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        finishAfterTransition();
    }

    private void setupTransitions() {
        setEnterSharedElementCallback(new SharedElementCallback() {
            @Override
            public void onSharedElementStart(
                    List<String> sharedElementNames,
                    List<View> sharedElements,
                    List<View> sharedElementSnapshots) {
                if (sharedElements != null && !sharedElements.isEmpty()) {
                    View searchIcon = sharedElements.get(0);
                    if (searchIcon.getId() != R.id.searchback) return;
                    int centerX = (searchIcon.getLeft() + searchIcon.getRight()) / 2;
                    CircularReveal hideResults = (CircularReveal) Utils.findTransition(
                            (TransitionSet) getWindow().getReturnTransition(),
                            CircularReveal.class, R.id.results_container);
                    if (hideResults != null) {
                        hideResults.setCenter(new Point(centerX, 0));
                    }
                }
            }
        });
    }

    private void initializeSearchView() {
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchFor(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                if (TextUtils.isEmpty(query)) {
                    clearResults();
                }
                return true;
            }
        });
    }

    private void searchFor(String query) {
        clearResults();
        progress.setVisibility(View.VISIBLE);
        Utils.hideIme(searchView);
        searchView.clearFocus();
        mPresenter.searchFor(query);
    }

    private void clearResults() {
        TransitionManager.beginDelayedTransition(container, getTransition(R.transition.auto));
        adapter.clear();
        results.setVisibility(View.GONE);
        progress.setVisibility(View.GONE);
        resultsScrim.setVisibility(View.GONE);
        setNoResultsVisibility(View.GONE);
    }

    private void setNoResultsVisibility(int visibility) {
        if (visibility == View.VISIBLE) {
            if (mNoResults == null) {
                mNoResults = (TextView) ((ViewStub)
                        findViewById(R.id.stub_no_search_results)).inflate();
                ViewUtils.setTextViewDrawableColor(this, mNoResults, R.color.primary_text);
                mNoResults.setOnClickListener(v -> {
                    searchView.setQuery("", false);
                    searchView.requestFocus();
                    Utils.showIme(searchView);
                });
            }
            String message = String.format(
                    getString(R.string.message_no_search_results), searchView.getQuery().toString());
            SpannableStringBuilder ssb = new SpannableStringBuilder(message);
            ssb.setSpan(new StyleSpan(Typeface.ITALIC),
                    message.indexOf('“') + 1,
                    message.length() - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            mNoResults.setText(ssb);
        }
        if (mNoResults != null) {
            mNoResults.setVisibility(visibility);
        }
    }

    private Transition getTransition(@TransitionRes int transitionId) {
        Transition transition = mTransitions.get(transitionId);
        if (transition == null) {
            transition = TransitionInflater.from(this).inflateTransition(transitionId);
            mTransitions.put(transitionId, transition);
        }
        return transition;
    }
}
