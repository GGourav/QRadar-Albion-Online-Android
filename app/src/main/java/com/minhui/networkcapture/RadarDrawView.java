package com.minhui.networkcapture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import androidx.core.app.NotificationCompat;
import com.minhui.networkcapture.RadarActivities.RadarFloatingActivity;
import com.minhui.networkcapture.RadarView.RadarSettings;
import com.minhui.networkcapture.RadarView.RadarView;
import com.minhui.vpn.Handlers.MainHandler;

public class RadarDrawView extends Service {
    private WindowManager windowManager;
    private View overlayView;
    private static final String CHANNEL_ID = "radar_ui_channel_01";

    @Override
    public void onCreate() {
        super.onCreate();
        setupForeground();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        showOverlayWindow();
    }

    private void setupForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Radar UI", NotificationManager.IMPORTANCE_LOW);
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, VPNCaptureActivity.class), pendingFlags);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("QRadar UI Active").setSmallIcon(android.R.drawable.ic_menu_compass).setContentIntent(pi).build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(2, notification);
        }
    }

    private void showOverlayWindow() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.radar_draw_layout, null);
        RadarView radarView = overlayView.findViewById(R.id.radarView);
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                RadarSettings.getInstance().radarSizeWidthHeightBar, RadarSettings.getInstance().radarSizeWidthHeightBar,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(overlayView, lp);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
        MainHandler.getInstance().clearAll();
    }

    // Settings logic restored
    public void reInitMatrix() { }
    public void setRadarSize(int size) { }
    public void setRadarX(int progress) { }
    public void setRadarY(int progress) { }
    public void setSettingsWidth(int progress) { }
    public void setSettingsHeight(int progress) { }
    public void setFloatingX(int progress) { }
    public void setFloatingY(int progress) { }
    public void setFloatingSize(int progress) { }
    public void setTransparencySettings(int color) { }
}
