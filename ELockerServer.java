package com.elocker;

import static spark.Spark.*;
import com.google.gson.Gson;
import java.util.HashMap;
import java.util.Map;

public class ELockerServer {
    private static final Gson gson = new Gson();
    private static final Map<String, User> users = new HashMap<>();
    private static final Map<String, Document> documents = new HashMap<>();

    public static void main(String[] args) {
        // Enable CORS
        options("/*", (request, response) -> {
            String accessControlRequestHeaders = request.headers("Access-Control-Request-Headers");
            if (accessControlRequestHeaders != null) {
                response.header("Access-Control-Allow-Headers", accessControlRequestHeaders);
            }
            String accessControlRequestMethod = request.headers("Access-Control-Request-Method");
            if (accessControlRequestMethod != null) {
                response.header("Access-Control-Allow-Methods", accessControlRequestMethod);
            }
            return "OK";
        });

        before((request, response) -> {
            response.header("Access-Control-Allow-Origin", "*");
            response.header("Access-Control-Allow-Headers", "*");
            response.type("application/json");
        });

        // User endpoints
        post("/api/users/signup", (req, res) -> {
            User user = gson.fromJson(req.body(), User.class);
            if (users.containsKey(user.getUserId())) {
                res.status(400);
                return gson.toJson(Map.of("error", "Username already exists"));
            }
            users.put(user.getUserId(), user);
            return gson.toJson(Map.of("message", "User registered successfully"));
        });

        post("/api/users/login", (req, res) -> {
            User loginUser = gson.fromJson(req.body(), User.class);
            User storedUser = users.get(loginUser.getUserId());
            if (storedUser != null && storedUser.getPassword().equals(loginUser.getPassword())) {
                return gson.toJson(Map.of("message", "Login successful"));
            }
            res.status(401);
            return gson.toJson(Map.of("error", "Invalid credentials"));
        });

        // Document endpoints
        post("/api/documents", (req, res) -> {
            Document doc = gson.fromJson(req.body(), Document.class);
            documents.put(doc.getDocumentId(), doc);
            return gson.toJson(Map.of("message", "Document added successfully"));
        });

        get("/api/documents/:username", (req, res) -> {
            String username = req.params(":username");
            return gson.toJson(documents.values().stream()
                .filter(doc -> doc.getOwnerId().equals(username))
                .toList());
        });

        delete("/api/documents/:id", (req, res) -> {
            String docId = req.params(":id");
            if (documents.remove(docId) != null) {
                return gson.toJson(Map.of("message", "Document deleted successfully"));
            }
            res.status(404);
            return gson.toJson(Map.of("error", "Document not found"));
        });

        // Start the server
        port(4567);
    }
} 