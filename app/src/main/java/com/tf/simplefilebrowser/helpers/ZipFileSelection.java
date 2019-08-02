package com.tf.simplefilebrowser.helpers;

import android.content.Context;

import java.util.LinkedList;

public class ZipFileSelection{
    private Context mContext;
    private LinkedList<String> mSelectedFiles;
    /*private static ZipFileSelection mZipFileSelection;
    public static ZipFileSelection get(Context context){
        if(mZipFileSelection == null){
            mZipFileSelection = new ZipFileSelection(context);
        }
        return mZipFileSelection;
    }*/
    public ZipFileSelection(Context context){
        mContext = context;
        mSelectedFiles = new LinkedList<>();
    }

    public void addFileToSelection(String name){
        mSelectedFiles.add(name);
    }
    public void removeFileFromSelection(String name){
        mSelectedFiles.remove(name);
    }
    public LinkedList<String> getSelectedFiles(){
        return mSelectedFiles;
    }
    public boolean fileIsSelected(String name){
        for (String i:
             mSelectedFiles) {
            if(i.equals(name))
                return true;
        }
        return false;
    }
}
