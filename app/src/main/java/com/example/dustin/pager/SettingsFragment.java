package com.example.dustin.pager;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by Dustin on 1/18/2017.
 */

public class SettingsFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
    }
}