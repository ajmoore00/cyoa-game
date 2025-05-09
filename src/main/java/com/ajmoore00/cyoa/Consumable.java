package com.ajmoore00.cyoa;

// Class for consumable items (med-stim, snack, etc)
public class Consumable extends Item {
    private ConsumableType type;
    private int value;
    private int duration;

    // Enum for different types of consumables
    public enum ConsumableType {
        MED_STIM,
        MYSTERY_SNACK,
        SPOILED_DRINK
    }

    // Constructor for consumable
    public Consumable(String name, String description, ConsumableType type, int value, int duration) {
        super(name, description);
        this.type = type;
        this.value = value;
        this.duration = duration;
    }

    // Method for using the consumable
    public String use(Player player) {
        switch(type) {
            case MED_STIM:
                player.setHealth(player.getHealth() + value);
                return "You use a med-stim and feel a lot better.";
            case MYSTERY_SNACK:
                player.setMaxHealth(player.getMaxHealth() + 10);
                return "You eat the mystery snack. Your max health increases by 10!";
            case SPOILED_DRINK:
                player.addEffect("SICK", 1, 99); // 99 = until game ends or cured
                return "You drink the spoiled liquid. You feel sick...";
        }
        return "";
    }

    // Getters for consumable properties
    public int getDuration() { return duration; }
    public int getValue() { return value; }
    public ConsumableType getConsumableType() { return type; }
}
