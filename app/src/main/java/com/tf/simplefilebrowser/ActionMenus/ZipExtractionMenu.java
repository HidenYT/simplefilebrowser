package com.tf.simplefilebrowser.ActionMenus;

import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuItem;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.activities.ArchiveViewActivity;
import com.touchforce.pathselectiondialog.OnPathSelectedListener;
import com.touchforce.pathselectiondialog.PathSelectionDialog;

import java.util.LinkedList;
import java.util.zip.ZipEntry;

public class ZipExtractionMenu implements ActionMode.Callback {

    private final ArchiveViewActivity activity;

    public ZipExtractionMenu(ArchiveViewActivity activity){
        this.activity = activity;
    }
    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        activity.setActionMode(mode);
        activity.getMenuInflater().inflate(R.menu.extract_action_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        if(item.getItemId() == R.id.extract_action_button){
            PathSelectionDialog psd = new PathSelectionDialog(activity, "Choose extraction path");
            psd.setOnSelectedListener(new OnPathSelectedListener() {
                @Override
                public void onPathSelected(String curPath) {
                    activity.startExtraction(curPath);
                }
            });
            psd.createDialog();
        }
        if(item.getItemId() == R.id.menu_select_all){
            LinkedList<String> all = activity.getNamesCurPath();
            activity.getSelection().getSelectedFiles().clear();
            LinkedList<ZipEntry> entries = activity.getEntries();
            for(ZipEntry ze : entries){
                if(all.contains(ze.getName())){
                    if(ze.isDirectory()){
                        for(ZipEntry i : entries){
                            if(i.getName().startsWith(ze.getName())
                                    && !i.getName().equals(ze.getName())){
                                activity.getSelection().addFileToSelection(i.getName());
                            }
                        }
                    }
                    activity.getSelection().addFileToSelection(ze.getName());
                }
            }
            activity.updateUI();
        }
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode mode) {
        activity.getSelection().getSelectedFiles().clear();
        activity.updateUI();
    }
}
