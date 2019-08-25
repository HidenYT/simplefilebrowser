package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.provider.DocumentFile;
import android.util.Log;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.activities.FileExplorerActivity;

import org.apache.commons.io.FileUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;


public class ZipArchiveHelper {
    private final ContentResolver mContentResolver;
    private Activity mContext;
    private final String TAG = "TAG";
    public static ZipArchiveHelper mZipArchiveHelper;
    public static ZipArchiveHelper get(Activity context, ContentResolver contentResolver){
        if(mZipArchiveHelper == null)
            mZipArchiveHelper = new ZipArchiveHelper(context, contentResolver);
        return mZipArchiveHelper;
    }
    ZipArchiveHelper(Activity context, ContentResolver contentResolver){
        mContext = context;
        mContentResolver = contentResolver;
    }
    public void createZipFromFiles(LinkedList<String> files, String zipFile, String zipCurPath, ZipOutputStream out, String initPath) {
        boolean startsWith = initPath.startsWith(FileFoldersLab.get(mContext).getSDCardPath());
        try {
            BufferedInputStream origin;
            OutputStream os;
            ZipOutputStream zos;
            if(out == null){
                File f = new File(zipFile);
                if(startsWith){
                    DocumentFile d = StorageHelper.get(mContext).getDocumentFile(f);
                    DocumentFile df = d.createFile("application/zip", f.getName());
                    os = mContentResolver.openOutputStream(df.getUri());
                }else{
                    os  = new FileOutputStream(zipFile + File.separator + zipCurPath);
                }
                zos = new ZipOutputStream(new BufferedOutputStream(os));
            }else{
                zos = out;
            }

            byte[] bytes = new byte[2048];
            for(String f:files){
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
                    createZipFromFiles(contents,zipFile, zipCurPath + file.getName() + File.separator, zos, initPath);
                }else{
                    FileInputStream fis = new FileInputStream(file);
                    Log.d("TAG", "Creating File: " + zipCurPath + f.substring(f.lastIndexOf("/")+1));
                    origin=new BufferedInputStream(fis, 2048);
                    zipEntry = new ZipEntry(zipCurPath + f.substring(f.lastIndexOf("/")+1));
                    zos.putNextEntry(zipEntry);
                    int length;
                    while ((length = origin.read(bytes)) > 0) {
                        zos.write(bytes, 0, length);
                    }
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
    public class zipNotification extends AsyncTask<File, Integer, Void> {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(mContext);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, "CopyMoveNotification");
        int PROGRESS_MAX = 100;
        int CUR_PROGRESS = 0;
        int oldProgress;
        Long threadId;
        int notifId;
        ArrayList<String> srcFiles;
        Long totalSrcFilesSize = 0L;
        public zipNotification(Long threadId, ArrayList<String> src) {
            this.threadId = threadId;
            this.srcFiles = src;
            for(String i:src){
                File k = new File(i);
                Log.d("TAG", i);
                if(k.isDirectory()){
                    totalSrcFilesSize+= FileUtils.sizeOfDirectory(k);
                }else{
                    totalSrcFilesSize+=k.length();
                }
            }
        }
        public void onPreExecute() {
            builder.setContentTitle("Files compressing")
                    .setContentText("Compressing in progress")
                    .setSmallIcon(R.drawable.ic_paste_button)
                    .setPriority(NotificationCompat.PRIORITY_LOW)
                    .setOnlyAlertOnce(true)
                    .setOngoing(true);
            builder.setProgress(PROGRESS_MAX, CUR_PROGRESS, false);
            notifId = (int) (Math.random() * 100000);
            Intent snoozeIntent = new Intent(mContext, FileExplorerActivity.class);
            snoozeIntent.setAction("CancelZipping");
            snoozeIntent.putExtra("EXTRA_NOTIF_THREAD_FILES", threadId);
            PendingIntent snoozePendingIntent =
                    PendingIntent.getActivity(mContext, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.addAction(R.drawable.ic_paste_button, "Cancel", snoozePendingIntent);
            notificationManager.notify(notifId, builder.build());
        }
        @Override
        protected Void doInBackground(File... zipFile) {
            try {
                FileOutputStream fos = new FileOutputStream(zipFile[0].getPath());
                ZipOutputStream zos = new ZipOutputStream(fos);
                for(String f:srcFiles){
                    File file = new File(f);
                    FileInputStream fis = new FileInputStream(file);
                    ZipEntry zipEntry = new ZipEntry(f.substring(f.lastIndexOf("/")+1));
                    zos.putNextEntry(zipEntry);
                    byte[] bytes = new byte[1024];
                    int length;
                    while ((length = fis.read(bytes)) >= 0) {
                        if(Thread.currentThread().interrupted()){
                            notificationManager.cancel(notifId);
                            cancel(true);
                            break;
                        }
                        CUR_PROGRESS = Math.round(((float) zipFile[0].length()) / totalSrcFilesSize * 100);
                        if(CUR_PROGRESS >= oldProgress + 5){
                            oldProgress = CUR_PROGRESS;
                            publishProgress(CUR_PROGRESS);
                        }
                        zos.write(bytes, 0, length);
                    }
                    zos.closeEntry();
                    fis.close();
                }
                zos.finish();
                fos.close();
                zos.close();
            }catch (FileNotFoundException e){
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            builder.setProgress(PROGRESS_MAX, progress[0], false);
            notificationManager.notify(notifId, builder.build());
        }
        @Override
        protected void onPostExecute(Void result){
            notificationManager.cancel(notifId);
        }
    }

    public void unzip(String zipFilePath, String destPath) {
        try {
            FileInputStream fis = new FileInputStream(zipFilePath);
            ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
            ZipEntry entry;
            byte[] buff = new byte[2048];
            int count;
            while ((entry = zis.getNextEntry()) != null) {
                String fileName = entry.getName();
                if (entry.isDirectory()) {
                    File f = new File(destPath + File.separator + fileName);
                    f.mkdirs();
                    continue;
                }
                FileOutputStream fos = new FileOutputStream(destPath  + File.separator + fileName);
                while ((count = zis.read(buff)) != -1) {
                    fos.write(buff, 0, count);
                }
                fos.close();
                zis.closeEntry();
            }
            zis.close();
            fis.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void unzipFile(String zipFilePath, String destPath, String fileName) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()){
                entry = entries.nextElement();
                String fName = entry.getName();
                if(fName.equals(fileName)){
                    FileOutputStream fos = new FileOutputStream(destPath  + File.separator +
                            fileName.substring(fileName.lastIndexOf("/")+1));
                    int count;
                    byte[] buff = new byte[4096];
                    InputStream is = zipFile.getInputStream(entry);
                    while ((count = is.read(buff)) != -1) {
                        fos.write(buff, 0, count);
                    }
                    fos.close();
                    is.close();
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
    public void unzipFiles(String zipFilePath, String destPath, LinkedList<String> files, String notInclude) {
        try {
            ZipFile zipFile = new ZipFile(zipFilePath);
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            ZipEntry entry;
            while (entries.hasMoreElements()){
                entry = entries.nextElement();
                String fName = entry.getName();

                if(!entry.isDirectory()){
                    if(files.contains(fName)){
                        fName = fName.substring(notInclude.length());
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
                            fos.write(buff, 0, count);
                        }
                        fos.close();
                        is.close();
                    }
                }else{
                    if(files.contains(fName)){
                        fName = fName.substring(notInclude.length());
                        File nFile = new File(destPath + File.separator + fName);
                        if(!nFile.exists()){
                            nFile.mkdirs();
                        }
                    }
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public LinkedList<ZipEntry> getAllEntries(String zipFilePath) throws IOException {
        LinkedList<ZipEntry> returnEntries = new LinkedList<>();
        ZipFile zipFile = new ZipFile(zipFilePath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        ZipEntry entry;
        while(entries.hasMoreElements()){
            returnEntries.add(entries.nextElement());
        }
        return returnEntries;
    }

    public static int getEntriesAmount(String zipFilePath) throws IOException {
        ZipFile f = new ZipFile(zipFilePath);
        return f.size();
    }
}
