package com.minhui.vpn.Handlers;

import com.minhui.vpn.Handlers.HandlerItem.Harvestable;
import com.minhui.vpn.PhotonPackageParser.Utils;
import java.util.ArrayList;

public class HarvestablesHandler {
    private ArrayList<Harvestable> harvestableList = new ArrayList<>();

    public void addHarvestable(int id, int type, int tier, float posX, float posY, int charges, int enchant) {
        SharedLocks.harvestablesHandlerLock.writeLock().lock();
        try {
            // Check if already exists
            for (Harvestable existing : harvestableList) {
                if (existing.getId() == id) return;
            }
            harvestableList.add(new Harvestable(id, type, tier, posX, posY, charges, enchant));
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
        return new ArrayList<>(harvestableList);
    }

    public void clear() { harvestableList.clear(); }
}
