package com.ajmoore00.cyoa;

// Consumable items: med-stim, snack, spoiled drink, etc.
public class Consumable extends Item {
    private ConsumableType type;
    private int value;
    private int duration;

    // Types of consumables
    public enum ConsumableType {
        MED_STIM,
        MYSTERY_SNACK,
        SPOILED_DRINK
    }

    // Make a new consumable
    public Consumable(String name, String description, ConsumableType type, int value, int duration) {
        super(name, description);
        this.type = type;
        this.value = value;
        this.duration = duration;
    }

    // What happens when you use this item
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
            default:
                return "You use the item, but nothing happens.";
        }
    }

    // Getters for consumable stuff
    public int getDuration() { return duration; }
    public int getValue() { return value; }
    public ConsumableType getConsumableType() { return type; }
}
