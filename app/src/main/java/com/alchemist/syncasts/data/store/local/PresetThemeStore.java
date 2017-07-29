package com.alchemist.syncasts.data.store.local;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.ColorRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.annotations.AccentTheme;
import com.alchemist.syncasts.data.annotations.PrimaryTheme;
import com.alchemist.syncasts.utils.ThemeUtils;

public class PresetThemeStore implements ThemeStore {

    private Context mContext;
    private PreferencesHelper mPreferencesHelper;

    public PresetThemeStore(Context context, PreferencesHelper preferencesHelper) {
        mContext = context;
        mPreferencesHelper = preferencesHelper;
    }

    @Override
    public int getPrimaryColor() {
        return ContextCompat.getColor(mContext, getPrimaryColorRes());
    }

    @ColorRes
    private int getPrimaryColorRes() {
        switch (mPreferencesHelper.getPrimaryColor()) {
            case PrimaryTheme.GREY:
                return R.color.primary_grey;
            case PrimaryTheme.RED:
                return R.color.primary_red;
            case PrimaryTheme.ORANGE:
                return R.color.primary_orange;
            case PrimaryTheme.YELLOW:
                return R.color.primary_yellow;
            case PrimaryTheme.GREEN:
                return R.color.primary_green;
            case PrimaryTheme.CYAN:
                return R.color.primary;
            case PrimaryTheme.BLUE:
                return R.color.primary_blue;
            case PrimaryTheme.PURPLE:
                return R.color.primary_purple;
            case PrimaryTheme.BLACK:
                return R.color.primary_black;
            default:
                return R.color.primary;
        }
    }

    @Override
    public int getAccentColor() {
        return ContextCompat.getColor(mContext, getAccentColorRes());
    }

    @ColorRes
    private int getAccentColorRes() {
        switch (mPreferencesHelper.getAccentColor()) {
            case AccentTheme.GRAY:
                return R.color.accent_grey;
            case AccentTheme.RED:
                return R.color.accent_red;
            case AccentTheme.ORANGE:
                return R.color.accent_orange;
            case AccentTheme.YELLOW:
                return R.color.accent_yellow;
            case AccentTheme.GREEN:
                return R.color.accent_green;
            case AccentTheme.CYAN:
                return R.color.accent;
            case AccentTheme.BLUE:
                return R.color.accent_blue;
            case AccentTheme.PURPLE:
                return R.color.accent_purple;
            case AccentTheme.TEAL:
                return R.color.accent_black;
            default:
                return R.color.accent;
        }
    }

    @Override
    public void setTheme(AppCompatActivity activity) {
        ThemeUtils.setTheme(activity,
                activity.getDelegate(),
                mPreferencesHelper.getPrimaryColor(),
                mPreferencesHelper.getAccentColor(),
                mPreferencesHelper.getBaseColor());
        applyTaskDescription(activity);
    }

    private void applyTaskDescription(Activity activity) {
        String taskName = mContext.getString(R.string.app_name);
        int taskColor = getPrimaryColor();
        Bitmap taskIcon = getAppIcon();

        ActivityManager.TaskDescription taskDescription =
                new ActivityManager.TaskDescription(taskName, taskIcon, taskColor);
        activity.setTaskDescription(taskDescription);
    }

    private Bitmap getAppIcon() {
        return BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
    }

    @AppCompatDelegate.NightMode
    public int getNightMode() {
        return ThemeUtils.getNightMode(mPreferencesHelper.getBaseColor());
    }
}
