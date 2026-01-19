package com.muradelmanoglu;

import java.sql.*;

public class DatabaseConfig {
    public static Connection getConnection() throws SQLException {
        // Render panelində təyin etdiyin Environment Variable-ı oxuyur
        String dbUrl = System.getenv("DB_URL");

        if (dbUrl != null && !dbUrl.isEmpty()) {
            try {
                // Render-dən gələn 'postgresql://' linkini JDBC formatına 'jdbc:postgresql://' çeviririk
                String jdbcUrl = dbUrl.replace("postgresql://", "jdbc:postgresql://");

                // SSL sertifikatı Render/PostgreSQL üçün mütləqdir
                if (!jdbcUrl.contains("?")) {
                    jdbcUrl += "?sslmode=require";
                } else if (!jdbcUrl.contains("sslmode")) {
                    jdbcUrl += "&sslmode=require";
                }

                Class.forName("org.postgresql.Driver");
                return DriverManager.getConnection(jdbcUrl);
            } catch (Exception e) {
                e.printStackTrace();
                throw new SQLException("Bazaya qoşulma xətası: " + e.getMessage());
            }
        }

        // Lokal yoxlama üçün (əgər kompüterində işlədirsənsə)
        return DriverManager.getConnection("jdbc:postgresql://localhost:5432/codepolis_db", "postgres", "");
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Cədvəllər yoxdursa yaradılır
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname) ON DELETE CASCADE, user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");

            // Admin istifadəçisini yaradırıq (orxan / 12345)
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan Əlizadə', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            System.out.println("✅ Verilənlər bazası qoşuldu və hazır vəziyyətə gətirildi.");
        } catch (SQLException e) {
            System.err.println("❌ Database hazırlığı zamanı xəta!");
            e.printStackTrace();
        }
    }
}
