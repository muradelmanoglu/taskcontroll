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

        // --- AUTH: LOGIN ---
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
                } else { ctx.json(Map.of("success", false, "message", "Məlumatlar yanlışdır!")); }
            } catch (Exception e) { ctx.status(500); }
        });

        // --- AUTH: REGISTER ---
        app.post("/api/register", ctx -> {
            Map<String, String> b = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("INSERT INTO users VALUES (?, ?, ?, ?)")) {
                ps.setString(1, b.get("nickname").toLowerCase().trim());
                ps.setString(2, b.get("fullName"));
                ps.setString(3, b.get("password"));
                ps.setString(4, "STUDENT");
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.json(Map.of("success", false)); }
        });

        // --- USER: PASSWORD CHANGE ---
        app.post("/api/users/change-password", ctx -> {
            Map<String, String> b = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE users SET password = ? WHERE nickname = ?")) {
                ps.setString(1, b.get("newPassword"));
                ps.setString(2, b.get("nickname"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500).json(Map.of("success", false)); }
        });

        // --- ADMIN: DELETE USER ---
        app.post("/api/admin/delete-user", ctx -> {
            String nick = ctx.queryParam("nickname");
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM users WHERE nickname = ?")) {
                ps.setString(1, nick);
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500); }
        });

        // --- TASKS: GET ALL ---
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
                    t.put("githubLink", rs.getString("github_link"));
                    t.put("fileName", rs.getString("file_name"));
                    t.put("fileData", rs.getString("file_data"));
                    t.put("isEdited", rs.getBoolean("is_edited"));
                    t.put("updatedAt", rs.getString("updated_at"));
                    tasks.add(t);
                }
                ctx.json(tasks);
            } catch (Exception e) { ctx.status(500); }
        });

        // --- TASKS: ADD (WITH FILE & GITHUB) ---
        app.post("/api/tasks/add", ctx -> {
            Map<String, String> t = ctx.bodyAsClass(Map.class);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "INSERT INTO tasks VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, FALSE, ?)")) {
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
            } catch (Exception e) { e.printStackTrace(); ctx.status(500); }
        });

        // --- TASKS: UPDATE (STUDENT EDIT) ---
        app.post("/api/tasks/update", ctx -> {
            Map<String, String> b = ctx.bodyAsClass(Map.class);
            String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "UPDATE tasks SET title = ?, lesson = ?, github_link = ?, file_name = ?, file_data = ?, is_edited = TRUE, updated_at = ? WHERE id = ?")) {
                ps.setString(1, b.get("title"));
                ps.setString(2, b.get("lesson"));
                ps.setString(3, b.get("githubLink"));
                ps.setString(4, b.get("fileName"));
                ps.setString(5, b.get("fileData"));
                ps.setString(6, time);
                ps.setString(7, b.get("id"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500); }
        });

        // --- TASKS: CHANGE STATUS (ADMIN) ---
        app.post("/api/tasks/status", ctx -> {
            Map<String, String> b = ctx.bodyAsClass(Map.class);
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("UPDATE tasks SET status = ? WHERE id = ?")) {
                ps.setString(1, b.get("status"));
                ps.setString(2, b.get("id"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500); }
        });

        // --- TASKS: DELETE (ADMIN) ---
        app.post("/api/tasks/delete", ctx -> {
            try (Connection conn = DatabaseConfig.getConnection();
                 PreparedStatement ps = conn.prepareStatement("DELETE FROM tasks WHERE id = ?")) {
                ps.setString(1, ctx.queryParam("id"));
                ps.executeUpdate();
                ctx.json(Map.of("success", true));
            } catch (Exception e) { ctx.status(500); }
        });
    }
}
