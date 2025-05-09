package com.ajmoore00.cyoa;

import static spark.Spark.*;
import spark.Session;

import java.util.HashMap;
import java.util.Map;

public class WebServer {
    // Store games per session
    private static Map<String, Adventure> games = new HashMap<>();

    public static void main(String[] args) {
        Adventure.IS_WEB = true; // Add this line
        port(4567);
        staticFiles.location("/public"); // Serve files from src/main/resources/public

        // Endpoint: start a new game
        post("/start", (req, res) -> {
            Session session = req.session(true);
            Adventure game = new Adventure();
            games.put(session.id(), game);
            return game.getCurrentSceneAsJson();
        });

        // Endpoint: get current scene
        get("/scene", (req, res) -> {
            Session session = req.session(true);
            Adventure game = games.get(session.id());
            if (game == null) {
                game = new Adventure();
                games.put(session.id(), game);
            }
            return game.getCurrentSceneAsJson();
        });

        // Endpoint: make a choice
        post("/choice", (req, res) -> {
            Session session = req.session(true);
            Adventure game = games.get(session.id());
            if (game == null) {
                game = new Adventure();
                games.put(session.id(), game);
            }
            String choice = req.queryParams("choice");
            game.makeChoice(choice);
            return game.getCurrentSceneAsJson();
        });

        // Endpoint: set player name
        post("/set-name", (req, res) -> {
            Session session = req.session(true);
            Adventure game = games.get(session.id());
            if (game == null) {
                game = new Adventure();
                games.put(session.id(), game);
            }
            String name = req.queryParams("name");
            game.setPlayerName(name);
            return game.getCurrentSceneAsJson();
        });

        // Endpoint: use item from backpack
        post("/use-item", (req, res) -> {
            Session session = req.session(true);
            Adventure game = games.get(session.id());
            if (game == null) {
                game = new Adventure();
                games.put(session.id(), game);
            }
            String id = req.queryParams("id");
            game.useItemById(id);
            return game.getCurrentSceneAsJson();
        });

        // Endpoint: take a combat action
        post("/combat", (req, res) -> {
            Session session = req.session(true);
            Adventure game = games.get(session.id());
            if (game == null) {
                game = new Adventure();
                games.put(session.id(), game);
            }
            String action = req.queryParams("action");
            String id = req.queryParams("id");
            game.combatAction(action, id);
            return game.getCurrentSceneAsJson();
        });

        // Endpoint: equip weapon from backpack
        post("/equip-weapon", (req, res) -> {
            Session session = req.session(true);
            Adventure game = games.get(session.id());
            if (game == null) {
                game = new Adventure();
                games.put(session.id(), game);
            }
            String id = req.queryParams("id");
            game.equipWeaponById(id);
            return game.getCurrentSceneAsJson();
        });
    }
}
