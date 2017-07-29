package com.alchemist.syncasts.utils;

import android.app.Activity;
import android.support.annotation.StyleRes;
import android.support.v7.app.AppCompatDelegate;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.annotations.AccentTheme;
import com.alchemist.syncasts.data.annotations.PrimaryTheme;
import com.alchemist.syncasts.ui.search.SearchActivity;

import static com.alchemist.syncasts.data.annotations.BaseTheme.AUTO;
import static com.alchemist.syncasts.data.annotations.BaseTheme.DARK;
import static com.alchemist.syncasts.data.annotations.BaseTheme.LIGHT;

public final class ThemeUtils {

    public static void setTheme(Activity activity,
                                AppCompatDelegate delegate,
                                @PrimaryTheme int primaryColor,
                                @AccentTheme int accentColor,
                                @AppCompatDelegate.NightMode int baseColor) {
        applyNightMode(delegate, baseColor);
        if (!(activity instanceof SearchActivity)) {
            activity.setTheme(R.style.AppTheme);
        }
        activity.getTheme().applyStyle(getPrimaryThemeId(primaryColor), true);
        activity.getTheme().applyStyle(getAccentThemeId(accentColor), true);
    }

    private static void applyNightMode(AppCompatDelegate delegate, int baseColor) {
        AppCompatDelegate.setDefaultNightMode(getNightMode(baseColor));
        delegate.applyDayNight();
    }

    @AppCompatDelegate.NightMode
    public static int getNightMode(@AppCompatDelegate.NightMode int baseColor) {
        switch (baseColor) {
            case AUTO:
                return AppCompatDelegate.MODE_NIGHT_AUTO;
            case DARK:
                return AppCompatDelegate.MODE_NIGHT_YES;
            case LIGHT:
            default:
                return AppCompatDelegate.MODE_NIGHT_NO;
        }
    }

    @StyleRes
    private static int getPrimaryThemeId(@PrimaryTheme int primaryColor) {
        switch (primaryColor) {
            case PrimaryTheme.GREY:
                return R.style.Primary_Grey;
            case PrimaryTheme.RED:
                return R.style.Primary_Red;
            case PrimaryTheme.ORANGE:
                return R.style.Primary_Orange;
            case PrimaryTheme.YELLOW:
                return R.style.Primary_Yellow;
            case PrimaryTheme.GREEN:
                return R.style.Primary_Green;
            case PrimaryTheme.CYAN:
                return R.style.Primary_Cyan;
            case PrimaryTheme.BLUE:
                return R.style.Primary_Blue;
            case PrimaryTheme.PURPLE:
                return R.style.Primary_Purple;
            case PrimaryTheme.BLACK:
                return R.style.Primary_Black;
            default:
                return R.style.Primary_Cyan;
        }
    }

    @StyleRes
    private static int getAccentThemeId(@AccentTheme int accentColor) {
        switch (accentColor) {
            case AccentTheme.GRAY:
                return R.style.Accent_Grey;
            case AccentTheme.RED:
                return R.style.Accent_Red;
            case AccentTheme.ORANGE:
                return R.style.Accent_Orange;
            case AccentTheme.YELLOW:
                return R.style.Accent_Yellow;
            case AccentTheme.GREEN:
                return R.style.Accent_Green;
            case AccentTheme.CYAN:
                return R.style.Accent_Cyan;
            case AccentTheme.BLUE:
                return R.style.Accent_Blue;
            case AccentTheme.PURPLE:
                return R.style.Accent_Purple;
            case AccentTheme.TEAL:
                return R.style.Accent_Black;
            default:
                return R.style.Accent_Cyan;
        }
    }
}
