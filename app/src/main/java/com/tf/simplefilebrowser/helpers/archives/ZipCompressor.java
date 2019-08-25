package com.tf.simplefilebrowser.helpers.archives;

import android.app.Activity;
import android.content.ContentResolver;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.tf.simplefilebrowser.helpers.FileFoldersLab;
import com.tf.simplefilebrowser.helpers.StorageHelper;
import com.tf.simplefilebrowser.helpers.ZipArchiveHelper;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipProcess;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipCompressor implements Runnable, ZipProcess {
    private static final String TAG = "TAG";
    private final ZipCompressor initObject;
    public int totalFilesAmount;
    public int compressedFilesAmount;

    private LinkedList<String> files;
    private String zipFile;
    private String zipCurPath;
    private ZipOutputStream out;
    private String initPath;
    private Activity activity;
    private ContentResolver contentResolver;
    public ZipCompressor(Activity activity, LinkedList<String> files, String zipFile,
                         String zipCurPath, ZipOutputStream out, String initPath,
                         ContentResolver contentResolver, ZipCompressor initObject){
        this.files = files;
        this.zipFile = zipFile;
        this.zipCurPath = zipCurPath;
        this.out = out;
        this.initPath = initPath;
        this.activity = activity;
        this.contentResolver = contentResolver;
        if(initObject == null)
            this.initObject = this;
        else
            this.initObject = initObject;
        this.totalFilesAmount = FileFoldersLab.getAllFilesAmount(files);
    }
    public double getProgress(){
        return ((double)compressedFilesAmount/totalFilesAmount);
    }


    @Override
    public void run() {
        boolean startsWith = initPath.startsWith(FileFoldersLab.get(activity).getSDCardPath());
        try {
            BufferedInputStream origin;
            OutputStream os;
            ZipOutputStream zos;
            if(out == null){
                File f = new File(zipFile);
                if(startsWith){
                    DocumentFile d = StorageHelper.get(activity).getDocumentFile(f);
                    DocumentFile df = d.createFile("application/zip", f.getName());
                    os = contentResolver.openOutputStream(df.getUri());
                }else{
                    os  = new FileOutputStream(zipFile + File.separator + zipCurPath);
                }
                zos = new ZipOutputStream(new BufferedOutputStream(os));
            }else{
                zos = out;
            }

            byte[] bytes = new byte[2048];
            for(String f:files){
                if(Thread.currentThread().isInterrupted())
                    return;
                Log.d(TAG, "Start of createZipFromFiles: " + f);
                File file = new File(f);
                ZipEntry zipEntry;
                if(file.isDirectory()){
                    LinkedList<String> contents = new LinkedList<>();
                    for (File k: file.listFiles()) {
                        contents.add(k.getAbsolutePath());
                        Log.d(TAG, file.getName() + ":" + k.getName());
                    }
                    zos.putNextEntry(new ZipEntry(zipCurPath + file.getName() + File.separator));
                    String p = zipCurPath + file.getName() + File.separator;
                    new ZipCompressor(activity, contents, zipFile, p, zos, initPath, contentResolver,
                            initObject).run();
                    if(initObject == null)
                        compressedFilesAmount++;
                    else
                        initObject.compressedFilesAmount++;
                    //createZipFromFiles(contents,zipFile, zipCurPath + file.getName() + File.separator, zos, initPath);
                }else{
                    FileInputStream fis = new FileInputStream(file);
                    Log.d("TAG", "Creating File: " + zipCurPath + f.substring(f.lastIndexOf("/")+1));
                    origin=new BufferedInputStream(fis, 2048);
                    zipEntry = new ZipEntry(zipCurPath + f.substring(f.lastIndexOf("/")+1));
                    zos.putNextEntry(zipEntry);
                    int length;
                    while ((length = origin.read(bytes)) > 0) {
                        if(Thread.currentThread().isInterrupted())
                            return;
                        zos.write(bytes, 0, length);
                    }
                    if(initObject == null)
                        compressedFilesAmount++;
                    else
                        initObject.compressedFilesAmount++;
                    origin.close();
                }
            }
            if(zipCurPath.equals("")){
                zos.finish();
                zos.close();
            }
        }catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
