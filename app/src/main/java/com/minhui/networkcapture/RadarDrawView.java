package com.minhui.networkcapture;

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
                .setContentTitle("QRadar is Active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setContentIntent(pi)
                .setPriority(NotificationCompat.PRIORITY_LOW)
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

            // DIAGNOSTIC FIX: Give it a visible red border so we can see it on iQOO
            overlayView.setBackgroundColor(Color.argb(50, 255, 0, 0)); // Semi-transparent red

            WindowManager.LayoutParams lp = new WindowManager.LayoutParams(
                    500, 500, // Force visible size
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT);
            
            lp.gravity = Gravity.CENTER; // Put it in the middle of the screen
            windowManager.addView(overlayView, lp);

            // Settings Button (Small square in top right)
            WindowManager.LayoutParams lpSettings = new WindowManager.LayoutParams(
                    120, 120,
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                    PixelFormat.TRANSLUCENT);
            lpSettings.gravity = Gravity.TOP | Gravity.END;
            windowManager.addView(overlaySettingsView, lpSettings);

        } catch (Exception e) { e.printStackTrace(); }
    }

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

    // Required Stubs
    public void reInitMatrix() {}
    public void setRadarSize(int s) {}
    public void setRadarX(int p) {}
    public void setRadarY(int p) {}
    public void setSettingsWidth(int p) {}
    public void setSettingsHeight(int p) {}
    public void setFloatingX(int p) {}
    public void setFloatingY(int p) {}
    public void setFloatingSize(int p) {}
    public void setTransparencySettings(int c) {}
}
