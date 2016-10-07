package com.elneo.elneoscan;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

public class SettingsActivity extends AppCompatPreferenceActivity
            implements SharedPreferences.OnSharedPreferenceChangeListener {

    /**
     *   Allows to pick the warehouse and set aisle name
     *
     *   For the warehouse, data are loaded from res/values/arrays
     *   Key = warehouse id | Value = warehouse name
     *
     *   For the aisle, it's just a string
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
        // HOW TO INFLATE FROM XML ? like dis ...
        addPreferencesFromResource(R.xml.settings);
        populate(getPreferenceScreen().getSharedPreferences());
    }

    // Populate preference view
    private void populate(SharedPreferences sharedPreferences) {
        // ListPreference - Selected Warehouse
        // Fetch values from res/values/arrays.xml
        String[] harry = getResources().getStringArray(R.array.warehouse_values);
        String key = sharedPreferences.getString(ElneoScan.KEY_PREF_SELECTED_WAREHOUSE, "NoString");

        int index = 0;
        if (!key.equals("NoString")) {
            index = Integer.parseInt(key);
        }

        ListPreference listPreference =
                (ListPreference) findPreference(ElneoScan.KEY_PREF_SELECTED_WAREHOUSE);

        if (index > 0) {
            listPreference.setSummary(harry[index - 1]);
        } else {
            listPreference.setSummary("");
        }

        // EditTextPreference - Aisle
        String aisle = sharedPreferences.getString(ElneoScan.KEY_PREF_AISLE, "");

        EditTextPreference editTextPreference =
                (EditTextPreference) findPreference("aisle");
        editTextPreference.setSummary(aisle);
    }

    private void setupActionBar() {
        // Display back arrow (<-) in the actionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // When back is pressed ... go back
        NavUtils.navigateUpFromSameTask(this);
        return super.onOptionsItemSelected(item);
    }

    // Listener onSharedPreferenceChanged
    // Fix from http://stackoverflow.com/questions/2542938/sharedpreferences-onsharedpreferencechangelistener-not-being-called-consistently?noredirect=1&lq=1
    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
            .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        populate(sharedPreferences);
    }
}