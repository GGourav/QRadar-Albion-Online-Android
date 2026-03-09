package com.minhui.vpn.Handlers;

import java.util.ArrayList;

public class ChestHandler {
    public static class Chest {
        public int id;
        public float posX;
        public float posY;
        public String name;
        public Chest(int id, float x, float y, String name) {
            this.id = id; this.posX = x; this.posY = y; this.name = name;
        }
    }

    private ArrayList<Chest> chestList = new ArrayList<>();

    public void addChest(int id, float x, float y, String name) {
        // Prevent duplicates
        for (Chest c : chestList) {
            if (c.id == id) return;
        }
        chestList.add(new Chest(id, x, y, name));
    }

    public void removeChest(int id) {
        chestList.removeIf(c -> c.id == id);
    }

    // FIX: Method name must be getChests() to satisfy DrawChests.java
    public ArrayList<Chest> getChests() {
        return new ArrayList<>(chestList);
    }

    public void clear() {
        chestList.clear();
    }
}
