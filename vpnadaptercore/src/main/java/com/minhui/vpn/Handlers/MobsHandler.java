package com.minhui.vpn.Handlers;

import android.util.Log;

import com.minhui.vpn.Data.MobsDatabase;
import com.minhui.vpn.Data.MobsDatabase.MobInfo;
import com.minhui.vpn.Handlers.HandlerItem.Mob;
import com.minhui.vpn.PhotonPackageParser.enumerations.MobCodes;

import java.util.ArrayList;

public class MobsHandler {
    private static final String TAG = "MobsHandler";
    
    private ArrayList<Mob> mobsList;
    private MobsDatabase mobsDatabase;

    public MobsHandler() {
        mobsList = new ArrayList<>();
        mobsDatabase = MobsDatabase.getInstance();
    }

    public void clear() {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            mobsList.clear();
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    public void AddMob(int id, int typeId, String name, float posX, float posY, int health, int enchant, int rarity) {
        SharedLocks.mobsHandlerLock.writeLock().lock();

        try {
            Mob mob = new Mob(id, typeId, name, posX, posY, health, enchant, rarity);

            // Look up mob info from database
            MobInfo info = mobsDatabase.getMobInfo(typeId);
            
            if (info != null) {
                mob.tier = info.tier;
                mob.type = info.type;
                mob.name = info.localization;
                mob.info = true;
            } else {
                // Fallback for unknown mobs
                if (name != null && health == 1) {
                    mob.tier = 5;
                    mob.name = "mist";
                    mob.type = MobCodes.MistPortal;
                    mob.info = true;
                }
            }

            if (!mobsList.contains(mob)) {
                mobsList.add(mob);
            }
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    public void RemoveMob(int id) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            mobsList.removeIf(x -> x.getId() == id);
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    public ArrayList<Mob> getMobList() {
        SharedLocks.mobsHandlerLock.readLock().lock();
        try {
            return new ArrayList<>(mobsList);
        } finally {
            SharedLocks.mobsHandlerLock.readLock().unlock();
        }
    }

    public void UpdateMobPosition(int id, float posX, float posY) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            for (Mob mob : mobsList) {
                if (mob.getId() == id) {
                    mob.setPosX(posX);
                    mob.setPosY(posY);
                    break;
                }
            }
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }

    public void UpdateMobEnchantmentLevel(int mobId, int enchantmentLevel) {
        SharedLocks.mobsHandlerLock.writeLock().lock();
        try {
            for (Mob mob : mobsList) {
                if (mob.getId() == mobId) {
                    mob.setEnchantmentLevel(enchantmentLevel);
                    break;
                }
            }
        } finally {
            SharedLocks.mobsHandlerLock.writeLock().unlock();
        }
    }
}
