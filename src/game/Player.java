package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Player {
    private Room currentRoom;
    private final List<Item> inventory = new ArrayList<>();
    private int health = 100;

    public Player(Room startingRoom) {
        this.currentRoom = startingRoom;
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void moveTo(Room nextRoom) {
        this.currentRoom = nextRoom;
    }

    public List<Item> getInventory() {
        return Collections.unmodifiableList(inventory);
    }

    public void addItem(Item item) {
        inventory.add(item);
    }

    public boolean removeItemByName(String itemName) {
        return inventory.removeIf(i -> i.getName().equalsIgnoreCase(itemName));
    }

    public int getHealth() {
        return health;
    }

    public void damage(int amount) {
        health = Math.max(0, health - amount);
    }

    public void heal(int amount) {
        health = Math.min(100, health + amount);
    }
}


