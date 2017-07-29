package com.alchemist.syncasts.ui.main;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.model.ItunesPodcast;
import com.alchemist.syncasts.ui.BaseActivity;
import com.alchemist.syncasts.ui.feedviewer.FeedViewerActivity;
import com.alchemist.syncasts.ui.main.discover.DiscoverFragment;
import com.alchemist.syncasts.ui.podcastdetails.ItunesPodcastDetailsFragment;
import com.alchemist.syncasts.ui.search.SearchActivity;
import com.alchemist.syncasts.ui.settings.SettingsActivity;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import java.util.TreeMap;

import javax.inject.Inject;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements MainMvpView, DiscoverFragment.OnItunesPodcastSelectedListener {

    public static final int RC_SEARCH = 0;
    private static final String EXTRA_FEED_URL = "com.alchemist.syncasts.ui.feedViewer.FeedViewerActivity.EXTRA_FEED_URL";

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.tablayout) TabLayout tabLayout;
    @BindView(R.id.viewpager) ViewPager viewPager;
    @BindView(R.id.sliding_layout) SlidingUpPanelLayout slidingLayout;
    @BindArray(R.array.countries) String[] countries;
    @BindArray(R.array.country_values) String[] countryValues;

    @Inject MainFragmentPagerAdapter mMainFragmentPagerAdapter;
    @Inject MainPresenter mPresenter;

    public static Intent getStartIntent(Context context, String feedUrl) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra(EXTRA_FEED_URL, feedUrl);
        return intent;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityComponent().inject(this);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mPresenter.attachView(this);

        initViews();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPresenter.detachView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_action_search:
                View searchMenuView = toolbar.findViewById(R.id.menu_action_search);
                Bundle options = ActivityOptions.makeSceneTransitionAnimation(this,
                        searchMenuView, getString(R.string.transition_search_back)).toBundle();
                startActivityForResult(SearchActivity.getStartIntent(this), RC_SEARCH, options);
                return true;
            case R.id.menu_action_countries:
                createCountriesDialog();
                return true;
            case R.id.menu_action_settings:
                startActivity(SettingsActivity.getStartIntent(this));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case RC_SEARCH:
                if (resultCode == RESULT_OK) {
                    String feedUrl = data.getStringExtra(EXTRA_FEED_URL);
                    startActivity(FeedViewerActivity.getStartIntent(this, feedUrl));
                }
                break;
        }
    }

    @Override
    public void onItunesPodcastSelected(ItunesPodcast itunesPodcast) {
        ItunesPodcastDetailsFragment fragment = (ItunesPodcastDetailsFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment_itunes_podcast_details);
        fragment.setItunesPodcast(itunesPodcast);
        slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.ANCHORED);
    }

    private void initViews() {
        setSupportActionBar(toolbar);

        viewPager.setAdapter(mMainFragmentPagerAdapter);
        tabLayout.setupWithViewPager(viewPager);

        slidingLayout.setFadeOnClickListener(v -> slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN));
    }

    private void createCountriesDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.action_change_country);
        final TreeMap<String, String> map = new TreeMap<>();
        if (countries.length == countryValues.length) {
            for (int index = 0; index < countries.length; index++) {
                map.put(countries[index], countryValues[index]);
            }
        }
        int countryIndex = getCountryIndex(countryValues);
        dialog.setSingleChoiceItems(map.keySet().toArray(new String[0]), countryIndex,
                (dialog1, which) -> {
                    String[] sortedCountries = map.keySet().toArray(new String[0]);
                    mPresenter.saveCountry(map.get(sortedCountries[which]));
                    DiscoverFragment discoverFragment = (DiscoverFragment) mMainFragmentPagerAdapter
                            .getRegisteredFragment(MainFragmentPagerAdapter.DISCOVER_FRAGMENT_POSITION);
                    if (discoverFragment != null) {
                        discoverFragment.loadTopPodcasts();
                    }
                    dialog1.dismiss();
                });
        dialog.create();
        dialog.show();
    }

    private int getCountryIndex(String[] countryValues) {
        String savedCountry = mPresenter.getCountry();
        for (int i = 0; i < countryValues.length; i++) {
            if (countryValues[i].equals(savedCountry)) return i;
        }
        return -1;
    }
}
