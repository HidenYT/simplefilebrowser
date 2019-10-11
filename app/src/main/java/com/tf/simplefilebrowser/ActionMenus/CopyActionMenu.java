package com.tf.simplefilebrowser.ActionMenus;

import android.view.ActionMode;
import android.view.MenuItem;

import com.tf.simplefilebrowser.StorageTypes;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.FileActionsHelper;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.NotificationsLab;

import java.io.File;

public class CopyActionMenu extends ActionMenu {
    private FileExplorerFragment mFragment;
    public CopyActionMenu(FileExplorerFragment fragment, String initFilePath){
        super(fragment, initFilePath);
        mFragment = fragment;
        setInitFilePath(initFilePath);
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.paste_button){
            setStorage(mFragment.getCurStorage());
            setMenuItem(menuItem);
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionMode.finish();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(getInitFilePath());
                    File dest = new File(FileFoldersLab.get(mFragment.getActivity()).getCurPath() + File.separator + file.getName());

                    FileActionsHelper fa = new FileActionsHelper(mFragment.getContentResolver());
                    if(new File(getInitFilePath()).isFile()){
                        NotificationsLab.get(mFragment.getActivity()).createProgressNotification(
                                Thread.currentThread().getId(), file, dest,
                                mFragment.getString(R.string.copying_file),
                                mFragment.getString(R.string.copying_in_progress), mFragment.getActivity());
                        FileActionsHelper.copyFileClass cfc = fa.new copyFileClass(
                                new File(getInitFilePath()), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                        if(getStorage() == StorageTypes.InternalStorage){
                            cfc.copyFile.run();
                        }else{
                            if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                cfc.copyFileToSD.run();
                            }
                        }
                    }else{
                        NotificationsLab.get(mFragment.getActivity()).createProgressNotification(
                                Thread.currentThread().getId(), file, dest,
                                mFragment.getString(R.string.copying_folder),
                                mFragment.getString(R.string.copying_in_progress), mFragment.getActivity());
                        FileActionsHelper.copyFolderClass cfc = fa.new copyFolderClass(
                                new File(getInitFilePath()), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                        if(getStorage() == StorageTypes.InternalStorage){
                            cfc.copyFolder.run();
                        }
                        else if(getStorage() == StorageTypes.SDCard){
                            if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                cfc.copyFolderToSD.run();
                            }
                        }
                    }
                    mFragment.updateUI(false);
                }
            }).start();
        }
        checkStorageAndSort(menuItem);
        return true;
    }
}