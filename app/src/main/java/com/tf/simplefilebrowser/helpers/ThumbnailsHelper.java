package com.tf.simplefilebrowser.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

import java.util.HashMap;

public class ThumbnailsHelper {
    private static final int THUMB_SIZE = 48;
    private static HashMap<String, Bitmap> generatedThumbnails =
            new HashMap<>();
    public static Bitmap createThumbForPic(String picPath){
        if(generatedThumbnails.containsKey(picPath)){
            return generatedThumbnails.get(picPath);
        }
        Bitmap btm = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picPath),
                THUMB_SIZE, THUMB_SIZE);
        generatedThumbnails.put(picPath, btm);
        return btm;
    }
    public static Bitmap createThumbForVideo(String videoPath){
        if(generatedThumbnails.containsKey(videoPath)){
            return generatedThumbnails.get(videoPath);
        }
        Bitmap btm = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
        generatedThumbnails.put(videoPath, btm);
        return btm;
    }
}
