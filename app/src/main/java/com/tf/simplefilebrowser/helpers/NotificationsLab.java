package com.tf.simplefilebrowser.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.tf.simplefilebrowser.R;
import com.tf.simplefilebrowser.helpers.archives.zip.ZipProcess;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.LinkedList;
import java.util.Set;

public class NotificationsLab {
    private final int PROGRESS_MAX = 100;
    private static Context mContext;
    private static NotificationsLab mNotificationsLab;
    private static final String EXTRA_PROCESS_THREAD = "EXTRA_PROCESS_THREAD";
    private static final String EXTRA_NOTIFICATION_THREAD = "EXTRA_NOTIFICATION_THREAD";
    private static final String EXTRA_NOTIFICATION_ID = "EXTRA_NOTIFICATION_ID";

    private LinkedList<Integer> takenNotifIds = new LinkedList<>();
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
            long notificationThreadId = intent.getLongExtra(EXTRA_NOTIFICATION_THREAD, -1);
            long processThreadId = intent.getLongExtra(EXTRA_PROCESS_THREAD, -1);
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
                                           final Context context) {
        String channelId = "CopyMoveNotification";
        createChannel(channelId);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        final NotificationCompat.Builder builder =  new NotificationCompat.Builder(context, channelId);
        final int notificationId = peekForFreeID();
        builder.setOngoing(true)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_MIN)
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
                builder.addAction(R.drawable.ic_notification, "Cancel", snoozePendingIntent);
                notificationManager.notify(notificationId, builder.build());

                while (srcFileSize != 0){
                    long destFileSize;
                    if(dest.isDirectory())
                        destFileSize = FileUtils.sizeOfDirectory(dest);
                    else
                        destFileSize = dest.length();

                    if(Thread.currentThread().isInterrupted()){
                        notificationManager.cancel(notificationId);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                        break;
                    }
                    CUR_PROGRESS = Math.round(100 * destFileSize/srcFileSize);
                    builder.setProgress(PROGRESS_MAX, CUR_PROGRESS, false);
                    notificationManager.notify(notificationId, builder.build());
                    if(destFileSize == srcFileSize){
                        notificationManager.cancel(notificationId);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                        break;
                    }
                    try {

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notificationManager.cancel(notificationId);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                        break;
                    }
                }
                if(srcFileSize == 0){
                    notificationManager.cancel(notificationId);
                    takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                }
            }
        }).start();
        return notificationId;
    }

    public int createProgressMultipleFiles(final long threadId, final LinkedList<String> src,
                                           final LinkedList<String> dest, String title,
                                           String text, final Activity activity){
        String channelId = "CopyMoveNotification";
        createChannel(channelId);
        final NotificationManagerCompat notificationManager = NotificationManagerCompat.from(activity);
        final NotificationCompat.Builder builder =  new NotificationCompat.Builder(activity, channelId);
        final int notificationId = peekForFreeID();
        builder.setOngoing(true)
                .setContentTitle(title)
                .setContentText(text)
                .setSmallIcon(R.drawable.ic_notification)
                .setSound(null)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_MIN)
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
                builder.addAction(R.drawable.ic_notification, "Cancel", snoozePendingIntent);
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
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                        break;
                    }
                    CUR_PROGRESS = Math.round(100 * destFileSize/ finalSrcFilesSize);
                    builder.setProgress(PROGRESS_MAX, CUR_PROGRESS, false);
                    notificationManager.notify(notificationId, builder.build());
                    if(destFileSize == finalSrcFilesSize){
                        notificationManager.cancel(notificationId);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                        break;
                    }
                    try {

                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        notificationManager.cancel(notificationId);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                        break;
                    }
                }
                if(finalSrcFilesSize == 0){
                    notificationManager.cancel(notificationId);
                    takenNotifIds.remove(takenNotifIds.indexOf(notificationId));
                }
            }
        }).start();
        return notificationId;
    }

    public void createZipProgress(final Long threadId, final ZipProcess compressor,
                                       String notificationTitle, String notificationText){
        String channelId = "ZipProcess";
        createChannel(channelId);
        final NotificationManagerCompat notificationManagerCompat = NotificationManagerCompat.from(mContext);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, channelId);
        final int notificationID = peekForFreeID();
        builder.setOngoing(true)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setSmallIcon(R.drawable.ic_notification)
                .setOnlyAlertOnce(true)
                .setSound(null)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setProgress(PROGRESS_MAX, 0, false)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notificationText))
                .setAutoCancel(true);
        final Intent snoozeIntent = new Intent(mContext, NotificationsLab.notificationReceiver.class);
        snoozeIntent.setAction("CancelCopying");
        snoozeIntent.putExtra(EXTRA_PROCESS_THREAD, threadId);
        snoozeIntent.putExtra(EXTRA_NOTIFICATION_ID, notificationID);
        new Thread(new Runnable() {
            @Override
            public void run() {
                Long threadId = Thread.currentThread().getId();
                snoozeIntent.putExtra(EXTRA_NOTIFICATION_THREAD, threadId);
                PendingIntent snoozePendingIntent =
                        PendingIntent.getBroadcast(mContext, 0, snoozeIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.addAction(R.drawable.ic_notification, "Cancel", snoozePendingIntent);

                while (true){
                    int CUR_PROGRESS = (int) Math.round(compressor.getProgress()*100);
                    if(Thread.currentThread().isInterrupted() || CUR_PROGRESS == 100){
                        notificationManagerCompat.cancel(notificationID);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationID));
                        break;
                    }
                    builder.setProgress(PROGRESS_MAX, CUR_PROGRESS, false);
                    notificationManagerCompat.notify(notificationID, builder.build());
                    try {
                        Thread.sleep(500);
                    }catch (InterruptedException e){
                        notificationManagerCompat.cancel(notificationID);
                        takenNotifIds.remove(takenNotifIds.indexOf(notificationID));
                        e.printStackTrace();
                        break;
                    }

                }
            }
        }).start();
    }

    private void createChannel(String channelId){
        CharSequence channelName = channelId;
        String channelDesc = "Simple File Browser actions";
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(channelId, channelName, importance);
            channel.setDescription(channelDesc);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = mContext.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            NotificationChannel currChannel = notificationManager.getNotificationChannel(channelId);
            if (currChannel == null)
                notificationManager.createNotificationChannel(channel);
        }
    }




    private int peekForFreeID() {
        if(takenNotifIds.size() == 100000){
            return -1;
        }
        int tmpId = (int) (Math.random()*100000);
        if(takenNotifIds.contains(tmpId)){
            return peekForFreeID();
        }
        takenNotifIds.add(tmpId);
        return tmpId;
    }
}
