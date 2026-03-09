package com.minhui.networkcapture;

import android.content.Intent;
import android.net.Uri;
import android.os.Build; // FIX: Added missing import
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ImageView;
import androidx.annotation.Keep;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.minhui.networkcapture.Fragments.MainFragment;
import com.minhui.vpn.utils.VpnServiceHelper;
import com.minhui.vpn.Handlers.MainHandler;

@Keep
public class VPNCaptureActivity extends FragmentActivity {
    private ImageView vpnButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_capture);
        
        // Initialize the Albion Database
        MainHandler.getInstance().initDatabase(this);

        vpnButton = findViewById(R.id.vpn);
        vpnButton.setOnClickListener(v -> {
            boolean isRunning = VpnServiceHelper.vpnRunningStatus();
            VpnServiceHelper.changeVpnRunningStatus(this, !isRunning);
            
            if (!isRunning) {
                // Delay showing window slightly so VPN can establish
                vpnButton.postDelayed(this::startRadarWindowService, 1000);
            }
        });

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
        }

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_vp, new MainFragment())
                .commit();
    }

    private void startRadarWindowService() {
        Intent intent = new Intent(this, RadarDrawView.class);
        // FIX: Start service correctly based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ContextCompat.startForegroundService(this, intent);
        } else {
            startService(intent);
        }
    }
}
