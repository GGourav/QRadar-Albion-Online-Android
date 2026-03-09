package com.minhui.vpn.service;

import static com.minhui.vpn.VPNConstants.VPN_SP_NAME;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import androidx.annotation.Keep;
import androidx.core.app.NotificationCompat;
import com.minhui.vpn.Packet;
import com.minhui.vpn.UDPServer;
import com.minhui.vpn.utils.VpnServiceHelper;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

@Keep public class FirewallVpnService extends VpnService implements Runnable {
    private boolean IsRunning = false;
    private Thread mVPNThread;
    private ParcelFileDescriptor mVPNInterface;
    private FileOutputStream mVPNOutputStream;
    private byte[] mPacket;
    private ConcurrentLinkedQueue<Packet> udpQueue;
    private FileInputStream in;
    private UDPServer udpServer;
    private final String selectPackage = "com.albiononline"; // Your corrected package
    public static final int MUTE_SIZE = 1500;

    @Override
    public void onCreate() {
        super.onCreate();
        setupNotification(); 
        VpnServiceHelper.onVpnServiceCreated(this);
        IsRunning = true;
        mVPNThread = new Thread(this, "VPNServiceThread");
        mVPNThread.start();
    }

    private void setupNotification() {
        String channelId = "vpn_channel";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Radar Service", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Albion Radar Active")
                .setSmallIcon(android.R.drawable.ic_menu_compass)
                .setOngoing(true)
                .build();
        startForeground(1, notification);
    }

    private ParcelFileDescriptor establishVPN() throws Exception {
        Builder builder = new Builder();
        builder.setMtu(MUTE_SIZE);
        builder.addAddress("10.0.0.2", 24);
        builder.addRoute("0.0.0.0", 0);
        builder.allowBypass(); // FIX: Keeps your game connected
        
        try {
            builder.addAllowedApplication(selectPackage);
            builder.addAllowedApplication(getPackageName());
        } catch (Exception e) { e.printStackTrace(); }

        builder.setSession("AlbionRadar");
        return builder.establish();
    }

    @Override
    public void run() {
        try {
            udpQueue = new ConcurrentLinkedQueue<>();
            udpServer = new UDPServer(this, udpQueue);
            udpServer.start();
            
            while (IsRunning) {
                if (mVPNInterface == null) {
                    mVPNInterface = establishVPN();
                    in = new FileInputStream(mVPNInterface.getFileDescriptor());
                    mVPNOutputStream = new FileOutputStream(mVPNInterface.getFileDescriptor());
                }

                mPacket = new byte[MUTE_SIZE];
                int size = in.read(mPacket);
                // The rest of the packet processing is handled by the vpnadaptercore
                Thread.sleep(10);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void onDestroy() {
        IsRunning = false;
        if (mVPNThread != null) mVPNThread.interrupt();
        try { if (mVPNInterface != null) mVPNInterface.close(); } catch (Exception e) {}
        super.onDestroy();
    }
}
