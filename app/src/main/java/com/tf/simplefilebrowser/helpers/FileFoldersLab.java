package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.v4.provider.DocumentFile;
import android.util.Log;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.activities.ArchiveViewActivity;
import com.tf.simplefilebrowser.fragments.FileExplorerFragment;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;


public class FileFoldersLab {
    private static Activity mContext;
    private final String INTERNAL_STORAGE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private final String APP_FOLDER_PATH = INTERNAL_STORAGE_PATH + File.separator + "Simple File Browser";
    public final String TMP_FOLDER_PATH = APP_FOLDER_PATH + File.separator + "tmp";
    private static FileFoldersLab sFileFoldersLab;
    private static Activity mActivity;
    private final String TAG = "TAG";
    public static final String NOT_FOUND = "not_found";
    public String getCurPath() {
        return mCurPath;
    }

    public void setCurPath(String curPath) {
        mCurPath = curPath;
    }

    private String mCurPath;

    public static FileFoldersLab get(Activity context) {
        if (sFileFoldersLab == null) {
            sFileFoldersLab = new FileFoldersLab(context);
        }
        return sFileFoldersLab;
    }
    public String getINTERNAL_STORAGE_PATH(){
        return INTERNAL_STORAGE_PATH;
    }
    public String getSDCardPath(){
        SharedPreferences sp = mContext.getSharedPreferences("Prefs", 0);
        return sp.getString("SD_Card_Path", NOT_FOUND);
    }
    public void setSDCardPath(String path){
        SharedPreferences sp = mContext.getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("SD_Card_Path", path).commit();
    }
    public String getSDCardUri(){
        SharedPreferences sp = mContext.getSharedPreferences("Prefs", 0);
        return sp.getString("SD_Card_Uri", NOT_FOUND);
    }
    public void setSDCardUri(String path){
        SharedPreferences sp = mContext.getSharedPreferences("Prefs", 0);
        SharedPreferences.Editor spe = sp.edit();
        spe.putString("SD_Card_Uri", path).commit();
    }
    public FileFoldersLab(Activity context) {

        mContext = context;

        mActivity = (Activity) mContext;
        try {
            mCurPath = Environment.getExternalStorageDirectory().getAbsolutePath();
            String sd = "";
            if(context.getExternalFilesDirs("").length > 1)
                sd = context.getExternalFilesDirs("")[1].getAbsolutePath();
            if(!sd.equals("")){
                setSDCardPath(sd.substring(0,sd.indexOf("/Android/")));
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public List<File> loadFilesFromPath() {
        List<File> mFileFolders = new LinkedList<>();
        File folder = new File(mCurPath);
        File[] files = folder.listFiles();
        for(int i =0; i < files.length; i++){
            mFileFolders.add(files[i]);
        }
        mFileFolders = sortFilesList(mFileFolders);
        return mFileFolders;
    }
    public List<File> loadFilesFromPath(String path) {
        File folder = new File(path);
        File[] files = folder.listFiles();
        List<File> mFileFolders = new LinkedList<>(Arrays.asList(files));
        mFileFolders = sortFilesList(mFileFolders);
        return mFileFolders;
    }

    public static LinkedList<File> loadFilesNoSort(String path){
        File folder = new File(path);
        File[] files = folder.listFiles();
        return new LinkedList<>(Arrays.asList(files));
    }

    public static List<File> sortFilesByDate(List<File> files){
        List<File> fileList = files;
        Collections.sort(fileList, new Comparator<File>() {
            @Override
            public int compare(File file, File t1) {
                return Long.toString(file.lastModified())
                        .compareTo(Long.toString(t1.lastModified()));
            }
        });
        return fileList;
    }

    public String prevPath() {
        String[] pathArray = mCurPath.split("/");
        String finPath = "";
        pathArray[pathArray.length - 1] = "";
        for (int i = 0; i < pathArray.length; i++) {
            if (pathArray[i] != "")
                finPath += pathArray[i] + "/";
        }
        return finPath;
    }
    public static LinkedList<ZipEntry> sortZipEntries(LinkedList<ZipEntry> entries) {
        LinkedList<ZipEntry> fileFolder = entries;
        Collections.sort(fileFolder, new Comparator<ZipEntry>() {
            @Override
            public int compare(ZipEntry o1, ZipEntry o2) {
                return o1.getName().toLowerCase().
                        compareTo(o2.getName().toLowerCase());
            }
        });
        Collections.sort(fileFolder, new Comparator<ZipEntry>() {
            @Override
            public int compare(ZipEntry o1, ZipEntry o2) {
                return Boolean.toString(!o1.isDirectory()).compareTo(Boolean.toString(!o2.isDirectory()));
            }
        });
        return fileFolder;
    }

    public static List<File> sortFilesList(List<File> fileFolders) {
        List<File> fileFolder = fileFolders;
        Collections.sort(fileFolder, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return o1.getName().toLowerCase().
                        compareTo(o2.getName().toLowerCase());
            }
        });
        Collections.sort(fileFolder, new Comparator<File>() {
            @Override
            public int compare(File o1, File o2) {
                return Boolean.toString(o1.isFile()).compareTo(Boolean.toString(o2.isFile()));
            }
        });
        return fileFolder;
    }

    public void createFile(String name) {
        Log.d(TAG, "createFile: " + mCurPath);
        File file = new File(mCurPath + File.separator + name);
        if(!file.exists()){
            if(!mCurPath.startsWith(getSDCardPath())){
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                //File fold = file.getParentFile();
                DocumentFile a = StorageHelper.get(mContext).getDocumentFile(file);
                a.createFile("text/*", name);
            }
        }else{
            final Toast toast = Toast.makeText(mContext, R.string.file_already_exists, Toast.LENGTH_SHORT);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.show();
                }
            });
        }
    }

    public void createFolder(String folderName) {
        File dir = new File(mCurPath + File.separator + folderName);
        if (!dir.exists()) {
            if(!mCurPath.startsWith(getSDCardPath())){
                dir.mkdirs();
            }else {
                DocumentFile a = StorageHelper.get(mContext).getDocumentFile(dir);
                a.createDirectory(folderName);
            }
        } else {
            final Toast toast = Toast.makeText(mContext, R.string.folder_already_exists, Toast.LENGTH_SHORT);
            mContext.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    toast.show();
                }
            });
        }
    }

    public static void removeFile(String path) {
        File file = new File(path);
        if(file.exists() &&
                file.getAbsolutePath().startsWith(FileFoldersLab.get(mActivity).getINTERNAL_STORAGE_PATH())){
            if(!path.startsWith(get(mActivity).getSDCardPath())){
                if (file.isDirectory() && file.listFiles().length != 0) {
                    File[] files = file.listFiles();
                    for (File i : files) {
                        removeFile(i.getAbsolutePath());
                    }
                }
                if (file.isFile() || (file.isDirectory() && file.listFiles().length == 0)) {
                    file.delete();
                }
            }
        }
    }
    public static void removeFileSD(String path){
        File f = new File(path);
        if(f.exists()
                && f.getAbsolutePath().startsWith(FileFoldersLab.get(mActivity).getSDCardPath())){
            if(f.isFile()){
                DocumentFile d1 = StorageHelper.get(mActivity).getDocumentFile(f);
                d1.delete();
            }
            if(f.isDirectory()){
                File[] list = f.listFiles();
                if(list.length != 0){
                    for(File i : list){
                        if(i.isDirectory()){
                            removeFileSD(i.getAbsolutePath());
                        }else{
                            DocumentFile d1 = StorageHelper.get(mActivity).getDocumentFile(i);
                            d1.delete();
                        }
                    }
                }
                if(f.listFiles().length== 0){
                    DocumentFile d1 = StorageHelper.get(mActivity).getDocumentFile(f);
                    d1.delete();
                }
            }
        }
    }
    public void prepareEnvironment(){
        try {
            File AppFolder = new File(APP_FOLDER_PATH);
            if(!AppFolder.exists()){
                AppFolder.mkdirs();
            }
            removeTmpFolder();
            File tmpFolder = new File(TMP_FOLDER_PATH);
            tmpFolder.mkdirs();
            File nomediaFile = new File(tmpFolder.getAbsolutePath() + File.separator + MediaStore.MEDIA_IGNORE_FILENAME);
            nomediaFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void removeTmpFolder(){
        File tmpFolder = new File(TMP_FOLDER_PATH);
        if(tmpFolder.exists()){
            removeFile(tmpFolder.getAbsolutePath());
        }
    }
    public String createTmpFolderForArchiveContent(File zipFile){
        File p = new File(TMP_FOLDER_PATH + File.separator +  zipFile.getName());
        p.mkdirs();
        return p.getAbsolutePath();
    }
    public void openFile(File file){
        Uri uri = Uri.fromFile(file);
        String mimeType =  getFileMimeType(file);
        Intent intent = new Intent();
        if(mimeType == null) {
            mimeType = "text/*";
        }
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, mimeType);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        if(mimeType.split("/")[0].equals("text")){

        }
        if(mimeType.equals("application/zip")){
            Intent i = ArchiveViewActivity.newIntent(mActivity, new File((file.getPath())));
            mActivity.startActivity(i);
            mActivity.overridePendingTransition(0,0);
        }else{
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
            mActivity.startActivity(intent);
        }
    }

    public static String getFileMimeType(File file){
        Uri uri = Uri.fromFile(file);
        String extension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
        String mimeType =  MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType;
    }

    public static int getAllFilesAmount(LinkedList<String> src){
        int total = 0;
        for (String s:
             src) {
            total++;
            File f = new File(s);
            if(f.isDirectory()){
                LinkedList<String> tmp = new LinkedList<>();
                for (File j: f.listFiles()) {
                    tmp.add(j.getPath());
                }
                total+=getAllFilesAmount(tmp);
            }
        }
        return total;
    }

    public static void getSDCardAccess(FileExplorerFragment context){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        context.startActivityForResult(intent, FileExplorerFragment.REQUEST_SD_CARD_PATH);
    }
    public static void getSDCardAccess(Activity context, int requestCode){
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        context.startActivityForResult(intent, requestCode);
    }

    public static String getFileMD5(File f) {
        StringBuilder result = new StringBuilder();
        try {
            FileInputStream fis = new FileInputStream(f);
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[8192];
            int numOfBytesRead;
            while ((numOfBytesRead = fis.read(buffer)) > 0) {
                md.update(buffer, 0, numOfBytesRead);
            }
            byte[] hash = md.digest();
            for (int i = 0; i < hash.length; i++) {
                result.append(Integer.toString((hash[i] & 0xff) + 0x100, 16).substring(1));
            }
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return result.toString();
    }
}

