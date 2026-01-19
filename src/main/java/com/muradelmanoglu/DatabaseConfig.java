package com.muradelmanoglu;

import java.sql.*;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        // Render panelindəki DB_URL dəyişənini oxuyuruq
        String dbUrl = System.getenv("DB_URL");

        if (dbUrl != null && !dbUrl.isEmpty()) {
            try {
                String jdbcUrl;

                // 1. Formatı yoxlayırıq və JDBC standartına salırıq
                if (dbUrl.startsWith("postgres://")) {
                    jdbcUrl = dbUrl.replace("postgres://", "jdbc:postgresql://");
                } else if (dbUrl.startsWith("postgresql://")) {
                    jdbcUrl = dbUrl.replace("postgresql://", "jdbc:postgresql://");
                } else {
                    jdbcUrl = dbUrl;
                }

                // 2. SSL tənzimləməsini yoxlayırıq (Render mütləq tələb edir)
                if (!jdbcUrl.contains("sslmode")) {
                    if (jdbcUrl.contains("?")) {
                        jdbcUrl += "&sslmode=require";
                    } else {
                        jdbcUrl += "?sslmode=require";
                    }
                }

                Class.forName("org.postgresql.Driver");
                // Bağlantı cəhdi
                return DriverManager.getConnection(jdbcUrl);

            } catch (Exception e) {
                System.err.println("Bağlantı xətası detalları: " + e.getMessage());
                throw new SQLException("Bağlantı xətası: " + e.getMessage());
            }
        }

        // Lokal üçün fallback
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/codepolis_db", "postgres", "");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname) ON DELETE CASCADE, user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");

            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan Əlizadə', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            System.out.println("✅ Verilənlər bazası uğurla qoşuldu.");
        } catch (SQLException e) {
            System.err.println("❌ Database initialize xətası!");
            e.printStackTrace();
        }
    }
}
