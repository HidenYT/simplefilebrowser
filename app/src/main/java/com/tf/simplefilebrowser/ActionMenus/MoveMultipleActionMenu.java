package com.tf.simplefilebrowser.ActionMenus;

import android.util.Log;
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

public class MoveMultipleActionMenu implements ActionMode.Callback {
    ActionMode mActionMode;
    MenuItem mMenuItem;
    private int storage;
    private FileExplorerFragment mFragment;
    public MoveMultipleActionMenu(FileExplorerFragment fragment){
        mFragment = fragment;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mFragment.mFilesActionActive = true;
        mActionMode = mode;
        mFragment.getActivity().getMenuInflater().inflate(R.menu.file_explorer_fragment_menu_paste,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.paste_button){
            storage = mFragment.curStorage;
            mMenuItem = item;
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mode.finish();
                }
            });
            final String finalPath = FileFoldersLab.get(mFragment.getActivity()).getCurPath();
            final LinkedList<String> arr = SelectionHelper.get(mFragment.getActivity()).getSelectedFiles();
            final LinkedList<String> dests = new LinkedList<>();
            for (String i: arr) {
                dests.add(finalPath + File.separator + new File(i).getName());
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationsLab.get(mFragment.getActivity()).createProgressMultipleFiles(
                            Thread.currentThread().getId(), arr, dests, "Moving files",
                            "Moving in progress",mFragment.getActivity()
                    );
                    FileActionsHelper fa = new FileActionsHelper(mFragment.mContentResolver);
                    for(String k : arr){
                        if(new File(k).isFile()){
                            FileActionsHelper.copyFileClass cfc = fa.new copyFileClass(
                                    new File(k), FileFoldersLab.get(mFragment.getActivity()).getCurPath(), mFragment.getActivity());
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
                        if(k.startsWith(FileFoldersLab.get(mFragment.getActivity()).getSDCardPath()))
                            FileFoldersLab.removeFileSD(k);
                        else
                            FileFoldersLab.removeFile(k);
                    }
                    SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
                    mFragment.updateUI();

                }
            }).start();
        }
        if(item.getItemId() == R.id.action_bar_internal_storage){
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
        if(item.getItemId() == R.id.action_bar_sd_card){
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
        if(item.isCheckable() && !item.isChecked()){
            if(item.getItemId() == R.id.menu_sort_name){
                mFragment.sortMode = FileExplorerFragment.SortModes.ByName;
                item.setChecked(true);
                mFragment.updateUI();
            }
            if(item.getItemId() == R.id.menu_sort_date){
                mFragment.sortMode = FileExplorerFragment.SortModes.ByDate;
                item.setChecked(true);
                mFragment.updateUI();
            }
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        mFragment.mFilesActionActive = false;
        mActionMode = null;
        if(mMenuItem != null)
            Log.d("TAG", "onDestroyActionMode: ");
        if(mMenuItem == null)
            SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
        mFragment.updateUI();
        mMenuItem = null;
    }
}