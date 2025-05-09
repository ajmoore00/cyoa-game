package com.ajmoore00.cyoa;
import java.util.LinkedHashMap;

// Class for a scene/room/location in the game
public class Scene {
    private String description;
    private LinkedHashMap<String, String> choices; // choice text -> next scene key

    // Constructor for making a scene with a description
    public Scene(String description) {
        this.description = description;
        this.choices = new LinkedHashMap<>();
    }

    // Method to add a choice to this scene
    public void addChoice(String choiceText, String nextSceneKey) {
        choices.put(choiceText, nextSceneKey);
    }

    // Getter for the scene description
    public String getDescription() { return description; }
    // Getter for all the choices in this scene
    public LinkedHashMap<String, String> getChoices() { return choices; }
}
