package com.ajmoore00.cyoa;
import java.util.*;
import com.google.gson.Gson;

public class Adventure {
    public static boolean IS_WEB = false; // Add this at the top

    private Player player;
    private GameState gameState;
    private Map<String, Scene> scenesMap;
    private transient Scanner input;

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
    private boolean beastDefeated = false; // Add this with other booleans

    private String lastMessage = "";
    private Enemy currentEnemy = null;
    private boolean inCombat = false;

    // sets up everything for the game
    public Adventure() {
        player = new Player();
        gameState = new GameState();
        scenesMap = SceneFactory.createScenes(SHIP, MOON, PLANET, SYSTEM);
        // Only create Scanner if running in console
        if (!IS_WEB) {
            input = new Scanner(System.in);
        }
    }

    // this is the main game loop, keeps asking if you wanna play again
    public void start() {
        boolean again = true;
        while (again) {
            setupGame();
            playGame();
            if (!IS_WEB) {
                System.out.print("\nPlay again? (y/n): ");
                String ans = input.nextLine().trim().toLowerCase();
                again = ans.equals("y") || ans.equals("yes");
            } else {
                // web logic
            }
        }
        if (!IS_WEB) {
            System.out.println("\nThanks for playing!");
        } else {
            // web logic
        }
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
        if (!IS_WEB) {
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
        } else {
            // web logic
        }
    }

