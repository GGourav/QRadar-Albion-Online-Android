package com.minhui.networkcapture;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.util.List;

import com.minhui.vpn.Data.MobsDatabase;
import com.minhui.vpn.Data.HarvestablesDatabase;


/**
 * @author minhui.zhu
 *         Created by minhui.zhu on 2018/4/30.
 *         Copyright © 2017年 Oceanwing. All rights reserved.
 */

public class MyApplication extends Application
{
    public static final String BUGLY_ID="6c905fa4a7";
    private static Context context;
    
    @Override
    public void onCreate()
    {
        super.onCreate();
        
        // Initialize context
        context = getApplicationContext();
        
        // Load mob and harvestable databases
        MobsDatabase.getInstance().load(context);
        HarvestablesDatabase.getInstance().load(context);
    }
    
    public static Context getContext(){
        return context;
    }
}
