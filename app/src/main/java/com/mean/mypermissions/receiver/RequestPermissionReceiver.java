package com.mean.mypermissions.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.mean.mypermissions.MainActivity;
import com.mean.mypermissions.PermissionModeActivity;
import com.mean.mypermissions.R;
import com.mean.mypermissions.utils.AppUtil;

import static android.content.Context.NOTIFICATION_SERVICE;

public class RequestPermissionReceiver extends BroadcastReceiver {
    public static final String TAG = "RequestPermissionRecv";
    public static final String INTENT_EXTRA_APP_NAME = "appName";
    public static final String INTENT_EXTRA_PERMISSION_NAME = "permissionName";

    private Context context;
    private static final String CHANNEL_ID = "channel_1";
    private static final String NOTIFICATION_DESC = "权限请求类通知";
    private String appName;
    private String appPermission;
    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;
        appName = intent.getStringExtra(INTENT_EXTRA_APP_NAME);
        Log.d(TAG, "onReceive: appName："+appName);
        appName = AppUtil.getAppName(context,appName);
        appPermission = intent.getStringExtra(INTENT_EXTRA_PERMISSION_NAME);
        showNotification(intent); //弹出通知
        //TODO 记录日志
    }

    private void showNotification(Intent intent)
    {
        NotificationManager manager = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        Intent intent1 = new Intent(context, PermissionModeActivity.class);  // 跳到PermissionModeActivity处理
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent1, 0);
        if(Build.VERSION.SDK_INT >= 26)    //SDK 26+ 通知渠道
        {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, NOTIFICATION_DESC, NotificationManager.IMPORTANCE_HIGH);
            channel.enableVibration(true);
            channel.setSound(null, Notification.AUDIO_ATTRIBUTES_DEFAULT);
            manager.createNotificationChannel(channel);
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle("已假允许" + appName + " 申请以下权限：")
                    .setContentText(appPermission)
                    //setContentIntent(pendingIntent)
                    //.setFullScreenIntent(pendingIntent,true)
                    .setAutoCancel(true)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .build();
            manager.notify(1, notification);
        }
        else{  //sdk<26
            Notification notification = new NotificationCompat.Builder(context)
                    .setContentTitle("已假允许" + appName + " 申请以下权限：")
                    .setContentText(appPermission)
                    //.setContentIntent(pendingIntent)
                    //setFullScreenIntent(pendingIntent, true)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setPriority(NotificationManager.IMPORTANCE_HIGH)
                    .build();
            manager.notify(1,notification);
        }
    }
}
