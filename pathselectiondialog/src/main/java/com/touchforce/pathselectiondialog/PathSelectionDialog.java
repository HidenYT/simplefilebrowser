package com.touchforce.pathselectiondialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;

import java.io.File;

public class PathSelectionDialog {
    private Activity activity;
    private String dialogTitle;

    void setCurPath(String curPath) {
        this.curPath = curPath;
    }
    String getCurPath() {
        return this.curPath;
    }
    private String curPath;
    private OnPathSelectedListener pathSelectedListener;

    PathSelectionDialogAdapter getAdapter() {
        return adapter;
    }

    private PathSelectionDialogAdapter adapter;
    public PathSelectionDialog(Activity activity, String title){
        this.activity = activity;
        this.dialogTitle = title;
        curPath = FilesHelper.get().getEXTERNAL_STORAGE_PATH();
    }

    public void setOnSelectedListener(OnPathSelectedListener listener){
        pathSelectedListener = listener;
    }

    public void createDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(dialogTitle);
        View v = LayoutInflater.from(activity).inflate(R.layout.layout_dialog_path_selection,
                null,false);
        RecyclerView r = v.findViewById(R.id.main_recycler_view);
        r.setLayoutManager(new LinearLayoutManager(activity));
        adapter = new PathSelectionDialogAdapter(FilesHelper.getFilesInPath(curPath), this);
        builder.setPositiveButton(R.string.here_button, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                pathSelectedListener.onPathSelected(curPath);
            }
        });
        builder.setNegativeButton(R.string.cancel_button, null);
        builder.setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                if(keyEvent.getAction() == KeyEvent.ACTION_UP && keyEvent.getKeyCode() == KeyEvent.KEYCODE_BACK){
                    if(!curPath.equals(FilesHelper.get().getEXTERNAL_STORAGE_PATH())
                            && !(curPath+ File.separator).equals(FilesHelper.get().getEXTERNAL_STORAGE_PATH())){
                        curPath = curPath.substring(0, curPath.length()-1);
                        curPath = curPath.substring(0, curPath.lastIndexOf("/")+1);
                        adapter.setFiles(FilesHelper.getFilesInPath(curPath));
                        adapter.notifyDataSetChanged();
                    }
                }
                return true;
            }
        });
        r.setAdapter(adapter);
        builder.setView(v);
        builder.create().show();
    }
}
