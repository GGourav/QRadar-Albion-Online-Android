package com.minhui.vpn.Handlers;

import android.content.Context;
import com.minhui.vpn.Handlers.HandlerItem.Mob;

public class MainHandler {
    private static MainHandler instance;
    public MobsHandler mobsHandler;
    public HarvestablesHandler harvestablesHandler;
    // ... other handlers ...

    private MainHandler() {
        mobsHandler = new MobsHandler();
        harvestablesHandler = new HarvestablesHandler();
    }

    public static MainHandler getInstance() {
        if (instance == null) instance = new MainHandler();
        return instance;
    }

    // ADD THIS METHOD to load the data from your assets
    public void initDatabase(Context context) {
        if (mobsHandler != null) {
            mobsHandler.loadDatabase(context);
        }
    }
}
