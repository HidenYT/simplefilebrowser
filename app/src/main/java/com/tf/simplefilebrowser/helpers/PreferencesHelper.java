package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.content.SharedPreferences;

public class PreferencesHelper {
    private static PreferencesHelper preferencesHelper;
    private final Activity activity;
    private final String SHARED_PREFS_NAME = "Prefs";
    private final String ITEM_DISPLAY_HIDDEN_FILES = "DISPLAY_HIDDEN_FILES";
    private final SharedPreferences sharedPrefs;

    public static PreferencesHelper getInstance(Activity activity){
        if(preferencesHelper == null)
            preferencesHelper = new PreferencesHelper(activity);
        return preferencesHelper;
    }
    private PreferencesHelper(Activity activity){
        this.activity = activity;
        this.sharedPrefs = activity.getSharedPreferences(SHARED_PREFS_NAME, 0);
    }

    public boolean isDisplayingHiddenFiles(){
        if(!sharedPrefs.contains(ITEM_DISPLAY_HIDDEN_FILES)){
            SharedPreferences.Editor ed = sharedPrefs.edit();
            ed.putBoolean(ITEM_DISPLAY_HIDDEN_FILES, false).apply();
        }
        return sharedPrefs.getBoolean(ITEM_DISPLAY_HIDDEN_FILES, false);
    }
    public void setDisplayingHiddenFiles(boolean value){
        SharedPreferences.Editor ed = sharedPrefs.edit();
        ed.putBoolean(ITEM_DISPLAY_HIDDEN_FILES, value).apply();
    }
}
