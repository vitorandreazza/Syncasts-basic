package com.alchemist.syncasts.data.store.local;

import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

public interface ThemeStore {

    @ColorInt int getPrimaryColor();
    @ColorInt int getAccentColor();

    @AppCompatDelegate.NightMode int getNightMode();

    void setTheme(AppCompatActivity activity);
}