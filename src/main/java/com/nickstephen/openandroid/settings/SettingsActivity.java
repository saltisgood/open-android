package com.nickstephen.openandroid.settings;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.nickstephen.openandroid.R;

import org.holoeverywhere.preference.PreferenceActivity;

/**
 * Activity used for preferences
 */
public class SettingsActivity extends PreferenceActivity {
    @Override
    @SuppressLint("NewApi")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (android.os.Build.VERSION.SDK_INT >= 11) {
            this.getFragmentManager().beginTransaction().replace(android.R.id.content, new SettingsFrag()).commit();
        }
        else {
            //noinspection deprecation
            this.addPreferencesFromResource(R.xml.preferences);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class SettingsFrag extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            this.addPreferencesFromResource(R.xml.preferences);
        }
    }
}
