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
import java.util.LinkedList;

public class CopyMultipleActionMenu extends ActionMenu {
    private FileExplorerFragment mFragment;
    private String copyingFilesText;
    private String copyingInProgress;
    CopyMultipleActionMenu(FileExplorerFragment fragment){
        super(fragment);
        mFragment = fragment;
        copyingFilesText = mFragment.getString(R.string.multiple_files_copying);
        copyingInProgress = mFragment.getString(R.string.copying_in_progress);
    }
    @Override
    public boolean onCreateActionMode(ActionMode actionMode, Menu menu) {
        mFragment.getActivity().getMenuInflater().inflate(R.menu.file_explorer_fragment_menu_paste,menu);
        mFragment.mFilesActionActive = true;
        return true;
    }

    @Override
    public boolean onActionItemClicked(final ActionMode actionMode, MenuItem menuItem) {
        if(menuItem.getItemId() == R.id.paste_button){
            setStorage(mFragment.getCurStorage());
            setMenuItem(menuItem);

            final String finalPath = FileFoldersLab.get(mFragment.getActivity()).getCurPath();
            final LinkedList<String> arr =
                    (LinkedList<String>) SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clone();

            final LinkedList<String> dests = new LinkedList<>();
            for (String i:
                 arr) {
                dests.add(finalPath + File.separator + new File(i).getName());
            }
            mFragment.getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionMode.finish();
                    SelectionHelper.get(mFragment.getActivity()).getSelectedFiles().clear();
                    mFragment.updateUI(false);
                }
            });

            new Thread(new Runnable() {
                @Override
                public void run() {
                    NotificationsLab.get(mFragment.getActivity()).createProgressMultipleFiles(
                            Thread.currentThread().getId(), arr, dests, copyingFilesText,
                            copyingInProgress,mFragment.getActivity()
                    );
                    FileActionsHelper fa = new FileActionsHelper(mFragment.getContentResolver());
                    for(String k : arr){
                        if(new File(k).isFile()){
                            FileActionsHelper.copyFileClass cfc = fa.new copyFileClass(
                                    new File(k), finalPath, mFragment.getActivity());
                            if(getStorage() == StorageTypes.InternalStorage){
                                cfc.copyFile.run();
                            }else {
                                if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                    cfc.copyFileToSD.run();
                                }
                            }
                        }else{
                            FileActionsHelper.copyFolderClass cfc = fa.new copyFolderClass(
                                    new File(k), finalPath, mFragment.getActivity());
                            if(getStorage() == StorageTypes.InternalStorage){
                                cfc.copyFolder.run();
                            } else {
                                if(!FileFoldersLab.get(mFragment.getActivity()).getSDCardUri().equals("not_found")){
                                    cfc.copyFolderToSD.run();
                                }
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
