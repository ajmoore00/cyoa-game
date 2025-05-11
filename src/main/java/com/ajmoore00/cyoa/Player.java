package com.ajmoore00.cyoa;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

// Player class: handles health, inventory, effects, and all that jazz
public class Player {
    private String name;
    private int health;
    private int maxHealth;
    private Weapon equippedWeapon;
    private ArrayList<Item> inventory;
    private Map<String, Effect> activeEffects;
    private Adventure adventure;

    // Player starts with default stats and empty inventory
    public Player() {
        this.health = 100;
        this.maxHealth = 100;
        this.inventory = new ArrayList<>();
        this.activeEffects = new HashMap<>();
    }

    // Inner class for status effects (like being sick)
    private class Effect {
        int value;
        int duration;

        Effect(int value, int duration) {
            this.value = value;
            this.duration = duration;
        }
    }

    // Add a status effect (like "SICK")
    public void addEffect(String effectType, int value, int duration) {
        activeEffects.put(effectType, new Effect(value, duration));
        if (!Adventure.IS_WEB) System.out.println("Applied " + effectType + " effect for " + duration + " turns");
    }

    // Update all effects, remove them if they're done
    public void updateEffects() {
        activeEffects.entrySet().removeIf(entry -> {
            Effect effect = entry.getValue();
            effect.duration--;
            if (effect.duration <= 0) {
                if (!Adventure.IS_WEB) System.out.println(entry.getKey() + " effect has worn off");
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

    // Attack with weapon if you have one, otherwise punch
    public int attack() {
        if (equippedWeapon != null) {
            return equippedWeapon.getDamage();
        }
        // Fists: 2-4 damage
        return 2 + (int)(Math.random() * 3);
    }

    // Use an item (like a med-stim)
    public String useItem(Item item) {
        if (item instanceof Consumable) {
            Consumable c = (Consumable) item;
            switch (c.getConsumableType()) {
                case MED_STIM:
                    int heal = Math.min(c.getValue(), getMaxHealth() - getHealth());
                    setHealth(getHealth() + heal);
                    return "You use a Med-Stim and recover " + heal + " health.";
                case MYSTERY_SNACK:
                    setMaxHealth(getMaxHealth() + 10);
                    return "You eat the Mystery Snack. Your max health increases by 10!";
                case SPOILED_DRINK:
                    addEffect("SICK", 1, 99); // 99 = until game ends or cured
                    return "You drink the Spoiled Drink. You feel sick...";
                default:
                    return "You use the " + item.getName() + ".";
            }
        }
        return "You can't use that item.";
    }

    // Show backpack and let player use or equip an item (console only)
    public void showBackpack(Scanner scanner) {
        if (inventory.isEmpty()) {
            if (!Adventure.IS_WEB) System.out.println("Your backpack is empty.");
            return;
        }
        if (!Adventure.IS_WEB) System.out.println("Backpack:");
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            String eq = (item == equippedWeapon) ? " (equipped)" : "";
            if (!Adventure.IS_WEB) System.out.println((i + 1) + ". " + item.getName() + eq + " - " + item.getDescription());
        }
        if (!Adventure.IS_WEB) System.out.print("Choose an item to use/equip (number), or 0 to go back: ");
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
        if (item instanceof Consumable) {
            String result = useItem(item);
            if (!Adventure.IS_WEB) System.out.println(result);
            inventory.remove(item);
            relinkEquippedWeapon(); // <-- Add this line
            showBackpack(scanner); // Stay in backpack
        } else if (item instanceof Weapon) {
            setEquippedWeapon((Weapon) item); // Equip whatever weapon you pick
            if (!Adventure.IS_WEB) System.out.println("You equipped the " + item.getName() + ".");
            showBackpack(scanner); // Stay in backpack
        } else if (item.getName().equals("Device") && adventure != null) {
            adventure.useItemFromInventory(choice - 1);
            if (!Adventure.IS_WEB) System.out.println(adventure.getLastMessage());
            showBackpack(scanner); // Stay in backpack
        } else {
            if (!Adventure.IS_WEB) System.out.println("You can't use that item right now.");
            showBackpack(scanner); // Stay in backpack
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
    public void setEquippedWeapon(Weapon weapon) {
        for (Item item : inventory) {
            if (item instanceof Weapon && item.getId().equals(weapon.getId())) {
                this.equippedWeapon = (Weapon) item;
                System.out.println("Equipped weapon set to: " + item.getName() + " (" + item.getId() + ")");
                return;
            }
        }
        this.equippedWeapon = null;
        System.out.println("Equipped weapon set to: null");
    }
    public void addItem(Item item) { inventory.add(item); }
    public void removeItem(Item item) { inventory.remove(item); }
    public ArrayList<Item> getInventory() { return inventory; }
    public void setAdventure(Adventure adventure) {
        this.adventure = adventure;
    }

    // Equip a weapon by index (console only)
    public void equipWeapon(int index) {
        Item item = inventory.get(index);
        if (item instanceof Weapon) {
            setEquippedWeapon((Weapon) item);
        }
    }

    // Get the index of the equipped weapon in your backpack
    public int getEquippedWeaponIndex() {
        if (equippedWeapon == null) return -1;
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            if (item instanceof Weapon && item.getId().equals(equippedWeapon.getId())) {
                return i;
            }
        }
        return -1;
    }

    // Relink equipped weapon with inventory
    public void relinkEquippedWeapon() {
        if (equippedWeapon == null) return;
        for (Item item : inventory) {
            if (item instanceof Weapon && item.getId().equals(equippedWeapon.getId())) {
                equippedWeapon = (Weapon) item;
                return;
            }
        }
        // If not found, unequip
        equippedWeapon = null;
    }

    // Display inventory and equipped weapon
    public void displayInventory() {
        System.out.println("Inventory:");
        for (int i = 0; i < inventory.size(); i++) {
            Item item = inventory.get(i);
            System.out.println(i + ": " + item.getName() + " (" + item.getId() + ")");
        }
        if (equippedWeapon != null) {
            System.out.println("Equipped: " + equippedWeapon.getName() + " (" + equippedWeapon.getId() + ")");
        }
    }
}
