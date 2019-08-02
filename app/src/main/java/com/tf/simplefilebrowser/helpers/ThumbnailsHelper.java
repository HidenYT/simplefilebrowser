package com.tf.simplefilebrowser.helpers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;

public class ThumbnailsHelper {
    private static final int THUMB_SIZE = 64;
    public static Bitmap createThumbForPic(String picPath){
        return ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(picPath),
                THUMB_SIZE, THUMB_SIZE);
    }
    public static Bitmap createThumbForVideo(String videoPath){
        return ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Video.Thumbnails.MICRO_KIND);
    }
}
