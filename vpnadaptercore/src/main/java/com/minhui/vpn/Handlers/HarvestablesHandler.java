package com.minhui.vpn.Handlers;

import com.minhui.vpn.Handlers.HandlerItem.Harvestable;
import com.minhui.vpn.PhotonPackageParser.Utils;
import java.util.ArrayList;

public class HarvestablesHandler {
    private ArrayList<Harvestable> harvestableList = new ArrayList<>();

    public void addHarvestable(int id, int type, int tier, float posX, float posY, int charges, int enchant) {
        SharedLocks.harvestablesHandlerLock.writeLock().lock();
        try {
            for (Harvestable existing : harvestableList) { if (existing.getId() == id) return; }
            harvestableList.add(new Harvestable(id, type, tier, posX, posY, charges, enchant));
        } finally {
            SharedLocks.harvestablesHandlerLock.writeLock().unlock();
        }
    }

    // RESTORED: Required by vpnadaptercore
    public void removeHarvestable(int id) {
        SharedLocks.harvestablesHandlerLock.writeLock().lock();
        try {
            harvestableList.removeIf(x -> x.getId() == id);
        } finally {
            SharedLocks.harvestablesHandlerLock.writeLock().unlock();
        }
    }

    // RESTORED: Required by vpnadaptercore
    public void removeNotInRange(float lpX, float lpY) {
        SharedLocks.harvestablesHandlerLock.writeLock().lock();
        try {
            harvestableList.removeIf(x -> Utils.calculateDistance(lpX, lpY, x.getPosX(), x.getPosY()) > Utils.MaxDistance);
        } finally {
            SharedLocks.harvestablesHandlerLock.writeLock().unlock();
        }
    }

    public void updateHarvestable(int id, int charges, int enchantment) {
        SharedLocks.harvestablesHandlerLock.writeLock().lock();
        try {
            for (Harvestable h : harvestableList) {
                if (h.getId() == id) {
                    h.setCharges(charges);
                    h.setEnchant(enchantment);
                    break;
                }
            }
        } finally {
            SharedLocks.harvestablesHandlerLock.writeLock().unlock();
        }
    }

    public ArrayList<Harvestable> getHarvestableList() {
        SharedLocks.harvestablesHandlerLock.readLock().lock();
        try { return new ArrayList<>(harvestableList); } finally { SharedLocks.harvestablesHandlerLock.readLock().unlock(); }
    }

    public void clear() { harvestableList.clear(); }
}
