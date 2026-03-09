package com.minhui.networkcapture;

import static com.minhui.vpn.utils.VpnServiceHelper.START_VPN_SERVICE_REQUEST_CODE;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Keep;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.minhui.networkcapture.Fragments.MainFragment;
import com.minhui.networkcapture.RadarView.RadarSettings;
import com.minhui.vpn.ProxyConfig;
import com.minhui.vpn.utils.DebugLog;
import com.minhui.vpn.utils.VpnServiceHelper;
import com.minhui.vpn.Handlers.MainHandler;

@Keep
public class VPNCaptureActivity extends FragmentActivity
{
    private static final int REQUEST_STORAGE_PERMISSION = 104;
    private static final int REQUEST_OVERLAY_PERMISSION = 5469;
    public static Context mContext;

    ProxyConfig.VpnStatusListener vpnStatusListener = new ProxyConfig.VpnStatusListener()
    {
        @Override
        public void onVpnStart(Context context)
        {
            handler.post(()->vpnButton.setImageResource(R.mipmap.ic_stop));
            startRadarWindowService();
        }

        @Override
        public void onVpnEnd(Context context)
        {
            handler.post(()->vpnButton.setImageResource(R.mipmap.ic_start));

            if(windowService!=null)
            {
                stopService(windowService);
            }
        }
    };

    private ImageView vpnButton;
    String[] needPermissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    private Handler handler;

    @Override
    public void onBackPressed()
    {
        try {
            finishAffinity();
            if(windowService != null) stopService(windowService);
            System.exit(0);
        } catch (Exception ex) {}
    }

    ActivityResultLauncher<Intent> overlayPermissionResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (!Settings.canDrawOverlays(this)) {
                    // Handle permission denied
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vpn_capture);

        mContext = this;

        try {
            MainHandler.getInstance().initDatabase(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.toolbar));
        vpnButton = (ImageView) findViewById(R.id.vpn);

        vpnButton.setOnClickListener(v -> {
          if(VpnServiceHelper.vpnRunningStatus()) closeVpn();
          else startVPN();
        });

        initMainFragment();
        requestStoragePermission();

        vpnButton.setEnabled(true);
        ProxyConfig.Instance.registerVpnStatusListener(vpnStatusListener);

        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            overlayPermissionResultLauncher.launch(intent);
        }

        MainFragment newFragment = new MainFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_vp, newFragment)
                .addToBackStack(null)
                .commit();

        handler = new Handler();
        RadarSettings.getInstance().init(getApplicationContext());
    }

    Intent windowService;

    public void initMainFragment() {}

    @Override
    protected void onStop() { super.onStop(); }

    private void startRadarWindowService()
    {
        DebugLog.i("startRadar(%d)\n", 1);
        windowService = new Intent(this, RadarDrawView.class);
        startService(windowService);
    }

    // FIX: Changed 'fun' to 'void' (Java syntax)
    private void requestStoragePermission()
    {
        ActivityCompat.requestPermissions(this, needPermissions, REQUEST_STORAGE_PERMISSION);
    }

    private void closeVpn() {
        VpnServiceHelper.changeVpnRunningStatus(this,false);
    }

    private void startVPN() {
        VpnServiceHelper.changeVpnRunningStatus(this,true);
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy() ;
        ProxyConfig.Instance.unregisterVpnStatusListener(vpnStatusListener);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_OVERLAY_PERMISSION) {
            if (Settings.canDrawOverlays(this)) startRadarWindowService();
        }

        if (requestCode == START_VPN_SERVICE_REQUEST_CODE && resultCode == RESULT_OK) {
            VpnServiceHelper.startVpnService(getApplicationContext());
        }
    }
}
