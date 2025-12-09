package com.messaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class ListAllUsers {
    public static void main(String[] args) {
        try {
            // Load driver
            Class.forName("org.postgresql.Driver");
            System.out.println("PostgreSQL JDBC Driver Registered!");
            
            // Connection parameters
            String url = "jdbc:postgresql://82.128.132.118:5432/javaicq";
            String user = "postgres";
            String password = "1234";
            
            System.out.println("Attempting to connect to: " + url);
            System.out.println("Username: " + user);
            
            // Add connection properties
            Properties props = new Properties();
            props.setProperty("user", user);
            props.setProperty("password", password);
            props.setProperty("ssl", "false");
            props.setProperty("connectTimeout", "10");
            props.setProperty("loginTimeout", "10");
            props.setProperty("tcpKeepAlive", "true");
            
            // Try connection
            System.out.println("Establishing connection...");
            long startTime = System.currentTimeMillis();
            
            try (Connection conn = DriverManager.getConnection(url, props)) {
                long endTime = System.currentTimeMillis();
                System.out.println("Connection successful! Time: " + (endTime - startTime) + "ms");
                
                // List users
                listAllUsers(conn);
                
            } catch (SQLException e) {
                System.err.println("\n=== CONNECTION FAILED ===");
                System.err.println("SQL State: " + e.getSQLState());
                System.err.println("Error Code: " + e.getErrorCode());
                System.err.println("Message: " + e.getMessage());
                System.err.println("Localized Message: " + e.getLocalizedMessage());
                
                // Check for common issues
                if (e.getMessage().contains("password authentication")) {
                    System.err.println("\nPossible issue: Wrong password or authentication method mismatch!");
                    System.err.println("Your pg_hba.conf uses 'md5' for remote connections.");
                    System.err.println("Try: ALTER USER postgres WITH PASSWORD '1234';");
                } else if (e.getMessage().contains("connection refused")) {
                    System.err.println("\nPossible issue: PostgreSQL not listening or firewall blocking!");
                    System.err.println("Check: 1) listen_addresses in postgresql.conf");
                    System.err.println("       2) Windows Firewall on port 5432");
                    System.err.println("       3) PostgreSQL service is running");
                } else if (e.getMessage().contains("timeout")) {
                    System.err.println("\nPossible issue: Network timeout or firewall blocking!");
                }
                
                e.printStackTrace();
            }
            
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL JDBC Driver not found!");
            System.err.println("Make sure postgresql-42.7.8.jar is in your classpath");
            e.printStackTrace();
        }
    }
    
    private static void listAllUsers(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM icq ORDER BY icqno")) {
            
            System.out.println("\n=== ALL USERS IN ICQ TABLE ===");
            int count = 0;
            while (rs.next()) {
                count++;
                System.out.println("User #" + count + ":");
                System.out.println("  ICQ No: " + rs.getInt("icqno"));
                System.out.println("  Nickname: " + rs.getString("nickname"));
                System.out.println("  Password: " + rs.getString("password"));
                System.out.println("  Email: " + rs.getString("email"));
                System.out.println("  Info: " + rs.getString("info"));
                System.out.println("  Place: " + rs.getString("place"));
                System.out.println("  Pic: " + rs.getInt("pic"));
                System.out.println("  Sex: " + rs.getString("sex"));
                System.out.println("  IP: " + rs.getString("ip"));
                System.out.println("  Status: " + rs.getBoolean("status"));
                System.out.println("----------------------------");
            }
            System.out.println("Total users: " + count);
        }
    }
}