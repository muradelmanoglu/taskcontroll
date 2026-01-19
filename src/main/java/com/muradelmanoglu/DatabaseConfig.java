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
                String userInfo = dbUri.getUserInfo();
                String username = userInfo.split(":")[0];
                String password = userInfo.split(":")[1];
                String dbFullUrl = "jdbc:postgresql://" + dbUri.getHost() + ":" + dbUri.getPort() + dbUri.getPath();

                Properties props = new Properties();
                props.setProperty("user", username);
                props.setProperty("password", password);
                props.setProperty("ssl", "true");
                props.setProperty("sslmode", "require");

                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(dbFullUrl, props);
            } catch (Exception e) { throw new SQLException("Bağlantı xətası: " + e.getMessage()); }
        }
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/codepolis_db", "postgres", "12345");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // DİQQƏT: Əgər sütun xətası varsa, aşağıdakı iki sətri BİR DƏFƏLİK aktiv et (commentdən çıxar) ki, bazanı sıfırlasın:
            // stmt.execute("DROP TABLE IF EXISTS tasks");
            // stmt.execute("DROP TABLE IF EXISTS users CASCADE");

            // 1. İstifadəçilər
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            
            // 2. Tapşırıqlar (Bütün lazım olan sütunlarla)
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (" +
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

            // Admini yenidən yaradaq
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan Əlizadə', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            
            System.out.println("✅ Verilənlər bazası strukturu tam hazırdır.");
        } catch (SQLException e) { 
            System.err.println("❌ Database xətası: " + e.getMessage());
            e.printStackTrace(); 
        }
    }
}
