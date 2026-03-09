package com.minhui.networkcapture;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import com.minhui.networkcapture.Fragments.MainFragment;
import com.minhui.networkcapture.RadarView.RadarSettings;
import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.utils.VpnServiceHelper;
import com.minhui.vpn.Handlers.MainHandler;

@Keep
public class VPNCaptureActivity extends FragmentActivity {
    private ImageView vpnButton;
    private Handler handler;
    Intent windowService;

    ProxyConfig.VpnStatusListener vpnStatusListener = new ProxyConfig.VpnStatusListener() {
        @Override
        public void onVpnStart(Context context) {
            handler.post(()->vpnButton.setImageResource(R.mipmap.ic_stop));
            startRadarWindowService();
        }
        @Override
        public void onVpnEnd(Context context) {
            handler.post(()->vpnButton.setImageResource(R.mipmap.ic_start));
            if(windowService!=null) stopService(windowService);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_capture);
        handler = new Handler();
        MainHandler.getInstance().initDatabase(this);

        vpnButton = findViewById(R.id.vpn);
        vpnButton.setOnClickListener(v -> {
          if(VpnServiceHelper.vpnRunningStatus()) VpnServiceHelper.changeVpnRunningStatus(this,false);
          else VpnServiceHelper.changeVpnRunningStatus(this,true);
        });

        if (!Settings.canDrawOverlays(this)) {
            startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())));
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.container_vp, new MainFragment()).commit();
        RadarSettings.getInstance().init(getApplicationContext());
        ProxyConfig.Instance.registerVpnStatusListener(vpnStatusListener);
    }

    private void startRadarWindowService() {
        windowService = new Intent(this, RadarDrawView.class);
        ContextCompat.startForegroundService(this, windowService);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ProxyConfig.Instance.unregisterVpnStatusListener(vpnStatusListener);
    }
}