    // Set player name from frontend
    public void setPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            player.setName("Player");
        } else {
            player.setName(name.trim());
        }
        lastMessage = "Welcome, " + player.getName() + "!";
        gameState.setCurrentScene("CryoWake");
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
        if (!IS_WEB) {
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
        } else {
            // web logic
        }
    }

    // prints out the current scene and choices
    private void showScene() {
        player.updateEffects(); // <-- Add this line at the start
        if (player.hasEffect("SICK")) {
            player.setHealth(player.getHealth() - 1);
            if (IS_WEB) {
                lastMessage = (lastMessage.isEmpty() ? "" : lastMessage + " ") + "You feel sick. (-1 health)";
            }
            if (!IS_WEB) {
                System.out.println("You feel sick. (-1 health)");
            } else {
                // web logic
            }
        }
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) {
            if (!IS_WEB) {
                System.out.println("Scene not found!");
            } else {
                // web logic
            }
            gameState.setEnding(GameState.Ending.DEATH);
            return;
        }
        if (gameState.getCurrentScene().equals("CrashSite")) {
            if (!IS_WEB) {
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
                // web logic
            }
        } else {
            if (!IS_WEB) {
                System.out.println("\n" + scene.getDescription());
            } else {
                // web logic
            }
        }
        if (!IS_WEB) {
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
        } else {
            // web logic
        }
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
        if (!IS_WEB) {
            while (choiceNum < 1 || choiceNum > options.size() + 1) {
                System.out.print("What do you do? (Enter number): ");
                try {
                    choiceNum = Integer.parseInt(input.nextLine());
                } catch (NumberFormatException e) {
                    choiceNum = -1;
                }
            }
        } else {
            // web logic
        }
        // backpack option
        if (!IS_WEB) {
            if (choiceNum == options.size() + 1) {
                player.showBackpack(input);
                getPlayerChoice();
                return;
            }
        } else {
            // web logic
        }
        String choice = options.get(choiceNum - 1);
        gameState.setLastChoice(choice);
        String nextScene = scene.getChoices().get(choice);

        if (handleChoiceLogic(gameState.getCurrentScene(), choice, true)) {
            return;
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

    // Processes a choice from the frontend (handles all game logic for web)
    public void makeChoice(String choiceText) {
        player.updateEffects();
        if (player.hasEffect("SICK")) {
            player.setHealth(player.getHealth() - 1);
            if (IS_WEB) {
                lastMessage = (lastMessage.isEmpty() ? "" : lastMessage + " ") + "You feel sick. (-1 health)";
            }
        }
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) return;
        String current = gameState.getCurrentScene();
        String nextScene = scene.getChoices().get(choiceText);

        if (handleChoiceLogic(current, choiceText, false)) {
            return;
        }

        // Handle endings
        if (nextScene != null && nextScene.startsWith("END_")) {
            switch (nextScene) {
                case "END_ESCAPE":
                    gameState.setEnding(GameState.Ending.ESCAPE);
                    lastMessage = "You escape and warn the others. The moon stays a mystery, at least for now.";
                    break;
                case "END_DEATH":
                    gameState.setEnding(GameState.Ending.DEATH);
                    lastMessage = "Your vision fades as the cold and silence of Krylos closes in. This is where your story ends.";
                    break;
                case "END_SACRIFICE":
                    gameState.setEnding(GameState.Ending.SACRIFICE);
                    lastMessage = "You activate the device and a blinding white light fills the structure. You feel the ground shake as everything collapses. The secret of Krylos is buried, but so are you.";
                    break;
                case "END_STAY":
                    gameState.setEnding(GameState.Ending.STAY);
                    lastMessage = "You stay with the aliens, learning their secrets and exploring the mysteries of Krylos. The universe feels bigger and stranger than you ever imagined.";
                    break;
            }
        } else if (nextScene != null) {
            gameState.setCurrentScene(nextScene);
            lastMessage = "";
        }

        if (gameState.getEnding() == null) {
            if (Math.random() < 0.3) { // or always, for testing
                triggerRandomEvent();
            }
        }
    }

    /**
     * Handles the logic for a player choice, for both console and web.
     * Returns true if the choice was handled (special logic), false if normal scene transition.
     */
    private boolean handleChoiceLogic(String current, String choice, boolean isConsole) {
        // Storage room logic
        if (current.equals("Storage") && choice.equals("Search for tools")) {
            if (!gotWrench) {
                Weapon wrench = new Weapon(
                    "Wrench",
                    "A heavy wrench. Not ideal, but better than nothing.",
                    8
                );
                player.addItem(wrench);
                gotWrench = true;
                if (!IS_WEB) System.out.println("You find a sturdy wrench and add it to your gear.");
                else lastMessage = "You find a sturdy wrench and add it to your gear.";
            } else {
                if (!IS_WEB) System.out.println("You already grabbed the wrench.");
                else lastMessage = "You already grabbed the wrench.";
            }
            return true;
        }

        // Workshop logic
        if (current.equals("Workshop") && choice.equals("Grab plasma cutter")) {
            if (!gotCutter) {
                Weapon plasmaCutter = new Weapon(
                    "Plasma Cutter",
                    "A plasma cutter. This could do some real damage.",
                    20
                );
                player.addItem(plasmaCutter);
                gotCutter = true;
                if (!IS_WEB) System.out.println("You grab the plasma cutter and add it to your gear.");
                else lastMessage = "You grab the plasma cutter and add it to your gear.";
            } else {
                if (!IS_WEB) System.out.println("You already grabbed the plasma cutter.");
                else lastMessage = "You already grabbed the plasma cutter.";
            }
            return true;
        }

        // Medbay logic
        if (current.equals("Medbay") && choice.equals("Take a med-stim")) {
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
                if (isConsole) System.out.println("You grab a med-stim from the dispenser and stash it in your backpack.");
                else lastMessage = "You grab a med-stim from the dispenser and stash it in your backpack.";
            } else {
                if (isConsole) System.out.println("The dispenser is empty. Someone already took the last med-stim.");
                else lastMessage = "The dispenser is empty. Someone already took the last med-stim.";
            }
            return true;
        }

        // Spare Parts logic
        if (current.equals("SpareParts") && choice.equals("Grab shuttle parts")) {
            if (!gotParts) {
                gotParts = true;
                boolean hasParts = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Shuttle Parts")) hasParts = true;
                }
                if (!hasParts) player.addItem(new Item("Shuttle Parts", "Essential parts to repair the shuttle.") {
                    // anonymous class to make it non-abstract
                });
                if (!IS_WEB) System.out.println("You grab the box labeled 'Shuttle Parts' and lug it with you.");
                else lastMessage = "You grab the box labeled 'Shuttle Parts' and lug it with you.";
            } else {
                if (!IS_WEB) System.out.println("You already have the shuttle parts.");
                else lastMessage = "You already have the shuttle parts.";
            }
            return true;
        }

        // Shuttle fix/leave logic
        if (current.equals("ShuttleBay") && choice.equals("Try to fix the shuttle")) {
            if (shuttleFixed) {
                // Already fixed, just move to ShuttleBayFixed
                if (!IS_WEB) {
                    System.out.println("The shuttle is already patched together, ready to go.");
                } else {
                    lastMessage = "The shuttle is already patched together, ready to go.";
                }
                gameState.setCurrentScene("ShuttleBayFixed");
            } else if (gotWrench && gotCutter && gotParts) {
                shuttleFixed = true;
                player.getInventory().removeIf(i -> i.getName().equals("Shuttle Parts"));
                if (!IS_WEB) {
                    System.out.println("You use the wrench, plasma cutter, and shuttle parts to patch up the shuttle. It's barely holding together, but it might just work.");
                    System.out.print("Do you want to leave now? (y/n): ");
                    String leave = input.nextLine().trim().toLowerCase();
                    if (leave.equals("y") || leave.equals("yes")) {
                        gameState.setEnding(GameState.Ending.ESCAPE);
                    } else {
                        System.out.println("You look over your patched-together shuttle. It's ugly, but it might fly.");
                        gameState.setCurrentScene("ShuttleBayFixed");
                    }
                } else {
                    lastMessage = "You use the wrench, plasma cutter, and shuttle parts to patch up the shuttle. It's barely holding together, but it might just work.";
                    gameState.setCurrentScene("ShuttleBayFixed");
                }
            } else {
                if (!IS_WEB) System.out.println("You don't have the necessary tools to fix the shuttle.");
                else lastMessage = "You don't have the necessary tools to fix the shuttle.";
            }
            return true;
        }

        if (current.equals("ShuttleBayFixed") && choice.equals("Escape on the shuttle")) {
            gameState.setEnding(GameState.Ending.ESCAPE);
            if (!isConsole) lastMessage = "You escape and warn the others. The moon stays a mystery, at least for now.";
            return true;
        }

        // Beast fight logic
        if (current.equals("LargeCreatureEncounter")) {
            if (beastDefeated) {
                // Already dead, go to post-combat scene
                gameState.setCurrentScene("PostCombatLarge");
                if (!IS_WEB) {
                    System.out.println("The beast's corpse lies still. The device is gone.");
                } else {
                    lastMessage = "The beast's corpse lies still. The device is gone.";
                }
                return true;
            }
            if (choice.equals("Fight it")) {
                if (isConsole) {
                    Enemy bigMonster = new Enemy("Massive Pale Beast", 70, 16);
                    boolean survived = Combat.handleCombat(player, bigMonster);
                    if (!survived) {
                        System.out.println("The creature's claws tear through your suit. You fall, the world going white.");
                        gameState.setEnding(GameState.Ending.DEATH);
                    } else {
                        System.out.println("You barely survive, heart pounding. The creature drops something as it collapses.");
                        gotDevice = true;
                        beastDefeated = true;
                        boolean hasDevice = false;
                        for (Item i : player.getInventory()) {
                            if (i.getName().equals("Device")) hasDevice = true;
                        }
                        if (!hasDevice) player.addItem(new Item("Device", "A mysterious device from your ship. It looks dangerous.") {
                            // anonymous class to make it non-abstract
                        });
                        System.out.println("You grab the device from the creature's hand. It's clearly from your ship—a powerful explosive, maybe meant for emergencies. Why did the creature have it?");
                        gameState.setCurrentScene("PostCombatLarge");
                    }
                } else {
                    // Start web combat!
                    if (!inCombat) {
                        startCombat(new Enemy("Massive Pale Beast", 70, 16));
                    }
                }
                return true;
            }
            if (choice.equals("Run back to the ship")) {
                gameState.setCurrentScene("CrashSite");
                return true;
            }
        }

        // Device logic
        if (current.equals("DeviceRoom") && choice.equals("Take the device")) {
            boolean hasDevice = false;
            for (Item i : player.getInventory()) {
                if (i.getName().equals("Device")) hasDevice = true;
            }
            if (!hasDevice) player.addItem(new Item("Device", "A mysterious device from your ship. It looks dangerous.") {
                // anonymous class to make it non-abstract
            });
            return true;
        }

        return false; // Not a special case, do normal scene transition
    }

    // does a random event, like finding a snack or fighting a critter
    private void triggerRandomEvent() {
        RandomEvent event = RandomEvent.generateEvent();
        String type = event.getEventType();
        String desc = event.getDescription();
        if (!IS_WEB) System.out.println(desc);
        lastMessage = desc;

        switch (type) {
            case "Mystery Snack":
                boolean gotSnack = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Mystery Snack"))
                        gotSnack = true;
                }
                if (!gotSnack) {
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
            case "Spoiled Drink":
                boolean gotDrink = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Spoiled Drink"))
                        gotDrink = true;
                }
                if (!gotDrink) {
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
            case "Med-Stim":
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
            case "Enemy Encounter":
                Enemy enemy = new Enemy("Moon Critter", 18, 7);
                if (!IS_WEB) {
                    Combat.handleCombat(player, enemy);
                } else {
                    // Start web combat!
                    if (!inCombat) {
                        startCombat(enemy);
                    }
                }
                break;
        }
    }

    // Start a combat encounter (for web)
    public void startCombat(Enemy enemy) {
        this.currentEnemy = enemy;
        this.inCombat = true;
        lastMessage = "Combat started! " + enemy.getName() + " has " + enemy.getHealth() + " HP.";
    }

    // Handle a combat turn (for web)
    public void combatAction(String action, String idOrIndex) {
        player.updateEffects();
        if (player.hasEffect("SICK")) {
            player.setHealth(player.getHealth() - 1);
            if (IS_WEB) {
                lastMessage = (lastMessage.isEmpty() ? "" : lastMessage + " ") + "You feel sick. (-1 health)";
            }
        }
        if (!inCombat || currentEnemy == null) {
            lastMessage = "No enemy to fight!";
            return;
        }
        switch (action) {
            case "attack":
                int playerDamage = player.attack();
                currentEnemy.takeDamage(playerDamage);
                lastMessage = "You attack for " + playerDamage + " damage.";
                break;
            case "useItem":
                useItemById(idOrIndex);
                break;
            case "run":
                lastMessage = "You try to run! " + currentEnemy.getName() + " gets a free attack as you escape!";
                currentEnemy.attack(player);
                inCombat = false;
                currentEnemy = null;
                return;
        }
        // Enemy turn if enemy is still alive and player didn't run
        if (inCombat && currentEnemy != null && !currentEnemy.isDefeated()) {
            lastMessage += " " + currentEnemy.getName() + " attacks you for " + currentEnemy.getDamage() + " damage.";
            currentEnemy.attack(player);
        }
        // Check for end of combat
        if (player.getHealth() <= 0) {
            gameState.setEnding(GameState.Ending.DEATH);
            lastMessage += " You got knocked out by the " + currentEnemy.getName() + "...";
            inCombat = false;
            currentEnemy = null;
        } else if (currentEnemy.isDefeated()) {
            lastMessage += " You defeated the " + currentEnemy.getName() + "!";
            inCombat = false;
            // Special logic for large creature
            if ("Massive Pale Beast".equals(currentEnemy.getName())) {
                gotDevice = true;
                beastDefeated = true;
                boolean hasDevice = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Device")) hasDevice = true;
                }
                if (!hasDevice) player.addItem(new Item("Device", "A mysterious device from your ship. It looks dangerous.") {
                    // anonymous class to make it non-abstract
                });
                gameState.setCurrentScene("PostCombatLarge");
            }
            currentEnemy = null;
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
        // Build inventory with type info
        List<Map<String, Object>> inv = new ArrayList<>();
        for (Item item : player.getInventory()) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("name", item.getName());
            itemMap.put("description", item.getDescription());
            itemMap.put("type", item.getType());
            itemMap.put("id", item.getId()); // Add this line
            inv.add(itemMap);
        }
        data.put("inventory", inv);
        data.put("playerName", player.getName());
        data.put("lastMessage", lastMessage);
        data.put("inCombat", inCombat);
        int equippedWeaponIndex = -1;
        if (player.getEquippedWeapon() != null) {
            equippedWeaponIndex = player.getInventory().indexOf(player.getEquippedWeapon());
        }
        data.put("equippedWeaponIndex", equippedWeaponIndex);
        if (inCombat && currentEnemy != null) {
            Map<String, Object> enemyData = new HashMap<>();
            enemyData.put("name", currentEnemy.getName());
            enemyData.put("health", currentEnemy.getHealth());
            enemyData.put("damage", currentEnemy.getDamage());
            data.put("enemy", enemyData);
        }
        return new Gson().toJson(data);
    }

    public void useItemFromInventory(int index) {
        lastMessage = "";
        if (index < 0 || index >= player.getInventory().size()) {
            lastMessage = "Invalid item selection.";
            return;
        }
        Item item = player.getInventory().get(index);
        if (item instanceof Consumable) {
            lastMessage = player.useItem(item);
            player.getInventory().remove(index);
        } else if (item.getName().equals("Device")) {
            lastMessage = "You press the button on the device. There is a blinding flash. You are vaporized. Oops.";
            gameState.setEnding(GameState.Ending.DEATH);
            player.getInventory().remove(index);
        } else {
            lastMessage = "You can't use that item right now.";
        }
    }

    public void equipWeaponFromInventory(int index) {
        lastMessage = "";
        if (index < 0 || index >= player.getInventory().size()) {
            lastMessage = "Invalid item selection.";
            return;
        }
        Item item = player.getInventory().get(index);
        if (item instanceof Weapon) {
            player.setEquippedWeapon((Weapon) item); // Always set, don't check current
            lastMessage = "You equipped the " + item.getName() + ".";
        } else {
            lastMessage = "That's not a weapon!";
        }
    }

    public void equipWeaponById(String id) {
        lastMessage = "";
        Item item = null;
        for (Item i : player.getInventory()) {
            if (i.getId().equals(id)) {
                item = i;
                break;
            }
        }
        if (item == null) {
            lastMessage = "Invalid item selection.";
            return;
        }
        if (item instanceof Weapon) {
            player.setEquippedWeapon((Weapon) item);
            lastMessage = "You equipped the " + item.getName() + ".";
        } else {
            lastMessage = "That's not a weapon!";
        }
    }

    public void useItemById(String id) {
        lastMessage = "";
        Item item = null;
        for (Item i : player.getInventory()) {
            if (i.getId().equals(id)) {
                item = i;
                break;
            }
        }
        if (item == null) {
            lastMessage = "Invalid item selection.";
            return;
        }
        if (item instanceof Consumable) {
            lastMessage = player.useItem(item);
            player.getInventory().remove(item);
        } else if (item.getName().equals("Device")) {
            lastMessage = "You press the button on the device. There is a blinding flash. You are vaporized. Oops.";
            gameState.setEnding(GameState.Ending.DEATH);
            player.getInventory().remove(item);
        } else {
            lastMessage = "You can't use that item right now.";
        }
    }

    // Only run the console game if this is the main class being executed
    public static void main(String[] args) {
        Adventure.IS_WEB = false; // Add this line
        Adventure game = new Adventure();
        game.start();
    }
}
