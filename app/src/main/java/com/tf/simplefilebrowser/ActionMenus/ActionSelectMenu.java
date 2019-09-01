package com.tf.simplefilebrowser.ActionMenus;

import android.app.Activity;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;
import com.tf.simplefilebrowser.helpers.SelectionHelper;

public class ActionSelectMenu implements ActionMode.Callback {
    private FileExplorerFragment fragment;
    private MenuItem mMenuItem = null;
    private Activity activity;
    private boolean deleteSelection = true;


    public void setDeleteSelection(boolean deleteSelection) {
        this.deleteSelection = deleteSelection;
    }

    public ActionSelectMenu(FileExplorerFragment fragment){
        this.fragment = fragment;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        activity = fragment.getActivity();
        fragment.setActionSelectActionMode(mode);
        mMenuItem = null;
        activity.getMenuInflater().inflate(R.menu.multiple_files_action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.copy_button_action_menu){
            mMenuItem = item;
            activity.startActionMode(new CopyMultipleActionMenu(fragment));
        }
        if(item.getItemId() == R.id.move_button_action_menu) {
            mMenuItem = item;
            activity.startActionMode(new MoveMultipleActionMenu(fragment));
        }
        if(item.getItemId() == R.id.compress_button_action_menu){
            fragment.compressSelectedFiles(SelectionHelper.get(activity).getSelectedFiles(), this);
        }
        if(item.getItemId() == R.id.delete_button_action_menu){
            fragment.deleteSelectedFiles();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        fragment.setActionSelectActionMode(null);
        if(mMenuItem == null && deleteSelection)
            SelectionHelper.get(activity).getSelectedFiles().clear();
        mMenuItem = null;
        fragment.updateUI(false);
    }
}
