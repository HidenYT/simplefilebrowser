package com.tf.simplefilebrowser.helpers.archives.zip;

import android.util.Log;

import com.tf.simplefilebrowser.helpers.ZipArchiveHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipExtractor implements ZipProcess{


    private int filesExtracted;
    private int totalFilesAmount;

    private String zipFilePath;
    private String destPath;
    private LinkedList<String> files;
    private int notIncludeChars;
    public ZipExtractor(String zipFilePath, String destPath, LinkedList<String> files, String notIncludePath){
        this.zipFilePath = zipFilePath;
        this.destPath = destPath;
        this.files = files;
        this.notIncludeChars = notIncludePath.length();
        this.totalFilesAmount = files.size();
        filesExtracted = 0;
    }

    public double getProgress(){
        return ((double)filesExtracted/totalFilesAmount);
    }

    public void run(){
        try {
            for (String f:
                 files) {
                Log.d("TAG", "run: " + f);
            }
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()){
                entry = entries.nextElement();
                String fName = entry.getName();
                if(!entry.isDirectory()){
                    Log.d("TAG", "run: " + filesExtracted + "/" + totalFilesAmount);
                    if(files.contains(fName)){
                        fName = fName.substring(notIncludeChars);
                        File f = new File(destPath  + File.separator + fName);
                        if(!new File(f.getParent()).exists()){
                            new File(f.getParent()).mkdirs();
                        }
                        FileOutputStream fos = new FileOutputStream(destPath  + File.separator
                                + fName);
                        int count;
                        byte[] buff = new byte[4096];
                        InputStream is = zipFile.getInputStream(entry);
                        while ((count = is.read(buff)) != -1) {
                            if(Thread.currentThread().isInterrupted()){
                                return;
                            }
                            fos.write(buff, 0, count);
                        }
                        filesExtracted++;
                        fos.close();
                        is.close();
                    }
                }else{
                    if(files.contains(fName)){
                        fName = fName.substring(notIncludeChars);
                        File nFile = new File(destPath + File.separator + fName);
                        filesExtracted++;
                        if(!nFile.exists()){
                            if(Thread.currentThread().isInterrupted()){
                                return;
                            }
                            nFile.mkdirs();
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
