package com.ajmoore00.cyoa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// Class for the player (health, inventory, effects, etc)
public class Player {
    private String name;
    private int health;
    private int maxHealth;
    private Weapon equippedWeapon;
    private ArrayList<Item> inventory; // Inventory is an ArrayList
    private Map<String, Effect> activeEffects;

    // Constructor for player, sets up defaults
    public Player() {
        this.health = 100;
        this.maxHealth = 100;
        this.inventory = new ArrayList<>();
        this.activeEffects = new HashMap<>();
    }

    // Inner class for status effects (like sick)
    private class Effect {
        int value;
        int duration;

        Effect(int value, int duration) {
            this.value = value;
            this.duration = duration;
        }
    }

    // Method to add a status effect
    public void addEffect(String effectType, int value, int duration) {
        activeEffects.put(effectType, new Effect(value, duration));
        System.out.println("Applied " + effectType + " effect for " + duration + " turns");
    }

    // Method to update all effects (remove if done)
    public void updateEffects() {
        activeEffects.entrySet().removeIf(entry -> {
            Effect effect = entry.getValue();
            effect.duration--;
            if (effect.duration <= 0) {
                System.out.println(entry.getKey() + " effect has worn off");
                return true;
            }
            return false;
        });
    }

    // Check if player has a certain effect
    public boolean hasEffect(String effectType) {
        return activeEffects.containsKey(effectType);
    }

    // Get value of an effect (if any)
    public int getEffectValue(String effectType) {
        Effect effect = activeEffects.get(effectType);
        return effect != null ? effect.value : 0;
    }

    // Method for attacking (uses weapon if you have one)
    public int attack() {
        if (equippedWeapon != null) {
            return equippedWeapon.getDamage();
        }
        // Fists: 2-4 damage
        return 2 + (int)(Math.random() * 3);
    }

    // Use an item (like a med-stim)
    public void useItem(Item item) {
        if (item instanceof Consumable) {
            ((Consumable) item).use(this);
        }
    }

    // Show backpack and let player use an item
    public void showBackpack(Scanner scanner) {
        if (inventory.isEmpty()) {
            System.out.println("Your backpack is empty.");
            return;
        }
        System.out.println("Backpack:");
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            System.out.println((i + 1) + ". " + item.getName() + " - " + item.getDescription());
        }
        System.out.print("Choose an item to use (number), or 0 to go back: ");
        int choice = -1;
        while (choice < 0 || choice > inventory.size()) {
            try {
                choice = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                choice = -1;
            }
        }
        if (choice == 0) return;
        Item item = inventory.get(choice - 1);
        useItem(item);
        if (item instanceof Consumable) {
            inventory.remove(item);
        }
    }

    // Getters and setters for player stuff
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getHealth() { return health; }
    public void setHealth(int health) { this.health = Math.min(health, maxHealth); }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int maxHealth) { this.maxHealth = maxHealth; }
    public Weapon getEquippedWeapon() { return equippedWeapon; }
    public void setEquippedWeapon(Weapon weapon) { this.equippedWeapon = weapon; }
    public void addItem(Item item) { inventory.add(item); }
    public void removeItem(Item item) { inventory.remove(item); }
    public ArrayList<Item> getInventory() { return inventory; }
}
