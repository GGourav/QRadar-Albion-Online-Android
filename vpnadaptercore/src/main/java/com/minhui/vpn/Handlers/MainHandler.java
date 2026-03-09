package com.minhui.vpn.Handlers;

import android.content.Context;

public class MainHandler {
    private static MainHandler instance;
    
    // These must exist for the app to compile
    public MobsHandler mobsHandler;
    public HarvestablesHandler harvestablesHandler;
    public PlayersHandler playersHandler;
    public ChestHandler chestHandler;
    public FishingZoneHandler fishingZoneHandler;

    private MainHandler() {
        mobsHandler = new MobsHandler();
        harvestablesHandler = new HarvestablesHandler();
        playersHandler = new PlayersHandler();
        chestHandler = new ChestHandler();
        fishingZoneHandler = new FishingZoneHandler();
    }

    public static MainHandler getInstance() {
        if (instance == null) {
            instance = new MainHandler();
        }
        return instance;
    }

    // This is our new method to load the JSON data
    public void initDatabase(Context context) {
        if (mobsHandler != null) {
            mobsHandler.loadDatabase(context);
        }
    }

    // This is required by the PhotonPacketParser
    public void clearAll() {
        if (mobsHandler != null) mobsHandler.clear();
        if (harvestablesHandler != null) harvestablesHandler.clear();
        if (playersHandler != null) playersHandler.clear();
        if (chestHandler != null) chestHandler.clear();
        if (fishingZoneHandler != null) fishingZoneHandler.clear();
    }
}
