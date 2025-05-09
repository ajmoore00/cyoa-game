package com.ajmoore00.cyoa;

/* I made this class early on, but its not used,
 * because I opted for a different approach,
 * but kept it in case for the future.
 */

import java.util.Random;

public class RandomEvent {
    private static Random random = new Random();
    private String eventType;
    private String description;

    public RandomEvent(String eventType, String description) {
        this.eventType = eventType;
        this.description = description;
    }

    public String getEventType() { return eventType; }
    public String getDescription() { return description; }

    public static RandomEvent generateEvent() {
        int chance = random.nextInt(4);
        switch(chance) {
            case 0:
                return new RandomEvent(
                    "Mystery Snack",
                    "You spot a sealed snack bar in a locker. Looks weird, but it's probably edible."
                );
            case 1:
                return new RandomEvent(
                    "Spoiled Drink",
                    "You find a bottle of something left out since before cryo. It smells... off."
                );
            case 2:
                return new RandomEvent(
                    "Med-Stim",
                    "You find a first aid kit wedged under a seat. There's a med-stim inside."
                );
            default:
                return new RandomEvent(
                    "Enemy Encounter",
                    "You hear scratching from a vent. Suddenly, a moon critter bursts out!"
                );
        }
    }
}
