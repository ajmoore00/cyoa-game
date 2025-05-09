package com.ajmoore00.cyoa;
import java.util.Scanner;

// Class for handling combat between player and enemy
public class Combat {
    // Method for running a fight between player and enemy
    public static boolean handleCombat(Player player, Enemy enemy) {
        if (Adventure.IS_WEB) {
            throw new IllegalStateException("Combat.handleCombat should not be called in web mode!");
        }
        Scanner scanner = new Scanner(System.in);
        System.out.println("Combat started! " + enemy.getName() + " has " + enemy.getHealth() + " HP.");
        // While loop for the fight
        while (!enemy.isDefeated() && player.getHealth() > 0) {
            System.out.println("\nYour health: " + player.getHealth() + "/" + player.getMaxHealth());
            System.out.println(enemy.getName() + " health: " + enemy.getHealth());
            System.out.println("Choose your action:");
            System.out.println("1. Attack");
            System.out.println("2. Use item from backpack");
            System.out.println("3. Run");
            int choice = -1;
            // While loop to get a valid action
            while (choice < 1 || choice > 3) {
                System.out.print("> ");
                try {
                    choice = Integer.parseInt(scanner.nextLine());
                } catch (NumberFormatException e) {
                    choice = -1;
                }
            }
            if (choice == 1) {
                int playerDamage = player.attack();
                if (player.getEquippedWeapon() == null) {
                    System.out.println("You swing with your fists for " + playerDamage + " damage.");
                } else {
                    System.out.println("You attack with your " + player.getEquippedWeapon().getName() + " for " + playerDamage + " damage.");
                }
                enemy.takeDamage(playerDamage);
            } else if (choice == 2) {
                player.showBackpack(scanner);
                continue; // Don't let enemy attack if you just used an item
            } else if (choice == 3) {
                System.out.println("You try to run!");
                System.out.println(enemy.getName() + " gets a free attack as you escape!");
                enemy.attack(player);
                System.out.println("You escape the fight.");
                return player.getHealth() > 0;
            }
            if (!enemy.isDefeated()) {
                System.out.println(enemy.getName() + " attacks you for " + enemy.getDamage() + " damage.");
                enemy.attack(player);
            }
        }
        if (player.getHealth() > 0) {
            System.out.println("You defeated the " + enemy.getName() + "!");
            return true;
        } else {
            System.out.println("You got knocked out by the " + enemy.getName() + "...");
            return false;
        }
    }
}
