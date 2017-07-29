package com.alchemist.syncasts.ui;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.SyncastsApplication;
import com.alchemist.syncasts.data.annotations.AccentTheme;
import com.alchemist.syncasts.data.annotations.PrimaryTheme;
import com.alchemist.syncasts.data.inject.component.ActivitySubComponent;
import com.alchemist.syncasts.data.inject.component.ConfigPersistentComponent;
import com.alchemist.syncasts.data.inject.component.DaggerConfigPersistentComponent;
import com.alchemist.syncasts.data.inject.module.ActivityModule;
import com.alchemist.syncasts.data.store.DataManager;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Abstract activity that every other Activity in this application must implement. It handles
 * creation of Dagger components and makes sure that instances of ConfigPersistentComponent survive
 * across configuration changes.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private static final String KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID";
    private static final AtomicLong NEXT_ID = new AtomicLong(0);
    private static final Map<Long, ConfigPersistentComponent> sComponentsMap = new HashMap<>();

    private ActivitySubComponent mActivityComponent;
    private long mActivityId;

    @Inject DataManager mDataManager;
    @PrimaryTheme private int mPrimaryColor;
    @AccentTheme private int mAccentColor;
    @AppCompatDelegate.NightMode private int mBackgroundColor;
    private boolean mNightMode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        // Create the ActivityComponent and reuses cached ConfigPersistentComponent if this is
        // being called after a configuration change.
        mActivityId = savedInstanceState != null ? savedInstanceState.getLong(KEY_ACTIVITY_ID)
                : NEXT_ID.getAndIncrement();
        ConfigPersistentComponent configPersistentComponent;

        if (!sComponentsMap.containsKey(mActivityId)) {
            Timber.i("Creating new ConfigPersistentComponent id=%d", mActivityId);
            configPersistentComponent = DaggerConfigPersistentComponent.builder()
                    .applicationComponent(SyncastsApplication.get(this).getComponent())
                    .build();
            sComponentsMap.put(mActivityId, configPersistentComponent);
        } else {
            Timber.i("Reusing ConfigPersistentComponent id=%d", mActivityId);
            configPersistentComponent = sComponentsMap.get(mActivityId);
        }
        mActivityComponent = configPersistentComponent.activityComponent(new ActivityModule(this));
        activityComponent().injectBaseActivity(this);

        mDataManager.setTheme(this);
        mPrimaryColor = mDataManager.getPreferencesHelper().getPrimaryColor();
        mAccentColor = mDataManager.getPreferencesHelper().getAccentColor();
        mBackgroundColor = mDataManager.getNightMode();

        mNightMode = getResources().getBoolean(R.bool.is_night);

        super.onCreate(savedInstanceState);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // If the theme was changed since this Activity was created, or the automatic day/night
        // theme has changed state, recreate this activity
        mDataManager.setTheme(this);
        boolean primaryDiff = mPrimaryColor != mDataManager.getPreferencesHelper().getPrimaryColor();
        boolean accentDiff = mAccentColor != mDataManager.getPreferencesHelper().getAccentColor();
        boolean backgroundDiff = mBackgroundColor != mDataManager.getNightMode();

        boolean nightDiff = mNightMode != getResources().getBoolean(R.bool.is_night);

        if (primaryDiff || accentDiff || backgroundDiff
                || (mBackgroundColor == AppCompatDelegate.MODE_NIGHT_AUTO && nightDiff)) {
            recreate();
        }
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(context));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(KEY_ACTIVITY_ID, mActivityId);
    }

    @Override
    protected void onDestroy() {
        if (!isChangingConfigurations()) {
            Timber.i("Clearing ConfigPersistentComponent id=%d", mActivityId);
            sComponentsMap.remove(mActivityId);
        }
        super.onDestroy();
    }

    public ActivitySubComponent activityComponent() {
        return mActivityComponent;
    }
}
