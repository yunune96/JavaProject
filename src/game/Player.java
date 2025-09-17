package game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class Player {
    private Room currentRoom;
    private final List<Item> inventory = new ArrayList<>();
    private int health = 100;
    private int weaponBonusDamage = 0;
    private final java.util.Random attackRng = new java.util.Random();

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

    public int getAttackDamage() {
        int base = 8 + attackRng.nextInt(3);
        return Math.max(0, base + weaponBonusDamage);
    }


    public boolean hasItemByName(String itemName) {
        for (Item i : inventory) {
            if (i.getName().equalsIgnoreCase(itemName)) return true;
        }
        return false;
    }

    public boolean equipWeapon(String itemName) {
        if (!hasItemByName(itemName)) return false;
        int bonus = computeWeaponBonus(itemName);
        if (bonus == Integer.MIN_VALUE) return false; 
        weaponBonusDamage = bonus;
        return true;
    }

    public boolean isWeaponName(String itemName) {
        return computeWeaponBonus(itemName) != Integer.MIN_VALUE;
    }

    private int computeWeaponBonus(String itemName) {
        if ("단검".equalsIgnoreCase(itemName)) return 5;
        if ("장검".equalsIgnoreCase(itemName)) return 10;
        return Integer.MIN_VALUE;
    }

    public int getWeaponBonusDamage() {
        return weaponBonusDamage;
    }
}


