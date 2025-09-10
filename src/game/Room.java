package game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class Room {
    private String description;
    private final Map<String, Room> exits = new HashMap<>();
    private final List<Item> items = new ArrayList<>();
    private boolean locked = false;
    private String requiredItemName = null;
    private String npcName = null;
    private String npcDialog = null;
    private Monster monster = null;

    public Room(String description) {
        this.description = description;
    }

    public void setExit(String direction, Room neighbor) {
        exits.put(direction, neighbor);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Room getExit(String direction) {
        return exits.get(direction);
    }

    public Map<String, Room> getExits() {
        return Collections.unmodifiableMap(exits);
    }

    public List<Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public Item takeItemByName(String name) {
        for (int i = 0; i < items.size(); i++) {
            Item it = items.get(i);
            if (it.getName().equalsIgnoreCase(name)) {
                items.remove(i);
                return it;
            }
        }
        return null;
    }

    public boolean isLocked() {
        return locked;
    }

    public void lockWithItem(String requiredItemName) {
        this.locked = true;
        this.requiredItemName = requiredItemName;
    }

    public boolean unlock(String usedItemName) {
        if (locked && requiredItemName != null && requiredItemName.equalsIgnoreCase(usedItemName)) {
            locked = false;
            return true;
        }
        return false;
    }

    public void setNpc(String name, String dialog) {
        this.npcName = name;
        this.npcDialog = dialog;
    }

    public boolean hasNpc() {
        return npcName != null;
    }

    public String talkToNpc() {
        if (npcName == null) return "여기엔 아무도 없습니다.";
        return npcName + ": " + (npcDialog == null ? "..." : npcDialog);
    }

    public void setMonster(Monster monster) {
        this.monster = monster;
    }

    public Monster getMonster() {
        return monster;
    }

    public void clearMonster() {
        this.monster = null;
    }
}


