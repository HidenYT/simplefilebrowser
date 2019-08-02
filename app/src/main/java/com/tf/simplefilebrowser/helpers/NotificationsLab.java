package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tf.simplefilebrowser.R;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Set;

public class NotificationsLab {
    private static Context mContext;
    private static NotificationsLab mNotificationsLab;
    protected static final String EXTRA_PROCESS_THREAD = "EXTRA_PROCESS_THREAD";
    protected static final String EXTRA_NOTIFICATION_THREAD = "EXTRA_NOTIFICATION_THREAD";
    protected static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";
    public static NotificationsLab get(Context context){
        if(mNotificationsLab == null)
            mNotificationsLab = new NotificationsLab(context);
        return  mNotificationsLab;
    }
    private NotificationsLab(Context context){
        mContext = context;
    }
    public static class notificationReceiver extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            Long notificationThreadId = intent.getLongExtra(EXTRA_NOTIFICATION_THREAD, -1);
            Long processThreadId = intent.getLongExtra(EXTRA_PROCESS_THREAD, -1);
            Set<Thread> setOfThreads = Thread.getAllStackTraces().keySet();
            for(Thread t : setOfThreads){
                if(t.getId() == processThreadId || t.getId() == notificationThreadId){
                    t.interrupt();
                }
            }
        }
    }
    public int createProgressNotification(final Long threadId, final File src,
                                           final File dest,
                                           String notificationTitle, String notificationText,
                                           final Context context){
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        final NotificationCompat.Builder builder =  new NotificationCompat.Builder(context, "CopyMoveNotification");
        final int PROGRESS_MAX = 100;
        final int notificationId = (int) (Math.random()*100000);
        builder.setOngoing(true)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_paste_button)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setProgress(PROGRESS_MAX, 0, false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setAutoCancel(true);

        final Intent snoozeIntent = new Intent(context, NotificationsLab.notificationReceiver.class);
        snoozeIntent.setAction("CancelCopying");
        snoozeIntent.putExtra(EXTRA_PROCESS_THREAD, threadId);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        final long srcFileSize;
        if(src.isDirectory()){
            srcFileSize = FileUtils.sizeOfDirectory(src);
        }else{
            srcFileSize = src.length();
        }
        new Thread(new Runnable() {
            @Override
            public void run() {
                int CUR_PROGRESS;
                Long threadId = Thread.currentThread().getId();
                snoozeIntent.putExtra(EXTRA_NOTIFICATION_THREAD, threadId);
                PendingIntent snoozePendingIntent =
                        PendingIntent.getBroadcast(context, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_paste_button, "Cancel", snoozePendingIntent);
                notificationManager.notify(notificationId, builder.build());

                while (srcFileSize != 0){
                    long destFileSize;
                    if(dest.isDirectory())
                        destFileSize = FileUtils.sizeOfDirectory(dest);
                    else
                        destFileSize = dest.length();

                    if(Thread.currentThread().isInterrupted()){
                        notificationManager.cancel(notificationId);
                        break;
                    }
                    CUR_PROGRESS = Math.round(100 * destFileSize/srcFileSize);
                    builder.setProgress(PROGRESS_MAX, CUR_PROGRESS, false);
                    notificationManager.notify(notificationId, builder.build());
                    if(destFileSize == srcFileSize){
                        notificationManager.cancel(notificationId);
                        break;
                    }
                    try {

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notificationManager.cancel(notificationId);
                        break;
                    }
                }
                if(srcFileSize == 0){
                    notificationManager.cancel(notificationId);
                }
            }
        }).start();
        return notificationId;
    }

    public int createProgressMultipleFiles(final long threadId, final LinkedList<String> src,
                                           final LinkedList<String> dest, String title,
                                           String text, final Activity activity){
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        final NotificationCompat.Builder builder =  new NotificationCompat.Builder(activity, "CopyMoveNotification");
        final int PROGRESS_MAX = 100;
        final int notificationId = (int) (Math.random()*100000);
        builder.setOngoing(true)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_paste_button)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setProgress(PROGRESS_MAX, 0, false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setAutoCancel(true);

        final Intent snoozeIntent = new Intent(activity, NotificationsLab.notificationReceiver.class);
        snoozeIntent.setAction("CancelCopying");
        snoozeIntent.putExtra(EXTRA_PROCESS_THREAD, threadId);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationId);
        long srcFilesSize = 0;
        for(String i : src){
            File f = new File(i);
            if(f.isDirectory()){
                srcFilesSize += FileUtils.sizeOfDirectory(f);
            }else{
                srcFilesSize += f.length();
            }
        }

        final long finalSrcFilesSize = srcFilesSize;
        new Thread(new Runnable() {
            @Override
            public void run() {
                int CUR_PROGRESS;
                Long threadId = Thread.currentThread().getId();
                snoozeIntent.putExtra(EXTRA_NOTIFICATION_THREAD, threadId);
                PendingIntent snoozePendingIntent =
                        PendingIntent.getBroadcast(activity, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_paste_button, "Cancel", snoozePendingIntent);
                notificationManager.notify(notificationId, builder.build());

                while (finalSrcFilesSize != 0){
                    long destFileSize = 0;
                    for(String i : dest){
                        File f = new File(i);
                        if(f.isDirectory()){
                            destFileSize += FileUtils.sizeOfDirectory(f);
                        }else{
                            destFileSize += f.length();
                        }
                    }
                    if(Thread.currentThread().isInterrupted()){
                        notificationManager.cancel(notificationId);
                        break;
                    }
                    CUR_PROGRESS = Math.round(100 * destFileSize/ finalSrcFilesSize);
                    builder.setProgress(PROGRESS_MAX, CUR_PROGRESS, false);
                    notificationManager.notify(notificationId, builder.build());
                    if(destFileSize == finalSrcFilesSize){
                        notificationManager.cancel(notificationId);
                        break;
                    }
                    try {

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notificationManager.cancel(notificationId);
                        break;
                    }
                }
                if(finalSrcFilesSize == 0){
                    notificationManager.cancel(notificationId);
                }
            }
        }).start();
        return notificationId;
    }
}
