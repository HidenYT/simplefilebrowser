package com.tf.simplefilebrowser.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import com.tf.simplefilebrowser.viewholders.FileExplorerViewHolder;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThumbnailsHelper {
    private ExecutorService threadPool = Executors.newFixedThreadPool(2);
    private HashMap<String, FileExplorerViewHolder> addedInQueue = new HashMap<>();
    private static ThumbnailsHelper thumbnailsHelper;
    public static ThumbnailsHelper get(){
        if(thumbnailsHelper == null)
            thumbnailsHelper = new ThumbnailsHelper();
        return thumbnailsHelper;
    }
    private final int THUMB_SIZE = 48;
    private HashMap<String, Bitmap> generatedThumbnails =
            new HashMap<>();

    public Bitmap createThumbForPic(String picPath){
        if(generatedThumbnails.containsKey(picPath)){
            return generatedThumbnails.get(picPath);
        }
        Bitmap btm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picPath),
                THUMB_SIZE, THUMB_SIZE);
        generatedThumbnails.put(picPath, btm);
        //notifyAllListeners(picPath);
        return btm;
    }
    public Bitmap createThumbForVideo(String videoPath){
        if(generatedThumbnails.containsKey(videoPath)){
            return generatedThumbnails.get(videoPath);
        }
        Bitmap btm = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
        generatedThumbnails.put(videoPath, btm);
        //notifyAllListeners(videoPath);
        return btm;
    }

    public void addFileInQueue(final FileExplorerViewHolder vh, final File file){
        String p = file.getAbsolutePath();
        if((!addedInQueue.containsKey(p))||(addedInQueue.containsKey(p) && addedInQueue.get(p)!=vh)){
            addedInQueue.put(p, vh);
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    Bitmap btm = createBitmapForMedia(file);
                    if(btm != null){
                        if(vh.getFile() == file){
                            vh.setIcon(btm);
                        }
                    }
                    addedInQueue.remove(file.getAbsolutePath());
                }
            });
        }
    }
    private Bitmap createBitmapForMedia(File media){
        if(FileFoldersLab.getFileMimeType(media) != null){
            Bitmap btm = null;
            if(FileFoldersLab.getFileMimeType(media).startsWith("image/")){
                btm = createThumbForPic(media.getAbsolutePath());
            }
            if(FileFoldersLab.getFileMimeType(media).startsWith("video/")){
                btm = createThumbForVideo(media.getAbsolutePath());
            }
            return btm;
        }
        return null;
    }
}
