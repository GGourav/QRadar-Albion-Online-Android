package com.minhui.networkcapture;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
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
    private RadarView radarDrawView;
    private boolean showSettings = false;
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
                .setContentTitle("Radar Active").setSmallIcon(R.drawable.logo).setContentIntent(pi).build();

        if (Build.VERSION.SDK_INT >= 34) startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        else startForeground(2, notification);
    }

    private void showOverlayWindow() {
        try {
            overlayView = LayoutInflater.from(this).inflate(R.layout.radar_draw_layout, null);
            overlaySettingsView = LayoutInflater.from(this).inflate(R.layout.radar_settings_view, null);
            radarFloatingSettingsView = LayoutInflater.from(this).inflate(R.layout.activity_radar_floating_settings, null);

            radarDrawView = overlayView.findViewById(R.id.radarView);

            // 1. Radar Window (Large Box)
            WindowManager.LayoutParams lpRadar = new WindowManager.LayoutParams(
                    600, 600, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            lpRadar.gravity = Gravity.CENTER;
            windowManager.addView(overlayView, lpRadar);

            // 2. Settings Container (Hidden by default)
            WindowManager.LayoutParams lpPanel = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, PixelFormat.TRANSLUCENT);
            windowManager.addView(radarFloatingSettingsView, lpPanel);
            radarFloatingSettingsView.setVisibility(View.GONE);
            new RadarFloatingActivity(radarFloatingSettingsView, LayoutInflater.from(this), radarDrawView, this);

            // 3. Settings Button (Clickable Icon)
            WindowManager.LayoutParams lpBtn = new WindowManager.LayoutParams(
                    150, 150, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, PixelFormat.TRANSLUCENT);
            lpBtn.gravity = Gravity.TOP | Gravity.END;
            windowManager.addView(overlaySettingsView, lpBtn);

            // FIX: Make button clickable to show/hide settings
            overlaySettingsView.setOnClickListener(v -> {
                showSettings = !showSettings;
                radarFloatingSettingsView.setVisibility(showSettings ? View.VISIBLE : View.GONE);
            });

        } catch (Exception e) { e.printStackTrace(); }
    }

    // Restore required methods
    public void setRadarSize(int s) {}
    public void setRadarX(int p) {}
    public void setRadarY(int p) {}
    public void reInitMatrix() {}
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
        if (radarFloatingSettingsView != null) windowManager.removeView(radarFloatingSettingsView);
    }
}
