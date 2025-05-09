package com.ajmoore00.cyoa;

import java.util.UUID;

// Base class for all items (weapons, consumables, etc)
public abstract class Item {
    protected String name;
    protected String description;
    private final String id; // Unique ID

    // Constructor for item
    public Item(String name, String description) {
        this.name = name;
        this.description = description;
        this.id = UUID.randomUUID().toString();
    }

    // Getters for item properties
    public String getName() { return name; }
    public String getDescription() { return description; }

    public String getType() {
        return this instanceof Weapon ? "Weapon" : this instanceof Consumable ? "Consumable" : "Other";
    }

    public String getId() { return id; }
}
