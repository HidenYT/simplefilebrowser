package com.tf.simplefilebrowser.activities;

import android.app.Fragment;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;

import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.R;

import java.io.File;

public class FileExplorerActivity extends SingleFragmentActivity {
    private FileExplorerFragment mFileExplorerFragment;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);
        Intent intent = getIntent();
        Uri data = intent.getData();
        if(data != null && intent != null){
            Log.d("TAG", "onCreate: " + data.getPath());
            Intent i = ArchiveViewActivity.newIntent(this, new File(data.getPath()));

            startActivity(i);
        }

        if(savedInstanceState != null){
            mFileExplorerFragment = (FileExplorerFragment)
                    getFragmentManager().getFragment(savedInstanceState, "FileExplorerFragment");
        }
    }
    @Override
    public Fragment createFragment(){
        mFileExplorerFragment = new FileExplorerFragment();
        return mFileExplorerFragment;
    }
    @Override
    public void onSaveInstanceState(Bundle save){
        super.onSaveInstanceState(save);
        getFragmentManager().putFragment(save, "FileExplorerFragment", mFileExplorerFragment);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("TAG", "Destroyed");
        FileFoldersLab.get(this).removeTmpFolder();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP){
            mFileExplorerFragment.onBackButtonPressed();
            return true;
        }

        return super.dispatchKeyEvent(event);
    }
}
