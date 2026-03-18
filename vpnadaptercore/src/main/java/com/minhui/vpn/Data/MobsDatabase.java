package com.minhui.vpn.Data;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.minhui.vpn.PhotonPackageParser.enumerations.MobCodes;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class MobsDatabase {
    private static final String TAG = "MobsDatabase";
    public static final int OFFSET = 15;
    
    private static MobsDatabase instance;
    private Map<Integer, MobInfo> mobsById = new HashMap<>();
    private boolean isLoaded = false;
    
    public static class MobInfo {
        public int tier;
        public MobCodes type;
        public String localization;
        public boolean isHarvestable;
    }
    
    private MobsDatabase() {}
    
    public static synchronized MobsDatabase getInstance() {
        if (instance == null) {
            instance = new MobsDatabase();
        }
        return instance;
    }
    
    public void load(Context context) {
        if (isLoaded) return;
        
        try {
            InputStream is = context.getAssets().open("mobs.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonArray mobsArray = gson.fromJson(json, JsonArray.class);
            
            for (int i = 0; i < mobsArray.size(); i++) {
                JsonObject mob = mobsArray.get(i).getAsJsonObject();
                int typeId = i + OFFSET;
                
                MobInfo info = new MobInfo();
                info.tier = mob.has("t") ? mob.get("t").getAsInt() : 0;
                
                String category = mob.has("c") ? mob.get("c").getAsString() : "";
                info.type = parseCategory(category);
                
                if (mob.has("l")) {
                    String lootType = mob.get("l").getAsString();
                    info.localization = normalizeResourceType(lootType);
                    info.isHarvestable = info.localization != null;
                    if (info.isHarvestable) {
                        info.type = MobCodes.Harvestable;
                    }
                }
                
                if (info.localization == null) {
                    info.localization = category;
                }
                
                mobsById.put(typeId, info);
            }
            
            isLoaded = true;
            Log.i(TAG, "Loaded " + mobsById.size() + " mobs");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load mobs database", e);
        }
    }
    
    private MobCodes parseCategory(String category) {
        switch (category.toLowerCase()) {
            case "boss": return MobCodes.Boss;
            case "skinnable": return MobCodes.Skinnable;
            case "harvestable": return MobCodes.Harvestable;
            case "drone": return MobCodes.Drone;
            case "mist": return MobCodes.MistPortal;
            case "event": return MobCodes.Events;
            case "guard": return MobCodes.Guard;
            default: return MobCodes.Enemy;
        }
    }
    
    private String normalizeResourceType(String type) {
        if (type == null) return null;
        String upper = type.toUpperCase();
        
        if (upper.startsWith("SILVERCOINS") || upper.startsWith("DEADRAT")) return null;
        if (upper.startsWith("HIDE") || upper.startsWith("LEATHER")) return "hide";
        if (upper.startsWith("FIBER")) return "fiber";
        if (upper.startsWith("WOOD")) return "logs";
        if (upper.startsWith("ROCK") || upper.startsWith("STONE")) return "rock";
        if (upper.startsWith("ORE")) return "ore";
        return null;
    }
    
    public MobInfo getMobInfo(int typeId) {
        return mobsById.get(typeId);
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
}
