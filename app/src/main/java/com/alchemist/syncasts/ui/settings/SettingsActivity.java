package com.alchemist.syncasts.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatDelegate;

import com.alchemist.syncasts.R;
import com.alchemist.syncasts.data.annotations.AccentTheme;
import com.alchemist.syncasts.data.annotations.PrimaryTheme;
import com.alchemist.syncasts.data.store.local.PreferencesHelper;
import com.alchemist.syncasts.utils.ThemeUtils;

public class SettingsActivity extends AppCompatPreferenceActivity {

    public static Intent getStartIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new MainPreferenceFragment())
                .commit();
    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class MainPreferenceFragment extends PreferenceFragment
            implements SharedPreferences.OnSharedPreferenceChangeListener {

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager().setSharedPreferencesName(PreferencesHelper.PREF_FILE_NAME);
            addPreferencesFromResource(R.xml.prefs);
            setHasOptionsMenu(true);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (getString(R.string.pref_key_color_base).equals(key)
                    || getString(R.string.pref_key_color_accent).equals(key)
                    || getString(R.string.pref_key_color_primary).equals(key)) {
                @PrimaryTheme int primary =
                        sharedPreferences.getInt(getString(R.string.pref_key_color_primary), PrimaryTheme.CYAN);
                @AccentTheme int accent =
                        sharedPreferences.getInt(getString(R.string.pref_key_color_accent), primary);
                @AppCompatDelegate.NightMode int background =
                        sharedPreferences.getBoolean(getString(R.string.pref_key_color_base), true) ? 0 : 1;
                AppCompatPreferenceActivity activity = (AppCompatPreferenceActivity) getActivity();
                ThemeUtils.setTheme(activity, activity.getDelegate(), primary, accent, background);
                getActivity().recreate();
            }
        }
    }
}
