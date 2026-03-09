package com.minhui.networkcapture;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ServiceInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;

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
    private LinearLayout settings_edit;
    private LinearLayout content_layout;
    private WindowManager.LayoutParams layoutParams;
    private WindowManager.LayoutParams layoutParamsSettingsView;
    private RadarView radarDrawView;
    private boolean showRadarActivitySettings = false;
    private RadarDrawView radarDrawViewPseudoSingleton;
    private static final String CHANNEL_ID = "radar_ui_channel_01";

    @Override
    public void onCreate() {
        super.onCreate();
        radarDrawViewPseudoSingleton = this;
        setupForeground();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        showOverlayWindow();
    }

    private void setupForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "Radar UI", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        int pendingFlags = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ? PendingIntent.FLAG_IMMUTABLE : 0;
        PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, VPNCaptureActivity.class), pendingFlags);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("QRadar UI Active").setSmallIcon(R.drawable.logo).setContentIntent(pi).build();

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(2, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE);
        } else {
            startForeground(2, notification);
        }
    }

    private void showOverlayWindow() {
        overlayView = LayoutInflater.from(this).inflate(R.layout.radar_draw_layout, null);
        overlaySettingsView = LayoutInflater.from(this).inflate(R.layout.radar_settings_view, null);
        radarFloatingSettingsView = LayoutInflater.from(this).inflate(R.layout.activity_radar_floating_settings, null);

        settings_edit = radarFloatingSettingsView.findViewById(R.id.settings_edit);
        content_layout = radarFloatingSettingsView.findViewById(R.id.content_layout);
        radarDrawView = overlayView.findViewById(R.id.radarView);

        layoutParams = new WindowManager.LayoutParams(
                RadarSettings.getInstance().radarSizeWidthHeightBar, RadarSettings.getInstance().radarSizeWidthHeightBar,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        layoutParams.x = RadarSettings.getInstance().radarXBar;
        layoutParams.y = RadarSettings.getInstance().radarYBar;
        windowManager.addView(overlayView, layoutParams);

        WindowManager.LayoutParams lpSettingsFloating = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        windowManager.addView(radarFloatingSettingsView, lpSettingsFloating);
        radarFloatingSettingsView.setVisibility(View.GONE);
        new RadarFloatingActivity(radarFloatingSettingsView, LayoutInflater.from(this), radarDrawView, radarDrawViewPseudoSingleton);

        layoutParamsSettingsView = new WindowManager.LayoutParams(
                RadarSettings.getInstance().floatingWidthHeightBar, RadarSettings.getInstance().floatingWidthHeightBar,
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);
        layoutParamsSettingsView.x = RadarSettings.getInstance().floatingXBar;
        layoutParamsSettingsView.y = RadarSettings.getInstance().floatingYBar;
        windowManager.addView(overlaySettingsView, layoutParamsSettingsView);

        overlaySettingsView.setOnClickListener(v -> {
            showRadarActivitySettings = !showRadarActivitySettings;
            radarFloatingSettingsView.setVisibility(showRadarActivitySettings ? View.VISIBLE : View.GONE);
        });
    }

    // --- RESTORED METHODS FOR SETTINGS FRAGMENT ---
    public void reInitMatrix() { if (radarDrawView != null) radarDrawView.initMatrix(); }
    public void setRadarSize(int size) {
        layoutParams.width = size; layoutParams.height = size;
        windowManager.updateViewLayout(overlayView, layoutParams);
        reInitMatrix();
    }
    public void setRadarX(int progress) { layoutParams.x = progress; windowManager.updateViewLayout(overlayView, layoutParams); }
    public void setRadarY(int progress) { layoutParams.y = progress; windowManager.updateViewLayout(overlayView, layoutParams); }
    public void setSettingsWidth(int progress) {
        ViewGroup.LayoutParams lp = settings_edit.getLayoutParams();
        lp.width = progress; settings_edit.setLayoutParams(lp);
    }
    public void setSettingsHeight(int progress) {
        ViewGroup.LayoutParams lp = settings_edit.getLayoutParams();
        lp.height = progress; settings_edit.setLayoutParams(lp);
    }
    public void setFloatingX(int progress) { layoutParamsSettingsView.x = progress; windowManager.updateViewLayout(overlaySettingsView, layoutParamsSettingsView); }
    public void setFloatingY(int progress) { layoutParamsSettingsView.y = progress; windowManager.updateViewLayout(overlaySettingsView, layoutParamsSettingsView); }
    public void setFloatingSize(int progress) {
        layoutParamsSettingsView.width = progress; layoutParamsSettingsView.height = progress;
        windowManager.updateViewLayout(overlaySettingsView, layoutParamsSettingsView);
    }
    public void setTransparencySettings(int color) { if (content_layout != null) content_layout.setBackgroundColor(color); }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { return START_STICKY; }
    @Override
    public IBinder onBind(Intent intent) { return null; }
    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (overlayView != null) windowManager.removeView(overlayView);
            if (overlaySettingsView != null) windowManager.removeView(overlaySettingsView);
            if (radarFloatingSettingsView != null) windowManager.removeView(radarFloatingSettingsView);
        } catch (Exception e) {}
        MainHandler.getInstance().clearAll();
    }
}
