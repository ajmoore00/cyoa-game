package com.ajmoore00.cyoa;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.HashMap;
import java.util.function.BiFunction;

// Class for a scene/room/location in the game
public class Scene {
    private String description;
    private LinkedHashMap<String, String> choices; // choice text -> next scene key
    private Map<String, BiFunction<Adventure, Player, String>> choiceHandlers = new HashMap<>();

    // Constructor for making a scene with a description
    public Scene(String description) {
        this.description = description;
        this.choices = new LinkedHashMap<>();
    }

    // Method to add a choice to this scene
    public void addChoice(String choiceText, String nextSceneKey) {
        choices.put(choiceText, nextSceneKey);
    }

    // Add a choice with an action handler
    public void addChoice(String choiceText, String nextSceneKey, BiFunction<Adventure, Player, String> handler) {
        choices.put(choiceText, nextSceneKey);
        if (handler != null) {
            choiceHandlers.put(choiceText, handler);
        }
    }

    public BiFunction<Adventure, Player, String> getHandler(String choiceText) {
        return choiceHandlers.get(choiceText);
    }

    // Getter for the scene description
    public String getDescription() { return description; }
    // Getter for all the choices in this scene
    public LinkedHashMap<String, String> getChoices() { return choices; }
}
