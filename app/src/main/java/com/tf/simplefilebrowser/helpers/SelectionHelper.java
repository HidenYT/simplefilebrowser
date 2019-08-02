package com.tf.simplefilebrowser.helpers;

import android.content.Context;

import java.util.LinkedList;

public class SelectionHelper {
    private static SelectionHelper sSelectionHelper;
    private Context mContext;
    private LinkedList<String> mSelectedFiles;
    public LinkedList<String> getSelectedFiles() {
        return mSelectedFiles;
    }

    public static SelectionHelper get(Context context){
        if(sSelectionHelper == null){
            sSelectionHelper = new SelectionHelper(context);
        }
        return sSelectionHelper;
    }
    public SelectionHelper(Context context){
        mContext = context;
        mSelectedFiles = new LinkedList<>();

    }
    public void addSelectedFile(String filePath){
        mSelectedFiles.add(filePath);
    }
    public void removeSelectedFile(String filePath){
        mSelectedFiles.remove(filePath);
    }
    public boolean isSelected(String searchPath){
        for(String i : mSelectedFiles){
            if(searchPath.equals(i))
                return true;
        }
        return false;
    }
}
