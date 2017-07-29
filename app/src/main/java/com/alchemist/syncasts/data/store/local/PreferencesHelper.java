package com.alchemist.syncasts.data.store.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatDelegate;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.annotations.AccentTheme;
import com.alchemist.syncasts.data.annotations.PrimaryTheme;
import com.alchemist.syncasts.data.inject.ApplicationContext;

import javax.inject.Inject;
import javax.inject.Singleton;

import static com.alchemist.syncasts.R.string.pref_country_key;

@Singleton
public class PreferencesHelper {

    public static final String PREF_FILE_NAME = "syncasts_pref_file";
    private final SharedPreferences mPrefs;
    private Context mContext;

    @Inject
    public PreferencesHelper(@ApplicationContext Context context) {
        mPrefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        mContext = context;
    }

    private int getInt(@StringRes int keyRes, int defaultValue) {
        return mPrefs.getInt(mContext.getString(keyRes), defaultValue);
    }

    private boolean getBoolean(@StringRes int keyRes, boolean defaultValue) {
        return mPrefs.getBoolean(mContext.getString(keyRes), defaultValue);
    }

    private String getString(@StringRes int keyRes, String defaultValue) {
        return mPrefs.getString(mContext.getString(keyRes), defaultValue);
    }

    private void putInt(@StringRes int keyRes, int value) {
        mPrefs.edit().putInt(mContext.getString(keyRes), value).apply();
    }

    private void putString(@StringRes int keyRes, String value) {
        mPrefs.edit().putString(mContext.getString(keyRes), value).apply();
    }

    public String getCountry() {
        return getString(pref_country_key, "br");
    }

    @PrimaryTheme
    @SuppressWarnings("WrongConstant")
    public int getPrimaryColor() {
        return getInt(R.string.pref_key_color_primary, PrimaryTheme.CYAN);
    }

    @AccentTheme
    @SuppressWarnings("WrongConstant")
    public int getAccentColor() {
        return getInt(R.string.pref_key_color_accent, getPrimaryColor());
    }

    @AppCompatDelegate.NightMode
    public int getBaseColor() {
        return getBoolean(R.string.pref_key_color_base, true) ? 0 : 1;
    }

    public void setCountry(String country) {
        putString(pref_country_key, country);
    }
}
