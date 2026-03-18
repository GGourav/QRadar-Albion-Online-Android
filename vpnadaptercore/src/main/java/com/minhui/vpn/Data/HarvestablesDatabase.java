package com.minhui.vpn.Data;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HarvestablesDatabase {
    private static final String TAG = "HarvestablesDatabase";
    
    private static HarvestablesDatabase instance;
    private Map<Integer, String> typeNumberToResource = new HashMap<>();
    private Set<String> validCombinations = new HashSet<>();
    private boolean isLoaded = false;
    
    public static final String WOOD = "logs";
    public static final String ROCK = "rock";
    public static final String FIBER = "fiber";
    public static final String HIDE = "hide";
    public static final String ORE = "ore";
    
    private HarvestablesDatabase() {}
    
    public static synchronized HarvestablesDatabase getInstance() {
        if (instance == null) {
            instance = new HarvestablesDatabase();
        }
        return instance;
    }
    
    public void load(Context context) {
        if (isLoaded) return;
        
        initTypeNumberMapping();
        
        try {
            InputStream is = context.getAssets().open("harvestables.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            
            String json = new String(buffer, StandardCharsets.UTF_8);
            Gson gson = new Gson();
            JsonObject data = gson.fromJson(json, JsonObject.class);
            
            parseResourceData(data, "WOOD", WOOD);
            parseResourceData(data, "ROCK", ROCK);
            parseResourceData(data, "FIBER", FIBER);
            parseResourceData(data, "HIDE", HIDE);
            parseResourceData(data, "ORE", ORE);
            
            isLoaded = true;
            Log.i(TAG, "Loaded harvestables database");
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load harvestables database", e);
        }
    }
    
    private void initTypeNumberMapping() {
        for (int i = 0; i <= 5; i++) typeNumberToResource.put(i, WOOD);
        for (int i = 6; i <= 10; i++) typeNumberToResource.put(i, ROCK);
        for (int i = 11; i <= 15; i++) typeNumberToResource.put(i, FIBER);
        for (int i = 16; i <= 22; i++) typeNumberToResource.put(i, HIDE);
        for (int i = 23; i <= 27; i++) typeNumberToResource.put(i, ORE);
    }
    
    private void parseResourceData(JsonObject data, String jsonKey, String normalizedType) {
        if (!data.has(jsonKey)) return;
        
        JsonArray tiers = data.getAsJsonArray(jsonKey);
        for (int i = 0; i < tiers.size(); i++) {
            JsonObject tierObj = tiers.get(i).getAsJsonObject();
            int tier = tierObj.get("tier").getAsInt();
            for (int enchant = 0; enchant <= 4; enchant++) {
                validCombinations.add(normalizedType + "-" + tier + "-" + enchant);
            }
        }
    }
    
    public String getResourceType(int typeNumber) {
        return typeNumberToResource.get(typeNumber);
    }
    
    public boolean isValidResource(String resourceType, int tier, int enchant) {
        return validCombinations.contains(resourceType + "-" + tier + "-" + enchant);
    }
    
    public boolean isLoaded() {
        return isLoaded;
    }
}
