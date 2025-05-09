package com.ajmoore00.cyoa;
import java.util.*;
import com.google.gson.Gson;

public class Adventure {
    public static boolean IS_WEB = false;

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
    private boolean beastDefeated = false;

    private String lastMessage = "";
    private Enemy currentEnemy = null;
    private boolean inCombat = false;

    // Sets up everything for the game
    public Adventure() {
        player = new Player();
        gameState = new GameState();
        scenesMap = SceneFactory.createScenes(SHIP, MOON, PLANET, SYSTEM);
        if (!IS_WEB) {
            input = new Scanner(System.in);
        }
    }

    // Main game loop, keeps asking if you wanna play again
    public void start() {
        boolean again = true;
        while (again) {
            setupGame();
            playGame();
            if (!IS_WEB) {
                System.out.print("\nPlay again? (y/n): ");
                String ans = input.nextLine().trim().toLowerCase();
                again = ans.equals("y") || ans.equals("yes");
            }
        }
        if (!IS_WEB) {
            System.out.println("\nThanks for playing!");
        }
    }

    // Resets everything for a new game
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

    // Runs the actual game until you get an ending
    private void playGame() {
        while (gameState.getEnding() == null) {
            showScene();
            getPlayerChoice();
            // Sometimes a random event happens, just to keep things interesting
            if (Math.random() < 0.3 && gameState.getEnding() == null) {
                triggerRandomEvent();
            }
        }
        // Print out the ending
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
        }
    }

    // Prints out the current scene and choices
    private void showScene() {
        player.updateEffects();
        if (player.hasEffect("SICK")) {
            player.setHealth(player.getHealth() - 1);
            if (IS_WEB) {
                lastMessage = (lastMessage.isEmpty() ? "" : lastMessage + " ") + "You feel sick. (-1 health)";
            }
            if (!IS_WEB) {
                System.out.println("You feel sick. (-1 health)");
            }
        }
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) {
            if (!IS_WEB) {
                System.out.println("Scene not found!");
            }
            gameState.setEnding(GameState.Ending.DEATH);
            return;
        }
        // Special flavor text for stepping outside
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
            }
        } else {
            if (!IS_WEB) {
                System.out.println("\n" + scene.getDescription());
            }
        }
        if (!IS_WEB) {
            int i = 1;
            for (String choice : scene.getChoices().keySet()) {
                // Hide device option if you don't have it
                if (
                    choice.equals("Use the device to destroy the structure and everything inside")
                    && !gotDevice
                ) continue;
                // Hide shuttle option if you can't leave yet
                if (
                    choice.equals("Leave for the shuttle")
                    && !(shuttleFixed || (gotWrench && gotCutter && gotParts))
                ) continue;
                System.out.println(i++ + ". " + choice);
            }
            System.out.println(i + ". Open backpack");
        }
    }

    // Gets what the player wants to do next (console only)
    private void getPlayerChoice() {
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) return;
        List<String> options = new ArrayList<>();
        // Only add choices you can actually do
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
        if (!IS_WEB) {
            while (choiceNum < 1 || choiceNum > options.size() + 1) {
                System.out.print("What do you do? (Enter number): ");
                try {
                    choiceNum = Integer.parseInt(input.nextLine());
                } catch (NumberFormatException e) {
                    choiceNum = -1;
                }
            }
        }
        // Backpack option
        if (!IS_WEB) {
            if (choiceNum == options.size() + 1) {
                player.showBackpack(input);
                getPlayerChoice();
                return;
            }
        }
        String choice = options.get(choiceNum - 1);
        gameState.setLastChoice(choice);
        String nextScene = scene.getChoices().get(choice);

        // Handle any special logic for this choice
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

    // Handles a choice from the frontend (web version)
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

        // Handle any special logic for this choice
        if (handleChoiceLogic(current, choiceText, false)) {
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

        // Maybe trigger a random event
        if (gameState.getEnding() == null) {
            if (Math.random() < 0.3) {
                triggerRandomEvent();
            }
        }
    }

    /**
     * Handles the logic for a player choice, for both console and web.
     * Returns true if the choice was handled (special logic), false if normal scene transition.
     */
    private boolean handleChoiceLogic(String current, String choice, boolean isConsole) {
        // Storage room: grab the wrench if you haven't already
        if (current.equals("Storage") && choice.equals("Search for tools")) {
            if (!gotWrench) {
                player.addItem(new Weapon("Wrench", "A heavy wrench. Not fancy, but it'll smash stuff.", 8));
                gotWrench = true;
                lastMessage = "You grab a wrench. Feels solid in your hand.";
            } else {
                lastMessage = "You already grabbed the wrench earlier.";
            }
            return true;
        }

        // Workshop: grab the plasma cutter if you haven't already
        if (current.equals("Workshop") && choice.equals("Grab plasma cutter")) {
            if (!gotCutter) {
                player.addItem(new Weapon("Plasma Cutter", "Cuts through metal... or whatever else.", 16));
                gotCutter = true;
                lastMessage = "You snag the plasma cutter. This thing could come in handy.";
            } else {
                lastMessage = "You already took the plasma cutter.";
            }
            return true;
        }

        // Medbay: grab a med-stim if you haven't already
        if (current.equals("Medbay") && choice.equals("Take a med-stim")) {
            if (!medUsed) {
                player.addItem(new Consumable(
                    "Med-Stim",
                    "Heals 50 health.",
                    Consumable.ConsumableType.MED_STIM,
                    50,
                    0
                ));
                medUsed = true;
                lastMessage = "You grab a med-stim from the dispenser.";
            } else {
                lastMessage = "You already took the med-stim.";
            }
            return true;
        }

        // Spare Parts: grab shuttle parts if you haven't already
        if (current.equals("SpareParts") && choice.equals("Grab shuttle parts")) {
            if (!gotParts) {
                player.addItem(new Item("Shuttle Parts", "Essential parts to repair the shuttle.") {});
                gotParts = true;
                lastMessage = "You grab the shuttle parts. Hope they're all here.";
            } else {
                lastMessage = "You already took the shuttle parts.";
            }
            return true;
        }

        // Shuttle: try to fix it if you have all the stuff
        if (current.equals("ShuttleBay") && choice.equals("Try to fix the shuttle")) {
            if (shuttleFixed) {
                lastMessage = "You already patched up the shuttle.";
            } else if (gotWrench && gotCutter && gotParts) {
                shuttleFixed = true;
                gameState.setCurrentScene("ShuttleBayFixed");
                lastMessage = "You patch the shuttle together. It's ugly, but it might just fly.";
            } else {
                lastMessage = "You don't have everything you need to fix the shuttle.";
            }
            return true;
        }

        // Escape on the shuttle
        if (current.equals("ShuttleBayFixed") && choice.equals("Escape on the shuttle")) {
            gameState.setEnding(GameState.Ending.ESCAPE);
            if (!isConsole)
                lastMessage = "You escape on the shuttle!";
            return true;
        }

        // Beast fight logic
        if (current.equals("LargeCreatureEncounter")) {
            if (beastDefeated) {
                lastMessage = "The beast is already down. Nothing left to do here.";
                return true;
            }
            if (choice.equals("Fight it")) {
                startCombat(new Enemy("Massive Pale Beast", 40, 14));
                return true;
            }
            if (choice.equals("Run back to the ship")) {
                lastMessage = "You bolt back to the ship, heart pounding.";
                gameState.setCurrentScene("CrashSite");
                return true;
            }
        }

        // Device logic (for future expansion)
        if (current.equals("DeviceRoom") && choice.equals("Take the device")) {
            boolean hasDevice = false;
            for (Item i : player.getInventory()) {
                if (i.getName().equals("Device")) hasDevice = true;
            }
            if (!hasDevice) {
                player.addItem(new Item("Device", "A weird device. Looks dangerous.") {});
                gotDevice = true;
                lastMessage = "You take the device. It hums quietly in your hand.";
            } else {
                lastMessage = "You already have the device.";
            }
            return true;
        }

        return false; // Not a special case, just move to the next scene
    }

    // Handles random events (like finding snacks or running into critters)
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
                    if (i.getName().equals("Mystery Snack")) gotSnack = true;
                }
                if (!gotSnack) {
                    player.addItem(new Consumable(
                        "Mystery Snack",
                        "Looks weird, but probably edible. Increases max health.",
                        Consumable.ConsumableType.MYSTERY_SNACK,
                        0,
                        0
                    ));
                }
                break;
            case "Spoiled Drink":
                boolean gotDrink = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Spoiled Drink")) gotDrink = true;
                }
                if (!gotDrink) {
                    player.addItem(new Consumable(
                        "Spoiled Drink",
                        "Smells off. Might make you sick.",
                        Consumable.ConsumableType.SPOILED_DRINK,
                        0,
                        0
                    ));
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
                    startCombat(enemy);
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
        // Enemy gets a turn if they're still alive and you didn't run
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
            // If you beat the big beast, you get the device
            if ("Massive Pale Beast".equals(currentEnemy.getName())) {
                gotDevice = true;
                beastDefeated = true;
                boolean hasDevice = false;
                for (Item i : player.getInventory()) {
                    if (i.getName().equals("Device")) hasDevice = true;
                }
                if (!hasDevice) {
                    player.addItem(new Item("Device", "A weird device. Looks dangerous.") {});
                    lastMessage += " The beast drops a strange device. You pick it up.";
                }
            }
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
            itemMap.put("id", item.getId());
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

    // Use an item from inventory by index (console only)
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

    // Equip a weapon from inventory by index (console only)
    public void equipWeaponFromInventory(int index) {
        lastMessage = "";
        if (index < 0 || index >= player.getInventory().size()) {
            lastMessage = "Invalid item selection.";
            return;
        }
        Item item = player.getInventory().get(index);
        if (item instanceof Weapon) {
            player.setEquippedWeapon((Weapon) item);
            lastMessage = "You equipped the " + item.getName() + ".";
        } else {
            lastMessage = "That's not a weapon!";
        }
    }

    // Equip a weapon by its unique ID (web)
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

    // Use an item by its unique ID (web)
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

    // Run the console game if this is the main class being executed
    public static void main(String[] args) {
        Adventure.IS_WEB = false;
        Adventure game = new Adventure();
        game.start();
    }
}
