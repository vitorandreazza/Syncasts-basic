package com.alchemist.syncasts.data.inject.module;

import android.app.Activity;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;

import com.alchemist.syncasts.data.inject.ActivityContext;

import dagger.Module;
import dagger.Provides;

@Module
public class ActivityModule {

    private Activity mActivity;

    public ActivityModule(Activity mActivity) {
        this.mActivity = mActivity;
    }

    @Provides
    Activity provideActivity() {
        return mActivity;
    }

    @Provides
    @ActivityContext
    Context provideContext() {
        return mActivity;
    }

    @Provides
    FragmentManager provideFragmentManager(Activity activity) {
        return ((AppCompatActivity) activity).getSupportFragmentManager();
    }
}
