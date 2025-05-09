package com.ajmoore00.cyoa;

// Base class for all items (weapons, consumables, etc)
public abstract class Item {
    protected String name;
    protected String description;

    // Constructor for item
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Getters for item properties
    public String getName() { return name; }
    public String getDescription() { return description; }

    public String getType() {
        return this instanceof Weapon ? "Weapon" : this instanceof Consumable ? "Consumable" : "Other";
    }
}
