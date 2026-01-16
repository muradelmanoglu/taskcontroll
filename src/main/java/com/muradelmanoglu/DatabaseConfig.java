package com.muradelmanoglu;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {

    public static Connection getConnection() throws SQLException {
        // Render-in daxili DATABASE_URL dəyişənini oxuyuruq
        String dbUrl = System.getenv("DATABASE_URL");
        
        if (dbUrl == null || dbUrl.isEmpty()) {
            dbUrl = System.getenv("DB_URL");
        }

        if (dbUrl == null || dbUrl.isEmpty()) {
            throw new SQLException("XƏTA: DATABASE_URL və ya DB_URL mühit dəyişəni tapılmadı!");
        }

        // postgresql:// formatını JDBC (jdbc:postgresql://) formatına çeviririk
        String jdbcUrl = dbUrl.replace("postgres://", "jdbc:postgresql://")
                             .replace("postgresql://", "jdbc:postgresql://");

        try {
            // PostgreSQL drayverini yükləyirik
            Class.forName("org.postgresql.Driver");
            // SSL parametrləri olmadan birbaşa qoşuluruq
            return DriverManager.getConnection(jdbcUrl);
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL Driver tapılmadı: " + e.getMessage());
        } catch (Exception e) {
            throw new SQLException("Bağlantı xətası: " + e.getMessage());
        }
    }

    public static void initializeDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Cədvəllərin yaradılması
            stmt.execute("CREATE TABLE IF NOT EXISTS users (nickname TEXT PRIMARY KEY, full_name TEXT, password TEXT, role TEXT)");
            stmt.execute("CREATE TABLE IF NOT EXISTS tasks (id TEXT PRIMARY KEY, owner_nickname TEXT REFERENCES users(nickname), user_fullname TEXT, title TEXT, lesson TEXT, status TEXT, file_name TEXT, updated_at TEXT)");
            
            // İlkin admin istifadəçisi (əgər yoxdursa)
            stmt.execute("INSERT INTO users (nickname, full_name, password, role) VALUES ('orxan', 'Orxan', '12345', 'ADMIN') ON CONFLICT DO NOTHING");
            
            System.out.println("✅ Verilənlər bazası uğurla qoşuldu və hazırlandı!");
        } catch (SQLException e) {
            System.err.println("❌ Database init xətası: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
