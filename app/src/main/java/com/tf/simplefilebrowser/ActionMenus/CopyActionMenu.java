package com.tf.simplefilebrowser.ActionMenus;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.FileActionsHelper;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.NotificationsLab;
import com.tf.simplefilebrowser.helpers.SelectionHelper;

import java.io.File;

public class CopyActionMenu implements ActionMode.Callback {
    ActionMode mActionMode;
    MenuItem mMenuItem;
    int storage;
    private FileExplorerFragment mFragment;
    private String initFilePath;
    public CopyActionMenu(FileExplorerFragment fragment, String initFilePath){
        mFragment = fragment;
        this.initFilePath = initFilePath;
    }
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mFragment.mFilesActionActive = true;
        mActionMode = actionMode;
        mFragment.getActivity().getMenuInflater().inflate(R.menu.file_explorer_fragment_menu_paste,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.paste_button){
            storage = mFragment.curStorage;
            mMenuItem = menuItem;
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionMode.finish();
                }
            });
            new Thread(new Runnable() {
                @Override
                public void run() {
                    File file = new File(initFilePath);
                    File dest = new File(FileFoldersLab.get(mFragment.getActivity()).getCurPath() + File.separator + file.getName());

                    FileActionsHelper fa = new FileActionsHelper(mFragment.mContentResolver);
                    if(new File(initFilePath).isFile()){
                        NotificationsLab.get(mFragment.getActivity()).createProgressNotification(
                                Thread.currentThread().getId(), file, dest,
                                mFragment.getString(R.string.copying_file),
                                mFragment.getString(R.string.copying_in_progress), mFragment.getActivity());
                        FileActionsHelper.copyFileClass cfc = fa.new copyFileClass(
                                new File(initFilePath), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                        if(storage == 0){
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
                                new File(initFilePath), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                        if(storage == 0){
                            cfc.copyFolder.run();
                        }
                        else if(storage == 1){
                            if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                cfc.copyFolderToSD.run();
                            }
                        }
                    }
                    mFragment.updateUI();

                }
            }).start();
        }
        if(menuItem.getItemId() == R.id.action_bar_internal_storage){
            if(mFragment.curStorage != 0){
                mFragment.curStorage = 0;
                storage = 0;
                FileFoldersLab.get(mFragment.getActivity()).
                        setCurPath(FileFoldersLab.get(mFragment.getActivity()).getINTERNAL_STORAGE_PATH());
                mFragment.mSpinner.setSelection(0);
                mFragment.mRecyclerView.getLayoutManager().scrollToPosition(0);
                mFragment.updateUI();
            }
        }
        if(menuItem.getItemId() == R.id.action_bar_sd_card){
            if(mFragment.curStorage != 1){
                mFragment.curStorage = 1;
                storage = 1;
                FileFoldersLab.get(mFragment.getActivity()).
                        setCurPath(FileFoldersLab.get(mFragment.getActivity()).getSDCardPath());
                mFragment.mSpinner.setSelection(1);
                mFragment.mRecyclerView.getLayoutManager().scrollToPosition(0);
                mFragment.updateUI();
            }
        }
        if(menuItem.isCheckable() && !menuItem.isChecked()){
            if(menuItem.getItemId() == R.id.menu_sort_name){
                mFragment.sortMode = FileExplorerFragment.SortModes.ByName;
                menuItem.setChecked(true);
                mFragment.updateUI();
            }
            if(menuItem.getItemId() == R.id.menu_sort_date){
                mFragment.sortMode = FileExplorerFragment.SortModes.ByDate;
                menuItem.setChecked(true);
                mFragment.updateUI();
            }
        }

        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        mFragment.mFilesActionActive = false;
        mActionMode = null;
        if(mMenuItem == null)
            SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
        mFragment.updateUI();
        mMenuItem = null;
    }
}