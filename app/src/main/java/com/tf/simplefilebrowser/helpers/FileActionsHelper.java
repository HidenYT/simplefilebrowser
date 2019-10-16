package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import com.tf.simplefilebrowser.R;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;

public class FileActionsHelper {
    private String TAG = "TAG";
    private ContentResolver mContentResolver;
    enum optionsFiles{
        Rewrite,
        NotRewrite,
        None
    }
    enum optionsFolders{
        Merge,
        NotMerge,
        None
    }
    private optionsFiles mOptionFiles = optionsFiles.None;
    private optionsFolders mOptionFolders = optionsFolders.None;
    public FileActionsHelper(ContentResolver contentResolver){
        mContentResolver = contentResolver;
    }
    public class copyFileClass{
        private boolean deleteOnFinish = false;
        File src;
        String destPath;
        Activity mActivity;
        public copyFileClass(File src, String destPath, Activity activity){
            this.src = src;
            this.destPath = destPath;
            mActivity = activity;
        }
        public copyFileClass(File src, String destPath, Activity activity, boolean deleteOnFinish){
            this.src = src;
            this.destPath = destPath;
            mActivity = activity;
            this.deleteOnFinish = deleteOnFinish;
        }
        public final Runnable copyFile = new Runnable() {
            @Override
            public void run() {
                final File f = new File(destPath + File.separator + src.getName());
                if(f.exists()){
                    if(mOptionFiles == optionsFiles.None){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(mActivity.getString(R.string.rewrite_file_title));
                        View v = LayoutInflater.from(mActivity).inflate(R.layout.rewrite_file_dialog, null);
                        String s = mActivity.getString(R.string.rewrite_file, f.getAbsolutePath());
                        TextView t = v.findViewById(R.id.rewrite_file_dialog_filename);
                        t.setText(s);
                        final CheckBox cb = v.findViewById(R.id.rewrite_file_dialog_checkbox);
                        builder.setPositiveButton(mActivity.getString(R.string.DIALOG_OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                synchronized (copyFile){
                                    copyFile.notify();
                                }
                                if(cb.isChecked()){
                                    mOptionFiles = optionsFiles.Rewrite;
                                }
                                writeFile(src, f);
                                if(deleteOnFinish){
                                    checkAndDeleteFile(src, mActivity);
                                }
                            }
                        });
                        builder.setNegativeButton(mActivity.getString(R.string.DIALOG_NO), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(cb.isChecked()){
                                    mOptionFiles = optionsFiles.NotRewrite;
                                }
                                synchronized (copyFile){
                                    copyFile.notify();
                                }
                            }
                        });
                        builder.setView(v);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.create();
                                builder.show();
                            }
                        });

                        synchronized (copyFile){
                            try {
                                copyFile.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if(mOptionFiles == optionsFiles.Rewrite){
                        writeFile(src, f);
                        if(deleteOnFinish){
                            checkAndDeleteFile(src, mActivity);
                        }
                    }
                }else{
                    writeFile(src, f);
                    if(deleteOnFinish){
                        checkAndDeleteFile(src, mActivity);
                    }
                }
            }
        };

        public final Runnable copyFileToSD = new Runnable() {
            @Override
            public void run() {
                final File f = new File(destPath + File.separator + src.getName());
                if(f.exists()){
                    final DocumentFile doc = StorageHelper.get(mActivity).getDocumentFile(f);
                    if(mOptionFiles == optionsFiles.None){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(mActivity.getString(R.string.rewrite_file_title));
                        View v = LayoutInflater.from(mActivity).inflate(R.layout.rewrite_file_dialog, null);
                        String s = mActivity.getString(R.string.rewrite_file, f.getAbsolutePath());
                        TextView t = v.findViewById(R.id.rewrite_file_dialog_filename);
                        t.setText(s);
                        final CheckBox cb = v.findViewById(R.id.rewrite_file_dialog_checkbox);
                        builder.setPositiveButton(mActivity.getString(R.string.DIALOG_OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                synchronized (copyFile){
                                    copyFile.notify();
                                }
                                if(cb.isChecked()){
                                    mOptionFiles = optionsFiles.Rewrite;
                                }
                                writeFile(src, doc);
                                if(deleteOnFinish){
                                    checkAndDeleteFile(src, mActivity);
                                }
                            }
                        });
                        builder.setNegativeButton(mActivity.getString(R.string.DIALOG_NO), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(cb.isChecked()){
                                    mOptionFiles = optionsFiles.NotRewrite;
                                }
                                synchronized (copyFile){
                                    copyFile.notify();
                                }
                            }
                        });
                        builder.setView(v);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.create();
                                builder.show();
                            }
                        });

                        synchronized (copyFile){
                            try {
                                copyFile.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }else if(mOptionFiles == optionsFiles.Rewrite){
                        writeFile(src, doc);
                        if(deleteOnFinish){
                            checkAndDeleteFile(src, mActivity);
                        }
                    }
                }else{
                    final DocumentFile doc = StorageHelper.get(mActivity).getDocumentFile(f);
                    String type = mContentResolver.getType(Uri.fromFile(src));
                    assert type != null;
                    final DocumentFile doc1 = doc.createFile(type, f.getName());
                    Log.d(TAG, "run: " + doc1.getUri());
                    writeFile(src, doc1);
                    if(deleteOnFinish){
                        checkAndDeleteFile(src, mActivity);
                    }
                }

            }
        };
    }

    public class copyFolderClass{
        private boolean deleteOnFinish;
        File src;
        String destPath;
        Activity mActivity;
        boolean canMerge = false;
        public copyFolderClass(File src, String destPath, Activity activity){
            this.src = src;
            this.destPath = destPath;
            mActivity = activity;
        }
        public copyFolderClass(File src, String destPath, Activity activity, boolean deleteOnFinish){
            this.src = src;
            this.destPath = destPath;
            mActivity = activity;
            this.deleteOnFinish = deleteOnFinish;
        }
        public final Runnable copyFolder = new Runnable() {
            @Override
            public void run() {
                final File f = new File(destPath + File.separator + src.getName());
                if(f.exists()){
                    if(mOptionFolders == optionsFolders.None){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(mActivity.getString(R.string.merge_folders_title));
                        View v = LayoutInflater.from(mActivity).inflate(R.layout.rewrite_file_dialog, null);
                        String s = mActivity.getString(R.string.merge_folders, f.getAbsolutePath(), src.getAbsolutePath());
                        TextView t = v.findViewById(R.id.rewrite_file_dialog_filename);
                        t.setText(s);
                        final CheckBox cb = v.findViewById(R.id.rewrite_file_dialog_checkbox);
                        builder.setPositiveButton(mActivity.getString(R.string.DIALOG_OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                canMerge = true;
                                if(cb.isChecked()){
                                    mOptionFolders = optionsFolders.Merge;
                                }
                                synchronized (copyFolder){
                                    copyFolder.notify();
                                }
                            }
                        });
                        builder.setNegativeButton(mActivity.getString(R.string.DIALOG_NO), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(cb.isChecked()){
                                    mOptionFolders = optionsFolders.NotMerge;
                                }
                                synchronized (copyFolder){
                                    copyFolder.notify();
                                }
                            }
                        });
                        builder.setView(v);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.create();
                                builder.show();
                            }
                        });

                        synchronized (copyFolder){
                            try {
                                copyFolder.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(canMerge){
                            if(Thread.currentThread().isInterrupted()){
                                synchronized (copyFolder){
                                    try {
                                        copyFolder.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return;
                            }
                            makeFolder(src, f, mActivity);
                            if(deleteOnFinish){
                                checkAndDeleteFile(src, mActivity);
                            }
                        }
                    }else if(mOptionFolders == optionsFolders.Merge){
                        makeFolder(src, f, mActivity);
                        if(deleteOnFinish){
                            checkAndDeleteFile(src, mActivity);
                        }
                    }
                }else{
                    if(Thread.currentThread().isInterrupted()){
                        synchronized (copyFolder){
                            try {
                                copyFolder.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return;
                    }
                    makeFolder(src, f, mActivity);
                    if(deleteOnFinish){
                        checkAndDeleteFile(src, mActivity);
                    }
                }
            }
        };

        public final Runnable copyFolderToSD = new Runnable() {
            @Override
            public void run() {
                final File f = new File(destPath + File.separator + src.getName());
                if(f.exists()){
                    final DocumentFile doc = StorageHelper.get(mActivity).getDocumentFile(f);
                    if(mOptionFolders == optionsFolders.None){
                        final AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
                        builder.setTitle(mActivity.getString(R.string.merge_folders_title));
                        View v = LayoutInflater.from(mActivity).inflate(R.layout.rewrite_file_dialog, null);
                        String s = mActivity.getString(R.string.merge_folders, f.getAbsolutePath(), src.getAbsolutePath());
                        TextView t = v.findViewById(R.id.rewrite_file_dialog_filename);
                        t.setText(s);
                        final CheckBox cb = v.findViewById(R.id.rewrite_file_dialog_checkbox);
                        builder.setPositiveButton(mActivity.getString(R.string.DIALOG_OK), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                canMerge = true;
                                if(cb.isChecked()){
                                    mOptionFolders = optionsFolders.Merge;
                                }
                                synchronized (copyFolder){
                                    copyFolder.notify();
                                }
                            }
                        });
                        builder.setNegativeButton(mActivity.getString(R.string.DIALOG_NO), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(cb.isChecked()){
                                    mOptionFolders = optionsFolders.NotMerge;
                                }
                                synchronized (copyFolder){
                                    copyFolder.notify();
                                }
                            }
                        });
                        builder.setView(v);
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                builder.create();
                                builder.show();
                            }
                        });

                        synchronized (copyFolder){
                            try {
                                copyFolder.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        if(canMerge){
                            if(Thread.currentThread().isInterrupted()){
                                synchronized (copyFolder){
                                    try {
                                        copyFolder.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return;
                            }
                            makeFolder(src, f, doc, mActivity);
                            if(deleteOnFinish){
                                checkAndDeleteFile(src, mActivity);
                            }
                        }
                    }else if(mOptionFolders == optionsFolders.Merge){
                        makeFolder(src, f, doc, mActivity);
                        if(deleteOnFinish){
                            checkAndDeleteFile(src, mActivity);
                        }
                    }
                }else{
                    if(Thread.currentThread().isInterrupted()){
                        synchronized (copyFolder){
                            try {
                                copyFolder.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        return;
                    }
                    final DocumentFile doc = StorageHelper.get(mActivity).getDocumentFile(f);

                    makeFolder(src, f, doc, mActivity);
                    if(deleteOnFinish){
                        checkAndDeleteFile(src, mActivity);
                    }
                }
            }
        };
    }

    private void writeFile(File src, File dest){
        try {
            InputStream in = new FileInputStream(src);
            BufferedInputStream bis = new BufferedInputStream(in);
            FileOutputStream fos = new FileOutputStream(dest);
            byte[] buf = new byte[8192];
            int len;
            while((len = bis.read(buf))>0){
                if(Thread.currentThread().isInterrupted()){
                    return;
                }
                fos.write(buf,0,len);
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    private void writeFile(File src, DocumentFile dest){
        FileInputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = new FileInputStream(src);
            Log.d(TAG, "writeFile: " + dest.getUri());
            outputStream = (FileOutputStream) mContentResolver.openOutputStream(dest.getUri());
            byte[] buf = new byte[8192];
            int len;
            while((len = inputStream.read(buf))>0){
                if(Thread.currentThread().isInterrupted()){
                    return;
                }
                outputStream.write(buf,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeFolder(File src, File dest, Activity activity){

        dest.mkdirs();
        File[] contents = src.listFiles();
        for(File file : contents){
            if(file.isDirectory()){
                new copyFolderClass(file, dest.getAbsolutePath(), activity).copyFolder.run();
            }else{
                new copyFileClass(file, dest.getAbsolutePath(), activity).copyFile.run();
            }
        }
    }

    private void makeFolder(File src, File destFile, DocumentFile destDoc, Activity activity){

        destDoc.createDirectory(src.getName());
        File[] contents = src.listFiles();
        for(File file : contents){
            if(file.isDirectory()){
                new copyFolderClass(file, destFile.getAbsolutePath(), activity).copyFolderToSD.run();
            }else{
                new copyFileClass(file, destFile.getAbsolutePath(), activity).copyFileToSD.run();
            }
        }
    }
    private void checkAndDeleteFile(File file, Activity activity){
        String path = file.getAbsolutePath();
        if (path.startsWith(FileFoldersLab.get(activity).getSDCardPath())){
            FileFoldersLab.removeFileSD(path);
        }else if(path.startsWith(FileFoldersLab.get(activity).getINTERNAL_STORAGE_PATH())){
            FileFoldersLab.removeFile(path);
        }
    }
}
