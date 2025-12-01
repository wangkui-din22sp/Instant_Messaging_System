package com.messaging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;



public class ListAllUsers {

    private static void listAllUsers() {
    try (                        

                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
        Connection conn = DriverManager.getConnection(
            "jdbc:sqlserver://0.0.0.0:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true");
         Statement stmt = conn.createStatement();
         ResultSet rs = stmt.executeQuery("SELECT * FROM icq ORDER BY icqno")) {
        
        System.out.println("=== ALL USERS IN ICQ TABLE ===");
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
        
    } catch (SQLException e) {
        System.err.println("Error listing users: " + e.getMessage());
    }
}

    public static void main(String[] args) {
        listAllUsers();
    }

}
