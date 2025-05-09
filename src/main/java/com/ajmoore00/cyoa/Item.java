package com.ajmoore00.cyoa;

import java.util.UUID;

// Base class for all items (weapons, consumables, etc)
public abstract class Item {
    protected String name;
    protected String description;
    private final String id; // Unique ID for this item

    // Make a new item
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }

    // Getters for item properties
    public String getName() { return name; }
    public String getDescription() { return description; }

    // Figure out what kind of item this is
    public String getType() {
        return this instanceof Weapon ? "Weapon" : this instanceof Consumable ? "Consumable" : "Other";
    }

    // Get this item's unique ID
    public String getId() { return id; }
}
