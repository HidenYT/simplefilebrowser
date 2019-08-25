package com.touchforce.pathselectiondialog;

import android.os.Environment;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

class FilesHelper {
    private final String EXTERNAL_STORAGE_PATH;
    private static FilesHelper filesHelper;

    FilesHelper() {
        EXTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    }

    public String getEXTERNAL_STORAGE_PATH() {
        return EXTERNAL_STORAGE_PATH;
    }

    public static FilesHelper get(){
        if(filesHelper == null)
            filesHelper = new FilesHelper();
        return filesHelper;
    }
    static LinkedList<File> getFilesInPath(String path){
        File f = new File(path);
        File[] files = f.listFiles();
        assert files != null;
        LinkedList<File> l = new LinkedList<>(Arrays.asList(files));
        l = sortFilesByName(l);
        l = filterFolders(l);
        return l;
    }

    private static LinkedList<File> sortFilesByName(LinkedList<File> files){
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return file.getName().toLowerCase().compareTo(t1.getName().toLowerCase());
            }
        });
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return Boolean.toString(file.isFile()).compareTo(Boolean.toString(t1.isFile()));
            }
        });
        return files;
    }
    private static LinkedList<File> filterFolders(LinkedList<File> files){
        for(int i = 0; i < files.size(); i++){
            if(files.get(i).isFile()){
                files.remove(files.get(i));
            }
        }
        return files;
    }

}
