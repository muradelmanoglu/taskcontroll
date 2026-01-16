package com.muradelmanoglu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    public static Connection getConnection() throws SQLException {
        String dbUrl = System.getenv("DB_URL");
        
        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new SQLException("DB_URL mühit dəyişəni tapılmadı!");
        }

        // URL-i birbaşa JDBC formatına çeviririk (ən təhlükəsiz yol budur)
        String jdbcUrl = dbUrl.replace("postgres://", "jdbc:postgresql://")
                             .replace("postgresql://", "jdbc:postgresql://");

        // SSL-i mütləq sona əlavə edirik
        if (!jdbcUrl.contains("sslmode")) {
            jdbcUrl += (jdbcUrl.contains("?") ? "&" : "?") + "sslmode=require";
        }

        try {
            Class.forName("org.postgresql.Driver");
            return DriverManager.getConnection(jdbcUrl);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver tapılmadı!");
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname), user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            System.out.println("✅ Verilənlər bazası uğurla qoşuldu!");
        } catch (SQLException e) {
            System.err.println("❌ Database xətası: " + e.getMessage());
        }
    }
}
