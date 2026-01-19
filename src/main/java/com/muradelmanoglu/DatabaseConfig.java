package com.muradelmanoglu;

import java.sql.*;
import java.net.URI;
import java.util.Properties;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        if (dbUrl != null && !dbUrl.isEmpty()) {
            try {
                URI dbUri = new URI(dbUrl);
                String username = dbUri.getUserInfo().split(":")[0];
                String password = dbUri.getUserInfo().split(":")[1];
                String dbFullUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

                Properties props = new Properties();
                props.setProperty("user", username);
                props.setProperty("password", password);
                props.setProperty("ssl", "true");
                props.setProperty("sslmode", "require");

                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(dbFullUrl, props);
            } catch (Exception e) { throw new SQLException(e.getMessage()); }
        }
        // Lokal yoxlama üçün (şifrəni öz lokalına görə dəyişə bilərsən)
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/codepolis_db", "postgres", "12345");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // İstifadəçilər cədvəli
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");

            // Tapşırıqlar cədvəli (Fayl və GitHub linki əlavə edildi)
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
                    "id TEXT PRIMARY KEY, " +
                    "owner_nickname TEXT REFERENCES users(nickname) ON DELETE CASCADE, " +
                    "user_fullname TEXT, " +
                    "title TEXT, " +
                    "lesson TEXT, " +
                    "status TEXT, " +
                    "github_link TEXT, " +
                    "file_name TEXT, " +
                    "file_data TEXT, " + // Base64 formatında fayl mətni
                    "is_edited BOOLEAN DEFAULT FALSE, " +
                    "updated_at TEXT)");

            // Admin hesabı yaradılır
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan Əlizadə', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            System.out.println("✅ Verilənlər bazası strukturu uğurla yeniləndi.");
        } catch (SQLException e) {
            System.err.println("❌ Database initialize xətası!");
            e.printStackTrace();
        }
    }
}
