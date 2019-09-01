package com.tf.simplefilebrowser.ActionMenus;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.tf.simplefilebrowser.StorageTypes;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.FileActionsHelper;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.NotificationsLab;
import com.tf.simplefilebrowser.helpers.SelectionHelper;

import java.io.File;

public class MoveActionMenu extends ActionMenu {
    private FileExplorerFragment mFragment;
    private String initFilePath;
    public MoveActionMenu(FileExplorerFragment fragment, String initFilePath){
        super(fragment, initFilePath);
        mFragment = fragment;
        this.initFilePath = initFilePath;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.paste_button){
            setStorage(mFragment.getCurStorage());
            setMenuItem(item);
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mode.finish();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(initFilePath);
                    File dest = new File(FileFoldersLab.get(
                            mFragment.getActivity()).getCurPath() + File.separator + file.getName());

                    FileActionsHelper fa = new FileActionsHelper(mFragment.getContentResolver());
                    if(file.isFile()){
                        NotificationsLab.get(mFragment.getActivity()).createProgressNotification(
                                Thread.currentThread().getId(), file, dest,
                                mFragment.getString(R.string.moving_file),
                                mFragment.getString(R.string.moving_in_progress), mFragment.getActivity());
                        FileActionsHelper.copyFileClass cfc = fa.new copyFileClass(
                                new File(initFilePath), FileFoldersLab.get(
                                        mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                        if(getStorage() == StorageTypes.InternalStorage){
                            cfc.copyFile.run();
                        }
                        else if(getStorage() == StorageTypes.SDCard){
                            if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                cfc.copyFileToSD.run();
                            }
                        }
                    }else{
                        NotificationsLab.get(mFragment.getActivity()).createProgressNotification(
                                Thread.currentThread().getId(), file, dest,
                                mFragment.getString(R.string.moving_folder),
                                mFragment.getString(R.string.moving_in_progress), mFragment.getActivity());
                        FileActionsHelper.copyFolderClass cfc = fa.new copyFolderClass(
                                new File(initFilePath), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                        if(getStorage() == StorageTypes.InternalStorage){
                            cfc.copyFolder.run();
                        }
                        else if(getStorage() == StorageTypes.SDCard){
                            if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                cfc.copyFolderToSD.run();
                            }
                        }
                    }
                    if(initFilePath.startsWith(FileFoldersLab.get(mFragment.getActivity()).getSDCardPath()))
                        FileFoldersLab.removeFileSD(initFilePath);
                    else
                        FileFoldersLab.removeFile(initFilePath);

                    mFragment.updateUI(false);

                }
            }).start();
        }
        checkStorageAndSort(item);
        return true;
    }
}