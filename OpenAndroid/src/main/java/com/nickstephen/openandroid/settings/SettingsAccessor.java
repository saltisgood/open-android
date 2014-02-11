package com.nickstephen.openandroid.settings;

import android.content.Context;

import com.nickstephen.openandroid.R;

import org.holoeverywhere.preference.PreferenceManager;

/**
 * Static class for accessing user preferences
 */
public final class SettingsAccessor {
    private SettingsAccessor() {}

    /**
     * Query for the user's preference on whether to ignore the standard android classes when viewing
     * a package.
     * @param context A context with which to retrieve the preferences
     * @return True to ignore android, false otherwise (defaults to true)
     */
    public static boolean getIgnoreAndroidClasses(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(R.string.pref_android_filter_key, true);
    }

    /**
     * Query for the user's preference on whether to ignore synthetic methods when viewing a class
     * @param context A context with which to retrieve the preferences
     * @return True to ignore compiler generated methods, false otherwise (defaults to true)
     */
    public static boolean getIgnoreSyntheticMethods(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean(R.string.pref_synthetic_filter_key, true);
    }
}
