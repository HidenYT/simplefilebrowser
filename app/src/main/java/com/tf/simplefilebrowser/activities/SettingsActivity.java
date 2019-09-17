package com.tf.simplefilebrowser.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.CheckBox;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.PreferencesHelper;
import com.tf.simplefilebrowser.helpers.StorageHelper;

public class SettingsActivity extends AppCompatActivity {
    private CheckBox mShowHiddenFilesCheckBox;
    private final int SD_CARD_PATH_REQUEST_CODE = 0;
    private Activity activity;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setTitle(R.string.settings_tile);
        activity = this;
        prepareViews();
    }




    private void prepareViews(){
        ConstraintLayout mResetSDCardPathButton = findViewById(R.id.reset_sd_card_path_button);
        mResetSDCardPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(StorageHelper.get(activity).getAllPaths().size() > 1){
                    FileFoldersLab.getSDCardAccess(activity, SD_CARD_PATH_REQUEST_CODE);
                }
            }
        });
        mResetSDCardPathButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(StorageHelper.get(activity).getAllPaths().size() > 1){
                    FileFoldersLab.getSDCardAccess(activity, SD_CARD_PATH_REQUEST_CODE);
                }
            }
        });
        ConstraintLayout mShowHiddenFilesButton = findViewById(R.id.switch_display_hidden_files);
        mShowHiddenFilesCheckBox = findViewById(R.id.display_hidden_files_checkbox);
        mShowHiddenFilesCheckBox.setChecked(PreferencesHelper.getInstance(this).isDisplayingHiddenFiles());
        mShowHiddenFilesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PreferencesHelper.getInstance(activity).isDisplayingHiddenFiles()){
                    mShowHiddenFilesCheckBox.setChecked(false);
                    PreferencesHelper.getInstance(activity).setDisplayingHiddenFiles(false);
                }else{
                    mShowHiddenFilesCheckBox.setChecked(true);
                    PreferencesHelper.getInstance(activity).setDisplayingHiddenFiles(true);
                }
            }
        });
        mShowHiddenFilesCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(PreferencesHelper.getInstance(activity).isDisplayingHiddenFiles()){
                    mShowHiddenFilesCheckBox.setChecked(false);
                    PreferencesHelper.getInstance(activity).setDisplayingHiddenFiles(false);
                }else{
                    mShowHiddenFilesCheckBox.setChecked(true);
                    PreferencesHelper.getInstance(activity).setDisplayingHiddenFiles(true);
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!= Activity.RESULT_OK){
            return;
        }
        if(requestCode == SD_CARD_PATH_REQUEST_CODE){
            if(data != null && FileExplorerFragment.takePermission(activity, data.getData())){
                Uri uri = data.getData();
                assert uri != null;
                FileFoldersLab.get(activity).setSDCardUri(uri.toString());
            }
        }
    }
}
