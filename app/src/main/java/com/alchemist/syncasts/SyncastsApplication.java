package com.alchemist.syncasts;

import android.app.Application;
import android.content.Context;
import android.os.StrictMode;

import com.alchemist.syncasts.data.inject.component.ApplicationComponent;
import com.alchemist.syncasts.data.inject.component.DaggerApplicationComponent;
import com.alchemist.syncasts.data.inject.module.ApplicationModule;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.picasso.Picasso;

import timber.log.Timber;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class SyncastsApplication extends Application {

    private ApplicationComponent mApplicationComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }

        initializeStrictMode();
        LeakCanary.install(this);
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
            Picasso.with(this).setIndicatorsEnabled(true); //red=network | blue=disk | green=memory
        }
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Ubuntu-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());
    }

    public static SyncastsApplication get(Context context) {
        return (SyncastsApplication) context.getApplicationContext();
    }

    public ApplicationComponent getComponent() {
        if (mApplicationComponent == null) {
            mApplicationComponent = DaggerApplicationComponent.builder()
                    .applicationModule(new ApplicationModule(this))
                    .build();
        }
        return mApplicationComponent;
    }

    private void initializeStrictMode() {
        if (BuildConfig.DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectDiskReads()
                    .detectDiskWrites()
                    .detectAll()
                    .penaltyLog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectLeakedSqlLiteObjects()
                    .detectLeakedClosableObjects()
                    .penaltyLog()
                    .build());
        }
    }
}
