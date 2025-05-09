package com.ajmoore00.cyoa;
import java.util.*;
import com.google.gson.Gson;

public class Adventure {
    private Player player;
    private GameState gameState;
    private Map<String, Scene> scenesMap;
    private Scanner input;

    // World/ship names
    private static final String MOON = "Krylos";
    private static final String PLANET = "Virelia";
    private static final String SYSTEM = "Helion Drift";
    private static final String SHIP = "SS Pioneer";

    // Stuff you find, set to false until you find it
    private boolean gotWrench = false;
    private boolean gotCutter = false;
    private boolean gotParts = false;
    private boolean gotDevice = false;
    private boolean shuttleFixed = false;
    private boolean medUsed = false;

    // sets up everything for the game
    public Adventure() {
        player = new Player();
        gameState = new GameState();
        input = new Scanner(System.in);
        scenesMap = SceneFactory.createScenes(
            SHIP, MOON, PLANET, SYSTEM
        );
    }

    // this is the main game loop, keeps asking if you wanna play again
    public void start() {
        boolean again = true;
        while (again) {
            setupGame();
            playGame();
            System.out.print("\nPlay again? (y/n): ");
            String ans = input.nextLine().trim().toLowerCase();
            again = ans.equals("y") || ans.equals("yes");
        }
        System.out.println("\nThanks for playing!");
    }

    // resets everything for a new game
    private void setupGame() {
        player.setHealth(100);
        player.setMaxHealth(100);
        player.getInventory().clear();
        player.setEquippedWeapon(null);
        gameState.setEnding(null);
        gameState.setCurrentScene("CryoWake");
        gameState.setLastChoice("");
        gotWrench = false;
        gotCutter = false;
        gotParts = false;
        gotDevice = false;
        shuttleFixed = false;
        medUsed = false;
        System.out.println("=== Krylos ===");
        System.out.println(
            "Year 2342. You were part of a science crew on the " + SHIP +
            ", heading to " + MOON + ", a moon orbiting the gas giant " +
            PLANET + " in the " + SYSTEM + " system."
        );
        System.out.println(
            "Your team picked up weird stuff in the scans. The moon seemed to radiate some kind of energy, and command wanted answers."
        );
        System.out.print("\nWhat's your name, engineer? ");
        String playerName = input.nextLine();
        if (playerName == null || playerName.trim().isEmpty())
            playerName = "Player";
        player.setName(playerName);
        System.out.println(
            "\nYou remember going into cryo, but now you wake up to chaos..."
        );
    }

    // runs the actual game until you get an ending
    private void playGame() {
        while (gameState.getEnding() == null) {
            showScene();
            getPlayerChoice();
            // random event happens sometimes, just for fun
            if (Math.random() < 0.3 && gameState.getEnding() == null) {
                triggerRandomEvent();
            }
        }
        // print out the ending
        System.out.println("\n=== Ending: " + gameState.getEnding() + " ===");
        switch (gameState.getEnding()) {
            case ESCAPE:
                System.out.println(
                    "You scramble into the battered escape shuttle, patching wires and praying the engine holds. " +
                    "As you break atmosphere, you send a warning to the incoming ships: 'Turn back. Krylos isn't safe.' " +
                    "The mission is scrubbed. You live to engineer another day, but you'll never forget what you saw."
                );
                break;
            case SACRIFICE:
                System.out.println(
                    "You activate the device and a blinding white light fills the structure. " +
                    "You feel the ground shake as everything collapses. The secret of Krylos is buried, but so are you."
                );
                break;
            case DEATH:
                System.out.println(
                    "Your vision fades as the cold and silence of Krylos closes in. This is where your story ends."
                );
                break;
            case STAY:
                System.out.println(
                    "You stay with the aliens, learning their secrets and exploring the mysteries of " +
                    MOON + ". The universe feels bigger and stranger than you ever imagined."
                );
                break;
            default:
                System.out.println("The story ends... for now.");
        }
    }

