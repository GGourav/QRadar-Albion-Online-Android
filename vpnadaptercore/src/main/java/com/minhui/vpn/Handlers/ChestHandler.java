package com.minhui.vpn.Handlers;

import com.minhui.vpn.Handlers.HandlerItem.Chest;
import java.util.ArrayList;

public class ChestHandler {
    private ArrayList<Chest> chestList = new ArrayList<>();

    public void addChest(int id, float x, float y, String name) {
        // Prevent adding the same chest twice
        for (Chest c : chestList) {
            if (c.getId() == id) return;
        }
        chestList.add(new Chest(id, x, y, name));
    }

    public void removeChest(int id) {
        // Remove chest by ID
        chestList.removeIf(c -> c.getId() == id);
    }

    // This method name MUST be getChests() to match DrawChests.java
    public ArrayList<Chest> getChests() {
        return new ArrayList<>(chestList);
    }

    public void clear() {
        chestList.clear();
    }
}
