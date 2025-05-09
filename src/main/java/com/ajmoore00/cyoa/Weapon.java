package com.ajmoore00.cyoa;

// Weapon class: wrench, plasma cutter, etc.
public class Weapon extends Item {
    private int damage;

    // Make a new weapon
    public Weapon(String name, String description, int damage) {
        super(name, description);
        this.damage = damage;
    }

    // How much this weapon hurts stuff
    public int getDamage() { return damage; }
}
