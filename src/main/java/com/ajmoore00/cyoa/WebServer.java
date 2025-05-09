package com.ajmoore00.cyoa;

import static spark.Spark.*;
import com.google.gson.Gson;
import spark.Session;

import java.util.HashMap;
import java.util.Map;

public class WebServer {
    // Store games per session
    private static Map<String, Adventure> games = new HashMap<>();

    public static void main(String[] args) {
        port(4567);
        staticFiles.location("/public"); // Serve files from src/main/resources/public
        Gson gson = new Gson();

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
    }
}