    // prints out the current scene and choices
    private void showScene() {
        if (player.hasEffect("SICK")) {
            player.setHealth(player.getHealth() - 1);
            System.out.println("You feel sick. (-1 health)");
        }
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) {
            System.out.println("Scene not found!");
            gameState.setEnding(GameState.Ending.DEATH);
            return;
        }
        if (gameState.getCurrentScene().equals("CrashSite")) {
            System.out.println(
                "\nYou step outside. The landscape is almost blinding—flat, endless, and so stark white it barely seems real. " +
                "The air is perfectly still, not hot or cold, and the silence is absolute. You feel like you’re standing in a dream."
            );
            System.out.println(
                "Far off, you see something on the horizon. It almost looks like a structure, but the more you stare, " +
                "the more it seems to shimmer and blur, like a mirage."
            );
            System.out.println(
                "You spot some tracks leading away from the ship, and the " + SHIP + " is a twisted wreck behind you."
            );
        } else {
            System.out.println("\n" + scene.getDescription());
        }
        int i = 1;
        // loop through all the choices and print them
        for (String choice : scene.getChoices().keySet()) {
            if (
                choice.equals("Use the device to destroy the structure and everything inside")
                && !gotDevice
            ) continue;
            if (
                choice.equals("Leave for the shuttle")
                && !(shuttleFixed || (gotWrench && gotCutter && gotParts))
            ) continue;
            System.out.println(i++ + ". " + choice);
        }
        System.out.println(i + ". Open backpack");
    }

    // gets what the player wants to do next
    private void getPlayerChoice() {
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) return;
        List<String> options = new ArrayList<>();
        // add all the choices you can actually do
        for (String choice : scene.getChoices().keySet()) {
            if (
                choice.equals("Use the device to destroy the structure and everything inside")
                && !gotDevice
            ) continue;
            if (
                choice.equals("Activate the device and destroy everything")
                && !gotDevice
            ) continue;
            if (
                choice.equals("Leave for the shuttle")
                && !(shuttleFixed || (gotWrench && gotCutter && gotParts))
            ) continue;
            options.add(choice);
        }
        int choiceNum = -1;
        // ask until you get a valid number
        while (choiceNum < 1 || choiceNum > options.size() + 1) {
            System.out.print("What do you do? (Enter number): ");
            try {
                choiceNum = Integer.parseInt(input.nextLine());
            } catch (NumberFormatException e) {
                choiceNum = -1;
            }
        }
        // backpack option
        if (choiceNum == options.size() + 1) {
            player.showBackpack(input);
            getPlayerChoice();
            return;
        }
        String choice = options.get(choiceNum - 1);
        gameState.setLastChoice(choice);
        String nextScene = scene.getChoices().get(choice);

        // Storage room logic: give wrench if searching for tools
        if (
            gameState.getCurrentScene().equals("Storage")
            && choice.equals("Search for tools")
        ) {
            if (!gotWrench) {
                Weapon wrench = new Weapon(
                    "Wrench",
                    "A heavy wrench. Not ideal, but better than nothing.",
                    8
                );
                player.addItem(wrench);
                gotWrench = true;
                if (
                    player.getEquippedWeapon() == null
                    || wrench.getDamage() > player.getEquippedWeapon().getDamage()
                ) {
                    player.setEquippedWeapon(wrench);
                    System.out.println(
                        "You find a sturdy wrench and add it to your gear. You equip it."
                    );
                } else {
                    System.out.println(
                        "You find a sturdy wrench and add it to your gear."
                    );
                }
            } else {
                System.out.println("You already grabbed the wrench.");
            }
            return;
        }

        // Workshop logic: give plasma cutter if grabbing it
        if (
            gameState.getCurrentScene().equals("Workshop")
            && choice.equals("Grab plasma cutter")
        ) {
            if (!gotCutter) {
                Weapon plasmaCutter = new Weapon(
                    "Plasma Cutter",
                    "A plasma cutter. This could do some real damage.",
                    20
                );
                player.addItem(plasmaCutter);
                gotCutter = true;
                if (
                    player.getEquippedWeapon() == null
                    || plasmaCutter.getDamage() > player.getEquippedWeapon().getDamage()
                ) {
                    player.setEquippedWeapon(plasmaCutter);
                    System.out.println(
                        "You grab the plasma cutter and add it to your gear. You equip it."
                    );
                } else {
                    System.out.println(
                        "You grab the plasma cutter and add it to your gear."
                    );
                }
            } else {
                System.out.println("You already grabbed the plasma cutter.");
            }
            return;
        }

        // Medbay logic
        if (
            gameState.getCurrentScene().equals("Medbay")
            && choice.equals("Take a med-stim")
        ) {
            if (!medUsed) {
                player.addItem(
                    new Consumable(
                        "Med-Stim",
                        "Heals 50 health.",
                        Consumable.ConsumableType.MED_STIM,
                        50,
                        0
                    )
                );
                medUsed = true;
                System.out.println(
                    "You grab a med-stim from the dispenser and stash it in your backpack."
                );
            } else {
                System.out.println(
                    "The dispenser is empty. Someone already took the last med-stim."
                );
            }
            return;
        }
        if (
            gameState.getCurrentScene().equals("Medbay")
            && choice.equals("Continue down the hall")
        ) {
            gameState.setCurrentScene("SpareParts");
            return;
        }

        // Spare Parts logic
        if (
            gameState.getCurrentScene().equals("SpareParts")
            && choice.equals("Grab shuttle parts")
        ) {
            if (!gotParts) {
                gotParts = true;
                System.out.println(
                    "You grab the box labeled 'Shuttle Parts' and lug it with you."
                );
            } else {
                System.out.println("You already have the shuttle parts.");
            }
            return;
        }

        // Shuttle fix/leave logic
        if (
            gameState.getCurrentScene().equals("ShuttleBay")
            && choice.equals("Try to fix the shuttle")
        ) {
            if (gotWrench && gotCutter && gotParts) {
                System.out.println(
                    "You use the wrench, plasma cutter, and shuttle parts to patch up the shuttle. " +
                    "It's barely holding together, but it might just work."
                );
                shuttleFixed = true;
                System.out.print("Do you want to leave now? (y/n): ");
                String leave = input.nextLine().trim().toLowerCase();
                if (leave.equals("y") || leave.equals("yes")) {
                    gameState.setEnding(GameState.Ending.ESCAPE);
                    return;
                } else {
                    System.out.println(
                        "You look over your patched-together shuttle. It's ugly, but it might fly."
                    );
                    gameState.setCurrentScene("ShuttleBayFixed");
                    return;
                }
            } else {
                System.out.println(
                    "You don't have the necessary tools to fix the shuttle."
                );
                return;
            }
        }
        if (
            gameState.getCurrentScene().equals("ShuttleBayFixed")
            && choice.equals("Escape on the shuttle")
        ) {
            gameState.setEnding(GameState.Ending.ESCAPE);
            return;
        }

        // Beast fight logic: go straight to combat
        if (
            gameState.getCurrentScene().equals("LargeCreatureEncounter")
            && choice.equals("Fight it")
        ) {
            Enemy bigMonster = new Enemy(
                "Massive Pale Beast",
                70,
                16
            );
            boolean survived = Combat.handleCombat(player, bigMonster);
            if (!survived) {
                System.out.println(
                    "The creature's claws tear through your suit. You fall, the world going white."
                );
                gameState.setEnding(GameState.Ending.DEATH);
                return;
            } else {
                System.out.println(
                    "You barely survive, heart pounding. The creature drops something as it collapses."
                );
                gotDevice = true;
                System.out.println(
                    "You grab the device from the creature's hand. It's clearly from your ship—a powerful explosive, maybe meant for emergencies. Why did the creature have it?"
                );
                gameState.setCurrentScene("PostCombatLarge");
                return;
            }
        }

        // Handle endings
        if (nextScene != null && nextScene.startsWith("END_")) {
            switch (nextScene) {
                case "END_ESCAPE":
                    gameState.setEnding(GameState.Ending.ESCAPE);
                    break;
                case "END_DEATH":
                    gameState.setEnding(GameState.Ending.DEATH);
                    break;
                case "END_SACRIFICE":
                    gameState.setEnding(GameState.Ending.SACRIFICE);
                    break;
                case "END_STAY":
                    gameState.setEnding(GameState.Ending.STAY);
                    break;
            }
        } else if (nextScene != null) {
            gameState.setCurrentScene(nextScene);
        }
    }

    // does a random event, like finding a snack or fighting a critter
    private void triggerRandomEvent() {
        int roll = new Random().nextInt(4);
        switch (roll) {
            case 0:
                boolean gotSnack = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Mystery Snack"))
                        gotSnack = true;
                }
                if (!gotSnack) {
                    System.out.println(
                        "You spot a sealed snack bar in a locker. Looks weird, but it's probably edible."
                    );
                    player.addItem(
                        new Consumable(
                            "Mystery Snack",
                            "Increases your max health by 10.",
                            Consumable.ConsumableType.MYSTERY_SNACK,
                            0,
                            0
                        )
                    );
                }
                break;
            case 1:
                boolean gotDrink = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Spoiled Drink"))
                        gotDrink = true;
                }
                if (!gotDrink) {
                    System.out.println(
                        "You find a bottle of something left out since before cryo. It smells... off."
                    );
                    player.addItem(
                        new Consumable(
                            "Spoiled Drink",
                            "You can drink it, but it might not be a good idea.",
                            Consumable.ConsumableType.SPOILED_DRINK,
                            0,
                            0
                        )
                    );
                }
                break;
            case 2:
                System.out.println(
                    "You find a first aid kit wedged under a seat. There's a med-stim inside."
                );
                player.addItem(
                    new Consumable(
                        "Med-Stim",
                        "Heals 50 health.",
                        Consumable.ConsumableType.MED_STIM,
                        50,
                        0
                    )
                );
                break;
            case 3:
                System.out.println(
                    "You hear scratching from a vent. Suddenly, a moon critter bursts out!"
                );
                Enemy enemy = new Enemy(
                    "Moon Critter",
                    18,
                    7
                );
                boolean survived = Combat.handleCombat(player, enemy);
                if (!survived) {
                    System.out.println("You got wrecked by a moon critter. RIP.");
                    gameState.setEnding(GameState.Ending.DEATH);
                } else {
                    System.out.println("You survived the critter attack. Barely.");
                }
                break;
        }
    }

    // Returns the current scene and choices as JSON for the frontend
    public String getCurrentSceneAsJson() {
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        Map<String, Object> data = new HashMap<>();
        data.put("scene", gameState.getCurrentScene());
        data.put("description", scene != null ? scene.getDescription() : "Scene not found!");
        data.put("choices", scene != null ? scene.getChoices().keySet() : new ArrayList<>());
        data.put("ending", gameState.getEnding());
        data.put("playerHealth", player.getHealth());
        data.put("playerMaxHealth", player.getMaxHealth());
        data.put("inventory", player.getInventory());
        return new Gson().toJson(data);
    }

    // Processes a choice from the frontend
    public void makeChoice(String choiceText) {
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) return;
        // Find the next scene key for the given choice text
        String nextScene = scene.getChoices().get(choiceText);
        if (nextScene != null) {
            gameState.setCurrentScene(nextScene);
        }
        // You may want to add more logic here to handle inventory, combat, endings, etc.
    }

    // this is just the main thing to start the game
    public static void main(String[] args) {
        Adventure game = new Adventure();
        game.start();
    }
}
