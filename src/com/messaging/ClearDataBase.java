package com.messaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;

public class ClearDataBase {
    // Add this method to Server.java
    private static void dropDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // Load PostgreSQL driver
            Class.forName("org.postgresql.Driver");
            
            // Connect to postgres database (default database)
            conn = DriverManager.getConnection(
                "jdbc:postgresql://82.128.132.118:5432/postgres",
                "postgres", 
                "1234"
            );
            stmt = conn.createStatement();
            
            System.out.println("Connected to PostgreSQL server");
            
            // Terminate all active connections to javaicq database
            System.out.println("Terminating active connections to javaicq database...");
            String terminateConnections = 
                "SELECT pg_terminate_backend(pg_stat_activity.pid) " +
                "FROM pg_stat_activity " +
                "WHERE pg_stat_activity.datname = 'javaicq' " +
                "AND pid <> pg_backend_pid();";
            
            stmt.execute(terminateConnections);
            System.out.println("Active connections terminated");
            
            // Drop the database if it exists
            System.out.println("Dropping javaicq database...");
            String dropSQL = "DROP DATABASE IF EXISTS javaicq;";
            
            stmt.executeUpdate(dropSQL);
            System.out.println("javaicq database dropped successfully!");
            
        } catch (Exception e) {
            System.err.println("Error dropping database: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Optional: Method to just clear tables but keep database
    private static void clearTables() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                "jdbc:postgresql://82.128.132.118:5432/javaicq",
                "postgres", 
                "1234"
            );
            stmt = conn.createStatement();
            
            System.out.println("Connected to javaicq database");
            
            // Disable foreign key checks (not needed in PostgreSQL, but we use CASCADE)
            System.out.println("Clearing tables...");
            
            // Clear friend table first (foreign key constraints)
            stmt.executeUpdate("TRUNCATE TABLE friend CASCADE;");
            System.out.println("Friend table cleared");
            
            // Clear icq table
            stmt.executeUpdate("TRUNCATE TABLE icq CASCADE;");
            System.out.println("Icq table cleared");
            
            // Reset sequences if any (optional)
            try {
                stmt.executeUpdate("ALTER SEQUENCE IF EXISTS icq_icqno_seq RESTART WITH 1;");
                System.out.println("Sequences reset");
            } catch (Exception e) {
                // No sequences, that's OK
            }
            
            System.out.println("All tables cleared successfully!");
            
        } catch (Exception e) {
            System.err.println("Error clearing tables: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    // Create fresh database and tables (like Server.java's initializeDatabase)
    private static void createFreshDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            Class.forName("org.postgresql.Driver");
            
            // First connect to postgres database
            conn = DriverManager.getConnection(
                "jdbc:postgresql://82.128.132.118:5432/postgres",
                "postgres",
                "1234"
            );
            
            // Check if database exists
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM pg_database WHERE datname = 'javaicq'");
            
            if (!rs.next()) {
                // Create database if it doesn't exist
                stmt.executeUpdate("CREATE DATABASE javaicq");
                System.out.println("javaicq database created successfully");
            } else {
                System.out.println("javaicq database already exists");
            }
            
            rs.close();
            stmt.close();
            conn.close();
            
            // Now connect to the javaicq database
            conn = DriverManager.getConnection(
                "jdbc:postgresql://82.128.132.118:5432/javaicq",
                "postgres",
                "1234"
            );
            
            stmt = conn.createStatement();
            
            // Create icq table if it doesn't exist
            String createIcqTable = 
                "CREATE TABLE IF NOT EXISTS icq (" +
                "    icqno INT PRIMARY KEY," +
                "    nickname VARCHAR(50)," +
                "    password VARCHAR(50)," +
                "    email VARCHAR(100)," +
                "    info VARCHAR(255)," +
                "    place VARCHAR(100)," +
                "    pic INT," +
                "    sex VARCHAR(10)," +
                "    ip VARCHAR(50)," +
                "    status BOOLEAN DEFAULT false" +
                ")";
            stmt.executeUpdate(createIcqTable);
            System.out.println("icq table checked/created successfully");
            
            // Create friend table if it doesn't exist
            String createFriendTable = 
                "CREATE TABLE IF NOT EXISTS friend (" +
                "    icqno INT," +
                "    friend INT," +
                "    PRIMARY KEY (icqno, friend)" +
                ")";
            stmt.executeUpdate(createFriendTable);
            System.out.println("friend table checked/created successfully");
            
            System.out.println("Database initialization completed successfully!");
            
        } catch (Exception e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (stmt != null) stmt.close();
                if (conn != null) conn.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        System.out.println("=== Database Cleanup Utility ===");
        System.out.println("1. Clearing tables only...");
        clearTables();
        
        System.out.println("\n2. Recreating tables...");
        createFreshDatabase();
        
        System.out.println("\n=== Cleanup Complete ===");
        
        // Uncomment if you want to drop entire database:
        // System.out.println("\nDropping entire database...");
        // dropDatabase();
        // System.out.println("\nCreating fresh database...");
        // createFreshDatabase();
    }
}