package com;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class Server1 {

    // Helper method to check if two users are already friends
private boolean areFriends(int user1, int user2) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:derby:javaicq;create=true");
        String sql = "SELECT * FROM friend WHERE (userid = ? AND friendid = ?) OR (userid = ? AND friendid = ?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, user1);
        pstmt.setInt(2, user2);
        pstmt.setInt(3, user2);
        pstmt.setInt(4, user1);
        rs = pstmt.executeQuery();
        
        return rs.next(); // If there's a result, they're already friends
        
    } catch (SQLException e) {
        System.out.println("Error checking friendship: " + e.getMessage());
        return false;
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

// Helper method to check if a pending request exists
private boolean hasPendingRequest(int fromUser, int toUser) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:derby:javaicq;create=true");
        String sql = "SELECT * FROM friend_requests WHERE requester_id = ? AND requestee_id = ? AND status = 'pending'";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, fromUser);
        pstmt.setInt(2, toUser);
        rs = pstmt.executeQuery();
        
        return rs.next(); // If there's a result, request exists
        
    } catch (SQLException e) {
        System.out.println("Error checking pending requests: " + e.getMessage());
        return false;
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

// Helper method to create a friend request
private boolean createFriendRequest(int fromUser, int toUser) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:derby:javaicq;create=true");
        String sql = "INSERT INTO friend_requests (requester_id, requestee_id, status, request_date) VALUES (?, ?, 'pending', CURRENT_TIMESTAMP)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, fromUser);
        pstmt.setInt(2, toUser);
        
        int rows = pstmt.executeUpdate();
        return rows > 0;
        
    } catch (SQLException e) {
        System.out.println("Error creating friend request: " + e.getMessage());
        return false;
    } finally {
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

// Helper method to get user's nickname
private String getUserName(int jicq) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:derby:javaicq;create=true");
        String sql = "SELECT nickname FROM icq WHERE jicqno = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, jicq);
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getString("nickname");
        }
        return "Unknown";
        
    } catch (SQLException e) {
        System.out.println("Error getting username: " + e.getMessage());
        return "Unknown";
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

// Helper method to get online user's IP (you need to track this)
// You'll need to update your login handler to store user IPs
private Map<Integer, String> onlineUsers = new HashMap<>();

// Call this when user logs in
private void setUserOnline(int jicq, String ip) {
    onlineUsers.put(jicq, ip);
    System.out.println("User " + jicq + " set online with IP: " + ip);
}

// Call this when user logs out
private void setUserOffline(int jicq) {
    onlineUsers.remove(jicq);
    System.out.println("User " + jicq + " set offline");
}

// Get online user IP
private String getOnlineUserIp(int jicq) {
    return onlineUsers.get(jicq);
}

// Update user online status in database
private void updateUserOnlineStatus(int jicq, String ip, int status) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        conn = DriverManager.getConnection("jdbc:derby:javaicq;create=true");
        String sql = "UPDATE icq SET ip = ?, status = ? WHERE jicqno = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, ip);
        pstmt.setInt(2, status);
        pstmt.setInt(3, jicq);
        pstmt.executeUpdate();
        
    } catch (SQLException e) {
        System.out.println("Error updating online status: " + e.getMessage());
    } finally {
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}
    
}
