package com.muradelmanoglu;

import io.javalin.Javalin;
import java.sql.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {
    public static void main(String[] args) {
        DatabaseConfig.initializeDatabase();

        var app = Javalin.create(config -> {
            config.staticFiles.add("/public");
            config.bundledPlugins.enableCors(cors -> cors.addRule(it -> it.anyHost()));
        }).start(Integer.parseInt(System.getenv().getOrDefault("PORT", "10000")));

        // --- İSTİFADƏÇİ SİYAHISI (ADMİN ÜÇÜN) ---
        app.get("/api/users", ctx -> {
            List<Map<String, String>> users = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection(); 
                 Statement s = conn.createStatement(); 
                 ResultSet rs = s.executeQuery("SELECT * FROM users")) {
                while (rs.next()) {
                    users.add(Map.of(
                        "nickname", rs.getString("nickname"),
                        "fullName", rs.getString("full_name"),
                        "role", rs.getString("role")
                    ));
                }
                ctx.json(users);
            } catch (Exception e) { ctx.status(500).result("User list error: " + e.getMessage()); }
        });

        // --- BÜTÜN TAPŞIRIQLAR (ADMİN VƏ TƏLƏBƏ ÜÇÜN) ---
        app.get("/api/tasks", ctx -> {
            List<Map<String, Object>> tasks = new ArrayList<>();
            try (Connection conn = DatabaseConfig.getConnection(); 
                 Statement s = conn.createStatement(); 
                 ResultSet rs = s.executeQuery("SELECT * FROM tasks ORDER BY updated_at DESC")) {
                while (rs.next()) {
                    Map<String, Object> t = new HashMap<>();
                    t.put("id", rs.getString("id"));
                    t.put("ownerNickname", rs.getString("owner_nickname"));
                    t.put("userFullName", rs.getString("user_fullname"));
                    t.put("title", rs.getString("title"));
                    t.put("lesson", rs.getString("lesson"));
                    t.put("status", rs.getString("status"));
                    t.put("githubLink", rs.getString("github_link") != null ? rs.getString("github_link") : "");
                    t.put("fileName", rs.getString("file_name") != null ? rs.getString("file_name") : "");
                    t.put("fileData", rs.getString("file_data") != null ? rs.getString("file_data") : "");
                    t.put("isEdited", rs.getBoolean("is_edited"));
                    t.put("updatedAt", rs.getString("updated_at"));
                    tasks.add(t);
                }
                ctx.json(tasks);
            } catch (Exception e) { ctx.status(500).result("Task list error: " + e.getMessage()); }
        });

        // --- TAPŞIRIQ ƏLAVƏ ETMƏ ---
        app.post("/api/tasks/add", ctx -> {
            Map<String, String> t = ctx.bodyAsClass(Map.class);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            try (Connection conn = DatabaseConfig.getConnection(); 
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?)")) {
                ps.setString(1, UUID.randomUUID().toString());
                ps.setString(2, t.get("ownerNickname"));
                ps.setString(3, t.get("userFullName"));
                ps.setString(4, t.get("title"));
                ps.setString(5, t.get("lesson"));
                ps.setString(6, "In Progress");
                ps.setString(7, t.get("githubLink"));
                ps.setString(8, t.get("fileName"));
                ps.setString(9, t.get("fileData"));
                ps.setString(10, time);
                ps.executeUpdate();
                ctx.status(201).json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500).result(e.getMessage()); }
        });

        // --- LOGİN ---
        app.post("/api/login", ctx -> {
            Map<String, String> body = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement pstmt = conn.prepareStatement("SELECT * FROM users WHERE nickname = ? AND password = ?")) {
                pstmt.setString(1, body.get("nickname").trim().toLowerCase());
                pstmt.setString(2, body.get("password"));
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    ctx.json(Map.of("success", true, "user", Map.of(
                        "nickname", rs.getString("nickname"),
                        "fullName", rs.getString("full_name"),
                        "role", rs.getString("role")
                    )));
                } else { ctx.status(401).json(Map.of("success", false)); }
            }
        });

        // Diqqət: Digər silmə və şifrə dəyişmə endpointləri (əvvəlki kimi) bura əlavə edilməlidir.
        app.post("/api/admin/delete-user", ctx -> {
            try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE nickname = ?")) {
                ps.setString(1, ctx.queryParam("nickname"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500); }
        });
        
        app.post("/api/tasks/delete", ctx -> {
            try (Connection conn = DatabaseConfig.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                ps.setString(1, ctx.queryParam("id"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500); }
        });
    }
}
