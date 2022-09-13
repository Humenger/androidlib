package com.github.humenger.android13lib;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.github.humenger.rsharedpreferences.RSharedPreferences;

public class SettingsActivity extends AppCompatActivity {
public static final String TAG="SettingsActivity";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            getPreferenceManager().setSharedPreferencesName("Test");
            getPreferenceManager().setSharedPreferencesMode(Context.MODE_WORLD_READABLE);
            SharedPreferences preferences= RSharedPreferences.getSharedPreferences(getPreferenceManager());

            setPreferencesFromResource(R.xml.root_preferences, rootKey);
            Log.d(TAG, "onCreatePreferences: replace success");
        }
    }
}