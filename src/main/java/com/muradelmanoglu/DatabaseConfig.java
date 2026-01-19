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
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/codepolis_db", "postgres", "12345");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // MƏCBURİ SIFIRLAMA (Hər şeyi silirik ki, yeni sütunlar işləsin)
            stmt.execute("DROP TABLE IF EXISTS tasks CASCADE");
            stmt.execute("DROP TABLE IF EXISTS users CASCADE");

            // Yenidən yaratma
            stmt.execute("CREATE TABLE users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            
            stmt.execute("CREATE TABLE tasks (" +
                    "id TEXT PRIMARY KEY, " +
                    "owner_nickname TEXT REFERENCES users(nickname) ON DELETE CASCADE, " +
                    "user_fullname TEXT, " +
                    "title TEXT, " +
                    "lesson TEXT, " +
                    "status TEXT, " +
                    "github_link TEXT, " +
                    "file_name TEXT, " +
                    "file_data TEXT, " + 
                    "is_edited BOOLEAN DEFAULT FALSE, " +
                    "updated_at TEXT)");

            // Admini mütləq bərpa edirik
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan Əlizadə', '12345', 'ADMIN')");
            
            System.out.println("✅ Database tam sıfırlandı və yeni struktur quruldu!");
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
