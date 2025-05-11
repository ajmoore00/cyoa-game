package com.ajmoore00.cyoa;
import java.util.*;
import java.util.function.BiFunction;
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
        player.setAdventure(this);
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
        gameState.setCurrentScene("Intro");
        gameState.setLastChoice("");
        gotWrench = false;
        gotCutter = false;
        gotParts = false;
        gotDevice = false;
        shuttleFixed = false;
        medUsed = false;
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

            String prevMessage = lastMessage;

            // Sometimes a random event happens, just to keep things interesting
            String randomEventMsg = "";
            if (gameState.getEnding() == null && Math.random() < 0.3) {
                triggerRandomEvent();
                randomEventMsg = lastMessage;
                lastMessage = prevMessage; // Restore handler message for printing order
            }

            // Print all messages (random event first, then handler) after each turn
            if (!IS_WEB) {
                if (randomEventMsg != null && !randomEventMsg.isEmpty()) {
                    System.out.println(randomEventMsg);
                }
                if (prevMessage != null && !prevMessage.isEmpty()) {
                    System.out.println(prevMessage);
                }
                lastMessage = "";
            }
        }
        // Print out the ending
        if (!IS_WEB) {
            System.out.println("\n=== Ending: " + gameState.getEnding() + " ===");
            Scene endingScene = scenesMap.get("END_" + gameState.getEnding().name());
            if (endingScene != null) {
                System.out.println(endingScene.getDescription());
            } else {
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
            boolean hasDeviceItem = false;
            for (Item item : player.getInventory()) {
                if (item.getName().equals("Device")) {
                    hasDeviceItem = true;
                    break;
                }
            }
            for (String choice : scene.getChoices().keySet()) {
                // Hide device option if you don't have it
                if (
                    (choice.equals("Use the device to destroy the structure and everything inside")
                    || choice.equals("Activate the device and destroy everything"))
                    && !hasDeviceItem
                ) continue;
                // Hide shuttle option if you can't leave yet
                if (
                    choice.equals("Leave for the shuttle")
                    && !(shuttleFixed || (gotWrench && gotCutter && gotParts))
                ) continue;
                System.out.println(i++ + ". " + choice);
            }
            if (!IS_WEB && !gameState.getCurrentScene().equals("Intro") && !gameState.getCurrentScene().startsWith("END_")) {
                System.out.println(i + ". Open backpack");
            }
        }
    }

    // Gets what the player wants to do next (console only)
    private void getPlayerChoice() {
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        if (scene == null) return;

        // Special handling for Intro scene (console only)
        if (!IS_WEB && gameState.getCurrentScene().equals("Intro")) {
            System.out.print("Enter your name: ");
            String name = input.nextLine().trim();
            setPlayerName(name);
            return;
        }

        List<String> options = new ArrayList<>();
        boolean hasDeviceItem = false;
        for (Item i : player.getInventory()) {
            if (i.getName().equals("Device")) {
                hasDeviceItem = true;
                break;
            }
        }
        // Only add choices you can actually do
        for (String choice : scene.getChoices().keySet()) {
            if (
                (choice.equals("Use the device to destroy the structure and everything inside")
                || choice.equals("Activate the device and destroy everything"))
                && !hasDeviceItem
            ) continue;
            if (
                choice.equals("Leave for the shuttle")
                && !(shuttleFixed || (gotWrench && gotCutter && gotParts))
            ) continue;
            options.add(choice);
        }
        int choiceNum = -1;
        if (!IS_WEB && !gameState.getCurrentScene().equals("Intro") && !gameState.getCurrentScene().startsWith("END_")) {
            while (choiceNum < 1 || choiceNum > options.size() + 1) {
                System.out.print("What do you do? (Enter number): ");
                try {
                    choiceNum = Integer.parseInt(input.nextLine());
                } catch (NumberFormatException e) {
                    choiceNum = -1;
                }
            }
            if (choiceNum == options.size() + 1) {
                player.showBackpack(input);
                // If using an item triggered an ending, stop immediately
                if (gameState.getEnding() != null) return;
                showScene();
                getPlayerChoice();
                return;
            }
        } else {
            while (choiceNum < 1 || choiceNum > options.size()) {
                System.out.print("What do you do? (Enter number): ");
                try {
                    choiceNum = Integer.parseInt(input.nextLine());
                } catch (NumberFormatException e) {
                    choiceNum = -1;
                }
            }
        }
        String choice = options.get(choiceNum - 1);
        gameState.setLastChoice(choice);

        BiFunction<Adventure, Player, String> handler = scene.getHandler(choice);
        if (handler != null) {
            lastMessage = handler.apply(this, player);
            // If combat was started, handle it in console mode
            if (!IS_WEB && inCombat && currentEnemy != null) {
                boolean survived = Combat.handleCombat(player, currentEnemy);
                inCombat = false;
                currentEnemy = null;
                if (!survived) {
                    gameState.setEnding(GameState.Ending.DEATH);
                }
                // After combat, move to post-combat scene if needed
                if (gameState.getCurrentScene().equals("LargeCreatureEncounter") && survived) {
                    gameState.setCurrentScene("PostCombatLarge");
                }
                return;
            }
            // Handler may change the scene, so update nextScene
            String newScene = gameState.getCurrentScene();
            if (!newScene.equals(scene.getChoices().get(choice))) {
                // Handler already changed the scene, so return
                return;
            }
        }

        // Handle any special logic for this choice
        if (handleChoiceLogic(gameState.getCurrentScene(), choice, true)) {
            return;
        }

        String nextScene = scene.getChoices().get(choice);

        // Handle endings
        if (nextScene != null && nextScene.startsWith("END_")) {
            switch (nextScene) {
                case "END_ESCAPE":
                    gameState.setEnding(GameState.Ending.ESCAPE);
                    gameState.setCurrentScene("END_ESCAPE");
                    break;
                case "END_DEATH":
                    gameState.setEnding(GameState.Ending.DEATH);
                    gameState.setCurrentScene("END_DEATH");
                    break;
                case "END_SACRIFICE":
                    gameState.setEnding(GameState.Ending.SACRIFICE);
                    gameState.setCurrentScene("END_SACRIFICE");
                    break;
                case "END_STAY":
                    gameState.setEnding(GameState.Ending.STAY);
                    gameState.setCurrentScene("END_STAY");
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
        if (scene != null) {
            BiFunction<Adventure, Player, String> handler = scene.getHandler(choiceText);
            if (handler != null) {
                lastMessage = handler.apply(this, player);
                // Handler may have changed the scene or set a message.
                // Return immediately so the frontend can show the message and new scene.
                return;
            }
        }
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
                    gameState.setCurrentScene("END_ESCAPE");
                    break;
                case "END_DEATH":
                    gameState.setEnding(GameState.Ending.DEATH);
                    gameState.setCurrentScene("END_DEATH");
                    break;
                case "END_SACRIFICE":
                    gameState.setEnding(GameState.Ending.SACRIFICE);
                    gameState.setCurrentScene("END_SACRIFICE");
                    break;
                case "END_STAY":
                    gameState.setEnding(GameState.Ending.STAY);
                    gameState.setCurrentScene("END_STAY");
                    break;
            }
        } else if (nextScene != null) {
            gameState.setCurrentScene(nextScene);
        }

        // Maybe trigger a random event and append message
        if (gameState.getEnding() == null) {
            if (Math.random() < 0.3) {
                String prevMessage = lastMessage;
                triggerRandomEvent();
                if (prevMessage != null && !prevMessage.isEmpty() && lastMessage != null && !lastMessage.isEmpty()) {
                    lastMessage = prevMessage + " " + lastMessage;
                } else if (prevMessage != null && !prevMessage.isEmpty()) {
                    lastMessage = prevMessage;
                }
                // else, just use lastMessage as set by triggerRandomEvent
            }
        }
    }

    /**
     * Handles the logic for a player choice, for both console and web.
     * Returns true if the choice was handled (special logic), false if normal scene transition.
     */
    private boolean handleChoiceLogic(String current, String choice, boolean isConsole) {
        // Always redirect to ShuttleBayFixed if shuttle is fixed
        if ((choice.equals("Go to the escape shuttle") || choice.equals("Go back to the shuttle") || choice.equals("Go back to the escape shuttle") || choice.equals("Leave for the shuttle"))
            && shuttleFixed) {
            gameState.setCurrentScene("ShuttleBayFixed");
            lastMessage = "The shuttle is already patched together. It looks ugly, but it might just fly.";
            return true;
        }
        // All other scene-specific logic is now handled by SceneFactory handlers.
        return false;
    }

    // Handles random events (like finding snacks or running into critters)
    private void triggerRandomEvent() {
        RandomEvent event = RandomEvent.generateEvent();
        String type = event.getEventType();
        String desc = event.getDescription();
        lastMessage = desc; // <-- Always set lastMessage

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
                    lastMessage += " You found a Mystery Snack!";
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
                    lastMessage += " You found a Spoiled Drink!";
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
                lastMessage += " You found a Med-Stim!";
                break;
            case "Enemy Encounter":
                Enemy enemy = new Enemy("Moon Critter", 18, 7);
                if (!IS_WEB) {
                    Combat.handleCombat(player, enemy);
                } else {
                    startCombat(enemy);
                    lastMessage += " A Moon Critter attacks!";
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
                beastDefeated = true;
                gotDevice = false; // Only set true when picked up!
                gameState.setCurrentScene("PostCombatLarge");
            }
            currentEnemy = null;
            return;
        }
    }

    // Returns the current scene and choices as JSON for the frontend
    public String getCurrentSceneAsJson() {
        Scene scene = scenesMap.get(gameState.getCurrentScene());
        Map<String, Object> data = new HashMap<>();
        data.put("scene", gameState.getCurrentScene());
        data.put("description", scene != null ? scene.getDescription() : "Scene not found!");
        // Filter choices for device and shuttle
        List<String> filteredChoices = new ArrayList<>();
        boolean hasDeviceItem = false;
        for (Item i : player.getInventory()) {
            if (i.getName().equals("Device")) {
                hasDeviceItem = true;
                break;
            }
        }
        if (scene != null) {
            for (String choice : scene.getChoices().keySet()) {
                if (
                    (choice.equals("Use the device to destroy the structure and everything inside")
                    || choice.equals("Activate the device and destroy everything"))
                    && !hasDeviceItem
                ) continue;
                if (
                    choice.equals("Leave for the shuttle")
                    && !(shuttleFixed || (gotWrench && gotCutter && gotParts))
                ) continue;
                filteredChoices.add(choice);
            }
        }
        data.put("choices", filteredChoices);
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
        lastMessage = ""; // <-- Only after putting it in the data map
        data.put("inCombat", inCombat);
        int equippedWeaponIndex = player.getEquippedWeaponIndex();
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
            System.out.println("Before removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.getInventory().remove(index);
            System.out.println("After removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.relinkEquippedWeapon();
        } else if (item.getName().equals("Device")) {
            if (gameState.getCurrentScene().equals("AlienChamber") || gameState.getCurrentScene().equals("AlienTalk")) {
                gameState.setEnding(GameState.Ending.SACRIFICE);
                lastMessage = "You activate the device. The structure collapses in a blinding flash. The secret is buried, but so are you.";
            } else {
                lastMessage = "You press the button on the device. There is a blinding flash. You are vaporized. Oops.";
                gameState.setEnding(GameState.Ending.DEATH);
            }
            System.out.println("Before removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.getInventory().remove(item);
            System.out.println("After removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.relinkEquippedWeapon();
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
            player.setEquippedWeapon((Weapon) item); // This will now always use inventory reference
            lastMessage = "You equipped the " + item.getName() + ".";
        } else {
            lastMessage = "That's not a weapon!";
        }
    }

    // Equip a weapon by its unique ID (web)
    public void equipWeaponById(String id) {
        lastMessage = "";
        Weapon weaponToEquip = null;
        for (Item i : player.getInventory()) {
            if (i instanceof Weapon && i.getId().equals(id)) {
                weaponToEquip = (Weapon) i;
                break;
            }
        }
        if (weaponToEquip == null) {
            lastMessage = "Invalid item selection.";
            return;
        }
        player.setEquippedWeapon(weaponToEquip); // Always use inventory reference
        System.out.println("Equipping weapon: " + weaponToEquip.getName() + " (" + weaponToEquip.getId() + ")");
        lastMessage = "You equipped the " + weaponToEquip.getName() + ".";
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
            System.out.println("Before removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.getInventory().remove(item);
            System.out.println("After removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.relinkEquippedWeapon();
        } else if (item.getName().equals("Device")) {
            if (gameState.getCurrentScene().equals("AlienChamber") || gameState.getCurrentScene().equals("AlienTalk")) {
                gameState.setEnding(GameState.Ending.SACRIFICE);
                lastMessage = "You activate the device. The structure collapses in a blinding flash. The secret is buried, but so are you.";
            } else {
                lastMessage = "You press the button on the device. There is a blinding flash. You are vaporized. Oops.";
                gameState.setEnding(GameState.Ending.DEATH);
            }
            System.out.println("Before removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.getInventory().remove(item);
            System.out.println("After removal:");
            for (Item i : player.getInventory()) {
                System.out.println(i.getName() + " (" + i.getId() + ")");
            }
            player.relinkEquippedWeapon();
        } else {
            lastMessage = "You can't use that item right now.";
        }
    }

    // Getter and setter for medUsed (for SceneFactory handlers)
    public boolean isMedUsed() {
        return medUsed;
    }
    public void setMedUsed(boolean used) {
        this.medUsed = used;
    }

    // Getters and setters for modular scene access
    public boolean isInCombat() { return inCombat; }
    public void setInCombat(boolean inCombat) { this.inCombat = inCombat; }

    public boolean hasWrench() { return gotWrench; }
    public void setGotWrench(boolean gotWrench) { this.gotWrench = gotWrench; }

    public boolean hasCutter() { return gotCutter; }
    public void setGotCutter(boolean gotCutter) { this.gotCutter = gotCutter; }

    public boolean hasParts() { return gotParts; }
    public void setGotParts(boolean gotParts) { this.gotParts = gotParts; }

    public boolean hasDevice() { return gotDevice; }
    public void setGotDevice(boolean gotDevice) { this.gotDevice = gotDevice; }

    public boolean isShuttleFixed() { return shuttleFixed; }
    public void setShuttleFixed(boolean shuttleFixed) { this.shuttleFixed = shuttleFixed; }

    public boolean isBeastDefeated() {
        return beastDefeated;
    }

    public GameState getGameState() {
        return gameState;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    // Run the console game if this is the main class being executed
    public static void main(String[] args) {
        Adventure.IS_WEB = false;
        Adventure game = new Adventure();
        game.start();
    }
}
