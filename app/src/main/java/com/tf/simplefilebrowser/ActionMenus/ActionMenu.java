package com.tf.simplefilebrowser.ActionMenus;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.StorageTypes;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.SelectionHelper;
import com.tf.simplefilebrowser.helpers.StorageHelper;

public abstract class ActionMenu implements ActionMode.Callback {
    private FileExplorerFragment fragment;
    private MenuItem menuItem;
    private String initFilePath;
    private StorageTypes storage;

    StorageTypes getStorage() {
        return storage;
    }
    void setStorage(StorageTypes storage) {
        this.storage = storage;
    }
    void setMenuItem(MenuItem menuItem) {
        this.menuItem = menuItem;
    }
    String getInitFilePath() {
        return initFilePath;
    }
    void setInitFilePath(String initFilePath) {
        this.initFilePath = initFilePath;
    }

    ActionMenu(FileExplorerFragment fragment, String initFilePath){
        this.fragment = fragment;
        this.initFilePath = initFilePath;
    }
    ActionMenu(FileExplorerFragment fragment){
        this.fragment = fragment;
    }
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        fragment.mFilesActionActive = true;
        fragment.getActivity().getMenuInflater().inflate(R.menu.file_explorer_fragment_menu_paste,menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode actionMode, Menu menu) {
        return false;
    }

    @Override
    public abstract boolean onActionItemClicked(ActionMode actionMode, MenuItem menuItem);

    @Override
    public void onDestroyActionMode(ActionMode actionMode) {
        fragment.mFilesActionActive = false;
        if(menuItem == null)
            SelectionHelper.get(fragment.getActivity()).getSelectedFiles().clear();
        fragment.updateUI(false);
        menuItem = null;
    }
    void checkStorageAndSort(MenuItem item){
        if(item.getItemId() == R.id.action_bar_internal_storage){
            fragment.setCurStorage(StorageTypes.InternalStorage);
            storage = StorageTypes.InternalStorage;
            FileFoldersLab.get(fragment.getActivity()).
                    setCurPath(FileFoldersLab.get(fragment.getActivity()).getINTERNAL_STORAGE_PATH());
            fragment.mSpinner.setSelection(0);
            fragment.mRecyclerView.getLayoutManager().scrollToPosition(0);
            fragment.updateUI(false);
        }
        if(item.getItemId() == R.id.action_bar_sd_card){
            if(StorageHelper.get(fragment.getActivity()).getAllPaths().size() > 1){
                if(FileFoldersLab.get(fragment.getActivity()).getSDCardUri().equals(FileFoldersLab.NOT_FOUND)){
                    FileFoldersLab.getSDCardAccess(fragment);
                }else{
                    fragment.setCurStorage(StorageTypes.SDCard);
                    storage = StorageTypes.SDCard;
                    FileFoldersLab.get(fragment.getActivity()).
                            setCurPath(FileFoldersLab.get(fragment.getActivity()).getSDCardPath());
                    fragment.mSpinner.setSelection(1);
                    fragment.mRecyclerView.getLayoutManager().scrollToPosition(0);
                    fragment.updateUI(false);
                }
            }
        }
        if(item.isCheckable() && !item.isChecked()){
            if(item.getItemId() == R.id.menu_sort_name){
                fragment.setSortMode(FileExplorerFragment.SortModes.ByName);
                item.setChecked(true);
                fragment.updateUI(false);
            }
            if(item.getItemId() == R.id.menu_sort_date){
                fragment.setSortMode(FileExplorerFragment.SortModes.ByDate);
                item.setChecked(true);
                fragment.updateUI(false);
            }
        }
        if(item.getItemId() == R.id.menu_sort_desc){
            if(item.isChecked()){
                fragment.setDescendingSortMode(false);
                item.setChecked(false);
                fragment.updateUI(false);
            }else{
                fragment.setDescendingSortMode(true);
                item.setChecked(true);
                fragment.updateUI(false);
            }
        }
    }
}
