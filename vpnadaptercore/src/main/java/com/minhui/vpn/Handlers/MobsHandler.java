package com.minhui.vpn.Handlers;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.minhui.vpn.Handlers.HandlerItem.Mob;
import com.minhui.vpn.PhotonPackageParser.enumerations.MobCodes;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MobsHandler {
    private ArrayList<Mob> mobsList = new ArrayList<>();
    private Map<String, MobsInfo> mobDatabase = new HashMap<>();

    public class MobsInfo {
        public int tier;
        public String name;
        public String type;
    }

    public void loadDatabase(Context context) {
        try {
            Gson gson = new Gson();
            InputStreamReader reader = new InputStreamReader(context.getAssets().open("mobs.json"));
            Map<String, MobsInfo> loadedMobs = gson.fromJson(reader, new TypeToken<Map<String, MobsInfo>>(){}.getType());
            if (loadedMobs != null) mobDatabase.putAll(loadedMobs);
            reader.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void AddMob(int id, int typeId, String name, float posX, float posY, int health, int enchant, int rarity) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            Mob m = new Mob(id, typeId, name, posX, posY, health, enchant, rarity);
            String sid = String.valueOf(typeId);
            if (mobDatabase.containsKey(sid)) {
                MobsInfo info = mobDatabase.get(sid);
                m.tier = info.tier;
                m.name = info.name;
                m.type = MobCodes.Enemy;
                if (info.type != null && info.type.contains("Resource")) m.type = MobCodes.Harvestable;
            }
            if (!mobsList.contains(m)) mobsList.add(m);
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    // RESTORED: Required by vpnadaptercore
    public void RemoveMob(int id) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            mobsList.removeIf(m -> m.id == id);
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    // RESTORED: Required by vpnadaptercore
    public void UpdateMobEnchantmentLevel(int mobId, int enchantmentLevel) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            for (Mob m : mobsList) {
                if (m.id == mobId) {
                    m.enchantmentLevel = enchantmentLevel;
                    break;
                }
            }
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    public void UpdateMobPosition(int id, float posX, float posY) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            for (Mob m : mobsList) {
                if (m.id == id) { m.posX = posX; m.posY = posY; break; }
            }
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    public ArrayList<Mob> getMobList() {
        SharedLocks.mobsHandlerLock.readLock().lock();
        try { return new ArrayList<>(mobsList); } finally { SharedLocks.mobsHandlerLock.readLock().unlock(); }
    }

    public void clear() { mobsList.clear(); }
}
