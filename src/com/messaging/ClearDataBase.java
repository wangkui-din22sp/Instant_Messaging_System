package com.messaging;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ClearDataBase {
    // Add this method to Server.java
private static void dropDatabase() {
    Connection conn = null;
    Statement stmt = null;
    
    try {
        // Connect to master database
        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        conn = DriverManager.getConnection(
            "jdbc:sqlserver://localhost:1433;databaseName=master;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
        );
        stmt = conn.createStatement();
        
        // Close existing connections and drop database
        String dropSQL = 
            "IF EXISTS (SELECT name FROM sys.databases WHERE name = 'javaicq') " +
            "BEGIN " +
            "    ALTER DATABASE javaicq SET SINGLE_USER WITH ROLLBACK IMMEDIATE; " +
            "    DROP DATABASE javaicq; " +
            "    PRINT 'javaicq database dropped successfully'; " +
            "END " +
            "ELSE " +
            "    PRINT 'javaicq database does not exist';";
        
        stmt.executeUpdate(dropSQL);
        System.out.println("Database cleanup completed!");
        
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

public static void main(String args[]) throws IOException {

    dropDatabase();
    


}
}

// You can call this from main method if needed
