package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.storage.StorageManager;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;


public class StorageHelper {
    private static StorageHelper mStorageHelper;
    private Activity mActivity;
    private String TAG = "TAG";
    StorageHelper(Activity activity){
        mActivity = activity;
    }
    public static StorageHelper get(Activity activity){
        if(mStorageHelper == null)
            mStorageHelper = new StorageHelper(activity);
        return mStorageHelper;
    }
    StorageManager getStorageManager() {
        return (StorageManager) mActivity.getSystemService(Context.STORAGE_SERVICE);
    }
    public List<String> getAllPaths() {
        List<String> allPaths = new ArrayList<>();
        try {
            Class<?> storageVolumeClass = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = getStorageManager().getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClass.getMethod("getPath");
            Method getState = storageVolumeClass.getMethod("getState");
            Object getVolumeResult = getVolumeList.invoke(getStorageManager());
            final int length = Array.getLength(getVolumeResult);

            for (int i = 0; i < length; ++i) {
                Object storageVolumeElem = Array.get(getVolumeResult, i);
                String mountStatus = (String) getState.invoke(storageVolumeElem);
                if (mountStatus != null && mountStatus.equals("mounted")) {
                    String path = (String) getPath.invoke(storageVolumeElem);
                    if (path != null) {
                        allPaths.add(path);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return allPaths;
    }
    public boolean isFileWritable(File file) {
        boolean writable;
        try {
            new FileOutputStream(file, true).close();
            writable = file.exists() && file.canWrite();
        } catch (IOException e) {
            writable = false;
        }
        return writable;
    }

    public DocumentFile getDocumentFile(File file) {
        DocumentFile document = null;
        String baseFolder = null;
        for (String path : getAllPaths()) {
            Log.d(TAG, "getDocumentFile: " + path);
            File filePath = new File(path);
            if (file.getAbsolutePath().startsWith(filePath.getAbsolutePath())) {
                baseFolder = filePath.getAbsolutePath();
                break;
            }
        }
        if (baseFolder == null) {
            return null;
        }
        try {
            String relativePath = file.getCanonicalPath().substring(baseFolder.length() + 1);
            SharedPreferences sp = mActivity.getSharedPreferences("Prefs", 0);

            Uri permissionUri = Uri.parse(sp.getString("SD_Card_Uri", "not_found"));
            document = getDocumentFileForUri(permissionUri, relativePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    /*
        Метод для получения DocumentFile (шаги 4-6)
    */
    protected DocumentFile getDocumentFileForUri(Uri treeUri, String relativePath) {
        String[] parts = relativePath.split("/");
        if (parts.length == 0) {
            return null;
        }
        DocumentFile document = DocumentFile.fromTreeUri(mActivity, treeUri);
        for (String part : parts) {
            DocumentFile nextDocument = document.findFile(part);
            if (nextDocument != null) {
                document = nextDocument;
            }
        }
        return document;
    }
}
