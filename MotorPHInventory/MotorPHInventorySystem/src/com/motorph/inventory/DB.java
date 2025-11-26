package com.motorph.inventory;

import java.nio.file.*;
import java.sql.*;

public final class DB {
    private static final String URL = "jdbc:sqlite:data/motorph_inventory.db";

    static {
        try {
            Files.createDirectories(Paths.get("data"));
            Class.forName("org.sqlite.JDBC");
        } catch (Exception e) {
            throw new RuntimeException("SQLite JDBC driver not on the classpath", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        ensureSchema(conn);
        return conn;
    }

    private static void ensureSchema(Connection conn) throws SQLException {
        try (Statement st = conn.createStatement()) {
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS inventory (
                    engine_number TEXT PRIMARY KEY,
                    brand         TEXT NOT NULL,
                    date_entered  INTEGER NOT NULL,  -- epoch millis
                    status        TEXT NOT NULL,
                    level         TEXT NOT NULL
                )
            """);
            // Optional: users table (if you later want DB-backed AuthService)
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS users (
                    username TEXT PRIMARY KEY,
                    password TEXT NOT NULL,
                    role     TEXT NOT NULL
                )
            """);
            // ...existing code...
            st.executeUpdate("""
                CREATE TABLE IF NOT EXISTS audit_log (
                    id            INTEGER PRIMARY KEY AUTOINCREMENT,
                    ts            INTEGER NOT NULL,     -- epoch millis
                    user          TEXT,
                    action        TEXT NOT NULL,        -- ADD / DELETE / RECONCILE / etc.
                    engine_number TEXT,
                    status        TEXT,
                    level         TEXT,
                    outcome       TEXT NOT NULL,        -- SUCCESS / REJECTED / DENIED / CANCEL / ERROR
                    details       TEXT
                )
            """);
        }
    }

    public static void ensureReady() {
        try (Connection ignored = getConnection()) {
            System.out.println("[DB] Ready at " + Paths.get("data/motorph_inventory.db").toAbsolutePath());
        } catch (SQLException e) {
            throw new RuntimeException("Database init failed: " + e.getMessage(), e);
        }
    }

    

    private DB() {}
}