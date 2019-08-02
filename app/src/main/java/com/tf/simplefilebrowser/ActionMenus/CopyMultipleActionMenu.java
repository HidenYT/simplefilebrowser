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
import java.util.LinkedList;

public class CopyMultipleActionMenu implements ActionMode.Callback {
    ActionMode mActionMode;
    MenuItem mMenuItem;
    private int storage;
    private FileExplorerFragment mFragment;
    public CopyMultipleActionMenu(FileExplorerFragment fragment){
        mFragment = fragment;
    }
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mActionMode = actionMode;
        mFragment.getActivity().getMenuInflater().inflate(R.menu.file_explorer_fragment_menu_paste,menu);
        mFragment.mFilesActionActive = true;
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        mMenuItem = menuItem;
        if(menuItem.getItemId() == R.id.paste_button){
            storage = mFragment.curStorage;
            mMenuItem = menuItem;
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionMode.finish();
                }
            });
            final String finalPath = FileFoldersLab.get(mFragment.getActivity()).getCurPath();
            final LinkedList<String> arr = SelectionHelper.get(mFragment.getActivity()).getSelectedFiles();

            final LinkedList<String> dests = new LinkedList<>();
            for (String i:
                 arr) {
                dests.add(finalPath + File.separator + new File(i).getName());
            }

            new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationsLab.get(mFragment.getActivity()).createProgressMultipleFiles(
                            Thread.currentThread().getId(), arr, dests, "Copying files",
                            "Copying in progress",mFragment.getActivity()
                    );
                    FileActionsHelper fa = new FileActionsHelper(mFragment.mContentResolver);
                    for(String k : arr){
                        if(new File(k).isFile()){
                            FileActionsHelper.copyFileClass cfc = fa.new copyFileClass(
                                    new File(k), finalPath, mFragment.getActivity());
                            if(storage == 0){
                                cfc.copyFile.run();
                            }else {
                                if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                    cfc.copyFileToSD.run();
                                }
                            }
                        }else{
                            FileActionsHelper.copyFolderClass cfc = fa.new copyFolderClass(
                                    new File(k), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
                            if(storage == 0){
                                cfc.copyFolder.run();
                            }
                            else if(storage == 1){
                                if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                    cfc.copyFolderToSD.run();
                                }
                            }
                        }
                    }
                    SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
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
