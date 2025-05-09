package com.ajmoore00.cyoa;

// Class for weapons (like wrench or plasma cutter)
public class Weapon extends Item {
    private int damage;

    // Constructor for weapon
    public Weapon(String name, String description, int damage) {
        super(name, description);
        this.damage = damage;
    }

    // Getter for weapon damage
    public int getDamage() { return damage; }
}
