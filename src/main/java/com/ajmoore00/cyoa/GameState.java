package com.ajmoore00.cyoa;

// Tracks the current scene, last choice, and ending
public class GameState {
    private String currentScene;
    private String lastChoice;
    private Ending ending;
    
    // All possible endings
    public enum Ending {
        ESCAPE,
        DEATH,
        SACRIFICE,
        STAY
    }
    
    // Start at Intro scene for web
    public GameState() {
        currentScene = "Intro";
        lastChoice = "";
        ending = null;
    }

    // Getters and setters for state
    public String getCurrentScene() { return currentScene; }
    public void setCurrentScene(String scene) { this.currentScene = scene; }
    public String getLastChoice() { return lastChoice; }
    public void setLastChoice(String choice) { this.lastChoice = choice; }
    public Ending getEnding() { return ending; }
    public void setEnding(Ending ending) { this.ending = ending; }
}
