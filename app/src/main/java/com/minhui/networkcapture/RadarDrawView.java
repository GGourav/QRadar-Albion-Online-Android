package com.minhui.networkcapture;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
    private View overlaySettingsView;
    private View radarFloatingSettingsView;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager.LayoutParams layoutParamsSettingsView;
    private RadarView radarDrawView;
    private static final String CHANNEL_ID = "radar_ui_channel_01";

    @Override
    public void onCreate() {
        super.onCreate();
        setupForeground();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        showOverlayWindow();
    }

    private void setupForeground() {
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Radar UI", NotificationManager.IMPORTANCE_LOW);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, VPNCaptureActivity.class), pendingFlags);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Albion Radar UI Active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pi)
                .build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE);
        } else {
            startForeground(2, notification);
        }
    }

    private void showOverlayWindow() {
        try {
            overlayView = LayoutInflater.from(this).inflate(R.layout.radar_draw_layout, null);
            overlaySettingsView = LayoutInflater.from(this).inflate(R.layout.radar_settings_view, null);
            radarFloatingSettingsView = LayoutInflater.from(this).inflate(R.layout.activity_radar_floating_settings, null);

            // FIX: Force a default size of 400x400 if settings are 0
            int size = RadarSettings.getInstance().radarSizeWidthHeightBar;
            if (size <= 0) size = 400; 

            layoutParams = new WindowManager.LayoutParams(
                    size, size,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);

            radarDrawView = overlayView.findViewById(R.id.radarView);
            windowManager.addView(overlayView, layoutParams);

            // Settings Button Window
            layoutParamsSettingsView = new WindowManager.LayoutParams(
                    150, 150,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            windowManager.addView(overlaySettingsView, layoutParamsSettingsView);
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Required methods for the build to succeed
    public void reInitMatrix() { if (radarDrawView != null) radarDrawView.initMatrix(); }
    public void setRadarSize(int size) { layoutParams.width = size; layoutParams.height = size; windowManager.updateViewLayout(overlayView, layoutParams); }
    public void setRadarX(int p) { layoutParams.x = p; windowManager.updateViewLayout(overlayView, layoutParams); }
    public void setRadarY(int p) { layoutParams.y = p; windowManager.updateViewLayout(overlayView, layoutParams); }
    public void setSettingsWidth(int p) {}
    public void setSettingsHeight(int p) {}
    public void setFloatingX(int p) {}
    public void setFloatingY(int p) {}
    public void setFloatingSize(int p) {}
    public void setTransparencySettings(int c) {}

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (overlayView != null) windowManager.removeView(overlayView);
        if (overlaySettingsView != null) windowManager.removeView(overlaySettingsView);
    }
}
