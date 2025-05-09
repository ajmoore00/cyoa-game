package com.ajmoore00.cyoa;

// Class for enemies (like the beast or critters)
public class Enemy {
    private String name;
    private int health;
    private int damage;
    private boolean defeated;

    // Constructor for enemy
    public Enemy(String name, int health, int damage) {
        this.name = name;
        this.health = health;
        this.damage = damage;
        this.defeated = false;
    }

    // Method for enemy attacking the player
    public void attack(Player player) {
        player.setHealth(player.getHealth() - damage);
    }

    // Method for enemy taking damage
    public void takeDamage(int damage) {
        health -= damage;
        if (health <= 0) {
            defeated = true;
        }
    }

    // Getters for enemy properties
    public String getName() { return name; }
    public int getHealth() { return health; }
    public int getDamage() { return damage; }
    public boolean isDefeated() { return defeated; }
}
