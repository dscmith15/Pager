package com.example.dustin.pager;



import android.content.SharedPreferences;

import android.os.Bundle;

import android.preference.Preference;
import android.preference.PreferenceActivity;

import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;


public class SettingsActivity extends PreferenceActivity {
    public static final String KEY_PREF_SYNC_CONN = "pref_syncConnectionType";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();

    }

    public static class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                              String key) {
            if (key.equals(KEY_PREF_SYNC_CONN)) {
                Preference connectionPref = findPreference(key);
                // Set summary to be the user-description for the selected value
                connectionPref.setSummary(sharedPreferences.getString(key, ""));
            }

        }
        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        }
        @Override
        public void onPause() {
            getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
            super.onPause();
        }
    }



}
