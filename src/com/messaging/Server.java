package com.messaging;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Thread;
import java.net.SocketException;



class ServerThread extends Thread { // Inherit from Thread
    private Socket socket; // Define socket
    private BufferedReader in; // Define input stream
    private PrintWriter out; // Define output stream
    int no; // Define the requested JICQ number

    public ServerThread(Socket s) throws IOException { // Thread constructor
        socket = s; // Get passed parameter
        in = new BufferedReader(new InputStreamReader(socket.getInputStream())); // Create input stream
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true); // Create output stream
        start(); // Start thread
    }

    private void notifyNewFriendship(int user1, int user2, String user2Nickname, String user2Ip, boolean user2Online) {
    System.out.println("Notifying users about new friendship: " + user1 + " and " + user2);
    
    // Get user1's info
    try (Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin");
         PreparedStatement pstmt = conn.prepareStatement(
             "SELECT nickname, ip, status FROM icq WHERE icqno = ?")) {
        
        pstmt.setInt(1, user1);
        ResultSet rs = pstmt.executeQuery();
        
        if (rs.next()) {
            String user1Nickname = rs.getString("nickname");
            String user1Ip = rs.getString("ip");
            boolean user1Online = rs.getBoolean("status");
            
            // Notify user2 about user1 (if online)
            if (user2Online && user2Ip != null && !user2Ip.equals("null")) {
                String messageToUser2 = "friend_added:" + user1 + ":" + user1Nickname;
                sendUdpNotification(user2Ip, messageToUser2);
                System.out.println("Notified user " + user2 + " about new friend " + user1);
            }
            
            // Notify user1 about user2 (if online)
            if (user1Online && user1Ip != null && !user1Ip.equals("null")) {
                String messageToUser1 = "friend_added:" + user2 + ":" + user2Nickname;
                sendUdpNotification(user1Ip, messageToUser1);
                System.out.println("Notified user " + user1 + " about new friend " + user2);
            }
        }
        
    } catch (SQLException e) {
        System.out.println("Error notifying users about friendship: " + e.getMessage());
    }
}
    // Helper method to check if two users are already friends
private boolean areFriends(int user1, int user2) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        // Use correct column names: icqno and friend
        String sql = "SELECT * FROM friend WHERE (icqno = ? AND friend = ?) OR (icqno = ? AND friend = ?)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, user1);
        pstmt.setInt(2, user2);
        pstmt.setInt(3, user2);
        pstmt.setInt(4, user1);
        rs = pstmt.executeQuery();
        
        return rs.next(); // If there's a result, they're already friends
        
    } catch (Exception e) {
        System.out.println("Error checking friendship: " + e.getMessage());
        e.printStackTrace();
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
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        // Use correct column names: requester_jicq and recipient_jicq
        String sql = "SELECT * FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, fromUser);
        pstmt.setInt(2, toUser);
        rs = pstmt.executeQuery();
        
        return rs.next(); // If there's a result, request exists
        
    } catch (Exception e) {
        System.out.println("Error checking pending requests: " + e.getMessage());
        e.printStackTrace();
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
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        // Use correct column names: requester_jicq and recipient_jicq
        String sql = "INSERT INTO friend_requests (requester_jicq, recipient_jicq, status, request_time) VALUES (?, ?, 'pending', CURRENT_TIMESTAMP)";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, fromUser);
        pstmt.setInt(2, toUser);
        
        int rows = pstmt.executeUpdate();
        return rows > 0;
        
    } catch (Exception e) {
        System.out.println("Error creating friend request: " + e.getMessage());
        e.printStackTrace();
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
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        // Column name is icqno, not jicqno
        String sql = "SELECT nickname FROM icq WHERE icqno = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, jicq);
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            return rs.getString("nickname");
        }
        return "Unknown";
        
    } catch (Exception e) {
        System.out.println("Error getting username: " + e.getMessage());
        e.printStackTrace();
        return "Unknown";
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

// Helper method to get online user's IP (you need to track this)
// You'll need to update your login handler to store user IPs

// Helper method to get online user's IP (add to Server class)



// Helper method to notify friends when a user goes offline

// Update the existing sendUdpNotification method or create a new one
// This method can send any type of UDP message

// Helper method with default port (5001)

// Helper method to send UDP notifications
// Helper method to send UDP notifications
private void sendUdpNotification(String targetIp, String message) {
    if (targetIp == null || targetIp.equals("null") || targetIp.isEmpty()) {
        System.out.println("Cannot send UDP notification: invalid IP address");
        return;
    }
    
    try {
        byte[] udpData = message.getBytes("UTF-8");
        
        DatagramSocket udpSocket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(udpData, udpData.length, 
                InetAddress.getByName(targetIp), 5001);
        udpSocket.send(packet);
        udpSocket.close();
        
        System.out.println("Sent UDP notification to " + targetIp + ":5001 - " + message);
        
    } catch (Exception e) {
        System.out.println("Failed to send UDP notification to " + targetIp + ": " + e.getMessage());
    }
}

// Helper method to send UDP offline notification
public void sendUdpOfflineNotification(String friendIp, int offlineUserId) {
    try {
        String udpMessage = "offline" + offlineUserId;
        byte[] udpData = udpMessage.getBytes();
        
        // Note: UDP port 5001 is where clients listen for notifications
        int udpPort = 5001;
        
        DatagramSocket udpSocket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(udpData, udpData.length, 
                InetAddress.getByName(friendIp), udpPort);
        udpSocket.send(packet);
        udpSocket.close();
        
        System.out.println("Sent UDP offline notification to " + friendIp + ":" + udpPort + 
                          " about user " + offlineUserId + " going offline");
        
    } catch (Exception e) {
        System.out.println("Failed to send UDP offline notification to " + friendIp + 
                          " about user " + offlineUserId + ": " + e.getMessage());
    }
}


// Update user online status in database
// Helper method to update user online status in database
// Helper method to update user online status in database




// Add this method to send pending friend requests to user when they login
private void sendPendingFriendRequests(int userId, String userIp) {
    System.out.println("Checking pending friend requests for user: " + userId);
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Get pending friend requests for this user - use correct column names
        String sql = "SELECT fr.requester_jicq, i.nickname " +
                    "FROM friend_requests fr " +
                    "JOIN icq i ON fr.requester_jicq = i.icqno " +
                    "WHERE fr.recipient_jicq = ? AND fr.status = 'pending'";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, userId);
        rs = pstmt.executeQuery();
        
        int pendingCount = 0;
        while (rs.next()) {
            pendingCount++;
            int requesterId = rs.getInt("requester_jicq");
            String requesterName = rs.getString("nickname");
            
            System.out.println("Found pending request from: " + requesterId + " (" + requesterName + ")");
            
            // Send UDP notification for each pending request
            String udpMessage = "oneaddyou" + requesterId;
            sendUdpNotification(userIp, udpMessage);
        }
        
        if (pendingCount > 0) {
            System.out.println("Sent " + pendingCount + " pending friend request notifications to user " + userId);
        } else {
            System.out.println("No pending friend requests for user " + userId);
        }
        
    } catch (Exception e) {
        System.out.println("Error checking pending friend requests: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}



    public void run() { // Thread listening function
        try {
            while (true) {
                String str = in.readLine(); // Get input string
               
                // Client disconnected, break out of the loop
                
                if (str == null || str.equals("end")) break; // If "end", close connection
                else if (str.equals("setudpport")) {
    int userId = Integer.parseInt(in.readLine());
    String udpPortStr = in.readLine();
    System.out.println("User " + userId + " reports UDP port: " + udpPortStr);
    // You could store this in a separate map if needed
}
   else if (str.equals("login")) {
    System.out.println("=== LOGIN ATTEMPT STARTED ===");
    
    // Read JICQ number and password
    String jicqStr = in.readLine();
    String password = in.readLine();
    
    if (jicqStr == null || password == null) {
        out.println("false");
        System.out.println("Login failed: Null input received");
        System.out.println("=== LOGIN ATTEMPT ENDED ===");
        return;
    }
    
    System.out.println("Login attempt - JICQ: " + jicqStr + ", Password: [hidden]");
    
    boolean loginSuccessful = false;
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        int userId = Integer.parseInt(jicqStr);
        
        // Check database for user - USING POSTGRESQL
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Note: column name is "icqno", not "jicqno" in PostgreSQL
        String sql = "SELECT * FROM icq WHERE icqno = ? AND password = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, userId);
        pstmt.setString(2, password);
        rs = pstmt.executeQuery();
        
        if (rs.next()) {
            loginSuccessful = true;
            System.out.println("Login successful for user: " + userId);
        } else {
            System.out.println("Login failed: Invalid credentials for user: " + userId);
        }
        
    } catch (NumberFormatException e) {
        System.out.println("Login failed: Invalid JICQ number format: " + jicqStr);
    } catch (ClassNotFoundException e) {
        System.out.println("Login failed: PostgreSQL driver not found: " + e.getMessage());
    } catch (SQLException e) {
        System.out.println("Login failed: Database error: " + e.getMessage());
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
    
    if (loginSuccessful) {
        try {
            int userId = Integer.parseInt(jicqStr);
            String userIp = socket.getInetAddress().getHostAddress();
            
            // Store in online users map
            if (Server.onlineUsers == null) {
                Server.onlineUsers = new ConcurrentHashMap<>();
            }
            Server.onlineUsers.put(userId, userIp);
            System.out.println("User " + userId + " is now online at IP: " + userIp);
            
            // Update database with online status - ALSO USE POSTGRESQL
            Server.updateUserOnlineStatus(userId, userIp, 1); // 1 = online
            
            // Also, check and send any pending friend requests for this user
            sendPendingFriendRequests(userId, userIp);
            
            out.println("ok");
            
        } catch (Exception e) {
            System.out.println("Error during login processing: " + e.getMessage());
            out.println("false");
        }
    } else {
        out.println("false");
    }
    
    System.out.println("=== LOGIN ATTEMPT ENDED ===");
}

                // Handle client new user registration request
                // Handle client new user registration request
      else if (str.equals("new")) {
    System.out.println("=== NEW USER REGISTRATION STARTED ===");
    try {
        Class.forName("org.postgresql.Driver");
        Connection c2 = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"  // Make sure this matches your actual password
        );
        
        // Read all registration data
        int icqno = Integer.parseInt(in.readLine());
        String nickname = in.readLine().trim();
        String password = in.readLine().trim();
        String email = in.readLine().trim();
        String info = in.readLine().trim();
        String place = in.readLine().trim();
        int picindex = Integer.parseInt(in.readLine());
        String sex = in.readLine().trim();
        
        // GET CLIENT IP ADDRESS
        String clientIP = socket.getInetAddress().getHostAddress();
        
        System.out.println("Registration data received:");
        System.out.println("  ICQ: " + icqno);
        System.out.println("  Nickname: " + nickname);
        System.out.println("  Password: " + password);
        System.out.println("  Email: " + email);
        System.out.println("  Info: " + info);
        System.out.println("  Place: " + place);
        System.out.println("  Pic: " + picindex);
        System.out.println("  Sex: " + sex);
        System.out.println("  IP: " + clientIP);
        
        // UPDATED: Include ip field
        String newsql = "insert into icq(icqno,nickname,password,email,info,place,pic,sex,ip) values(?,?,?,?,?,?,?,?,?)";
        PreparedStatement prepare2 = c2.prepareCall(newsql);
        
        prepare2.clearParameters();
        prepare2.setInt(1, icqno);
        prepare2.setString(2, nickname);
        prepare2.setString(3, password);
        prepare2.setString(4, email);
        prepare2.setString(5, info);
        prepare2.setString(6, place);
        prepare2.setInt(7, picindex);
        prepare2.setString(8, sex);
        prepare2.setString(9, clientIP);  // Set IP address
        
        int r3 = prepare2.executeUpdate();
        System.out.println("Database insert result: " + r3 + " rows affected");
        
        if (r3 == 1) { // Success
            // Get the assigned ICQ number (should be same as input)
            String sql2 = "select icqno from icq where icqno=?";
            PreparedStatement prepare3 = c2.prepareCall(sql2);
            prepare3.clearParameters();
            prepare3.setInt(1, icqno);
            ResultSet r2 = prepare3.executeQuery();
            
            if (r2.next()) {
                no = r2.getInt(1);
                System.out.println("New user registered with ICQ: " + no);
            }
            
            out.println(no); // Send the ICQ number
            out.println("ok"); // Send success status
            System.out.println("Registration SUCCESS - Sent: " + no + " and 'ok'");
            r2.close();
            prepare3.close();
        } else {
            out.println("-1"); // Send error code
            out.println("false"); // Send failure status
            System.out.println("Registration FAILED - Insert returned: " + r3);
        }
        System.out.println("=== NEW USER REGISTRATION ENDED ===");
        c2.close();
    } catch (Exception e) {
        System.out.println("Registration ERROR: " + e.getMessage());
        e.printStackTrace();
        out.println("-1");
        out.println("false");
    }
    socket.close();
} // End new user registration // End new user registration

                // Handle user search for friends
                else if (str.equals("find")) {
                    try {
                        Class.forName("org.postgresql.Driver");
                        Connection c3 = DriverManager.getConnection(
                            "jdbc:postgresql://localhost:5432/javaicq",
                            "postgres",
                            "admin"
                        );
                        // Connect to database and return other users' nickname, gender, hometown, personal info, etc.
                        String find = "select nickname,sex,place,ip,email,info,status from icq";
                        Statement st = c3.createStatement();
                        ResultSet result = st.executeQuery(find);
                        while (result.next()) {
                            out.println(result.getString("nickname"));
                            out.println(result.getString("sex"));
                            out.println(result.getString("place"));
                            out.println(result.getString("ip"));
                            out.println(result.getString("email"));
                            out.println(result.getString("info"));
                            // PostgreSQL boolean to string conversion
                            boolean statusBool = result.getBoolean("status");
                            out.println(statusBool ? "1" : "0");
                        } // End while
                        out.println("over");
                        int d, x;
                        boolean y;
                        // Return user's JICQ number, avatar index, and online status
                        ResultSet iset = st.executeQuery("select icqno,pic,status from icq");
                        while (iset.next()) {
                            d = iset.getInt("icqno");
                            out.println(d);
                            x = iset.getInt("pic"); // Avatar info
                            out.println(x);
                            y = iset.getBoolean("status");
                            if (y) {
                                out.println("1");
                            } else {
                                out.println("0");
                            }
                        }
                        iset.close();
                        c3.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("false");
                    }
                } // End find friends

                // Handle reading friend data when user logs in
                // Handle user sending a friend request
// In ServerThread.run() method, inside the main loop
                
// Handle user sending a friend request
// Handle user sending a friend request
else if (str.equals("addfriend")) {
    System.out.println("=== PROCESSING FRIEND REQUEST ===");
    
    String friendJicqStr = null;
    String myJicqStr = null;
    
    try {
        // Read parameters first
        friendJicqStr = in.readLine();
        myJicqStr = in.readLine();
        
        if (friendJicqStr == null || myJicqStr == null) {
            System.out.println("Invalid request: null parameters");
            out.println("error:invalid_request");
            return;
        }
        
        int friendicqno = Integer.parseInt(friendJicqStr.trim());
        int myicqno = Integer.parseInt(myJicqStr.trim());
        
        System.out.println("User " + myicqno + " wants to add friend " + friendicqno);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            // Get database connection
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/javaicq",
                "postgres",
                "admin"
            );
            
            // ============ 1. Check if friend exists ============
            System.out.println("Checking if user exists: " + friendicqno);
            String checkUserSql = "SELECT nickname, status, ip FROM icq WHERE icqno = ?";
            pstmt = conn.prepareStatement(checkUserSql);
            pstmt.setInt(1, friendicqno);
            rs = pstmt.executeQuery();
            
            if (!rs.next()) {
                System.out.println("Friend " + friendicqno + " not found");
                out.println("user_not_found");
                return;
            }
            
            String friendNickname = rs.getString("nickname");
            boolean friendStatus = rs.getBoolean("status");
            String friendIp = rs.getString("ip");
            rs.close();
            pstmt.close();
            
            // ============ 2. Check if trying to add yourself ============
            if (friendicqno == myicqno) {
                System.out.println("User cannot add themselves as friend");
                out.println("error:cannot_add_self");
                return;
            }
            
            // ============ 3. Check if already friends ============
            System.out.println("Checking if already friends");
            String checkFriendSql = "SELECT 1 FROM friend WHERE (icqno = ? AND friend = ?) OR (icqno = ? AND friend = ?)";
            pstmt = conn.prepareStatement(checkFriendSql);
            pstmt.setInt(1, myicqno);
            pstmt.setInt(2, friendicqno);
            pstmt.setInt(3, friendicqno);
            pstmt.setInt(4, myicqno);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Already friends: " + myicqno + " and " + friendicqno);
                out.println("already_friends");
                return;
            }
            rs.close();
            pstmt.close();
            
            // ============ 4. Check if pending request exists ============
            System.out.println("Checking for existing pending request");
            String checkRequestSql = "SELECT 1 FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'";
            pstmt = conn.prepareStatement(checkRequestSql);
            pstmt.setInt(1, myicqno);
            pstmt.setInt(2, friendicqno);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Friend request already exists from " + myicqno + " to " + friendicqno);
                out.println("request_exists");
                return;
            }
            rs.close();
            pstmt.close();
            
            // ============ 5. Check if reverse request exists ============
            System.out.println("Checking for reverse request");
            String checkReverseSql = "SELECT 1 FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'";
            pstmt = conn.prepareStatement(checkReverseSql);
            pstmt.setInt(1, friendicqno);
            pstmt.setInt(2, myicqno);
            rs = pstmt.executeQuery();
            
            if (rs.next()) {
                System.out.println("Reverse request exists - auto-accepting");
                
                // Auto-accept the reverse request
                String acceptSql = "UPDATE friend_requests SET status = 'accepted' WHERE requester_jicq = ? AND recipient_jicq = ?";
                pstmt = conn.prepareStatement(acceptSql);
                pstmt.setInt(1, friendicqno);
                pstmt.setInt(2, myicqno);
                pstmt.executeUpdate();
                pstmt.close();
                
                // Add friendship in both directions
                String insertFriend1Sql = "INSERT INTO friend (icqno, friend) VALUES (?, ?)";
                pstmt = conn.prepareStatement(insertFriend1Sql);
                pstmt.setInt(1, myicqno);
                pstmt.setInt(2, friendicqno);
                pstmt.executeUpdate();
                pstmt.close();
                
                String insertFriend2Sql = "INSERT INTO friend (icqno, friend) VALUES (?, ?)";
                pstmt = conn.prepareStatement(insertFriend2Sql);
                pstmt.setInt(1, friendicqno);
                pstmt.setInt(2, myicqno);
                pstmt.executeUpdate();
                pstmt.close();
                
                out.println("auto_accepted");
                System.out.println("Auto-accepted reverse friend request");
                
                // Notify both users
                notifyNewFriendship(myicqno, friendicqno, friendNickname, friendIp, friendStatus);
                return;
            }
            rs.close();
            pstmt.close();
            
            // ============ 6. Create friend request ============
            System.out.println("Creating new friend request");
            String insertRequestSql = "INSERT INTO friend_requests (requester_jicq, recipient_jicq, status, request_time) VALUES (?, ?, 'pending', CURRENT_TIMESTAMP)";
            pstmt = conn.prepareStatement(insertRequestSql);
            pstmt.setInt(1, myicqno);
            pstmt.setInt(2, friendicqno);
            int rowsInserted = pstmt.executeUpdate();
            
            if (rowsInserted == 1) {
                System.out.println("Friend request created from " + myicqno + " to " + friendicqno);
                out.println("request_sent");
                
                // ============ 7. Notify friend if online ============
                if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty() && friendStatus) {
                    // Get my nickname for the notification
                    String getMyNameSql = "SELECT nickname FROM icq WHERE icqno = ?";
                    PreparedStatement getNameStmt = conn.prepareStatement(getMyNameSql);
                    getNameStmt.setInt(1, myicqno);
                    ResultSet nameResult = getNameStmt.executeQuery();
                    
                    if (nameResult.next()) {
                        String myNickname = nameResult.getString("nickname");
                        
                        // Send UDP notification
                        System.out.println("Friend " + friendicqno + " is online at IP " + friendIp);
                        
                        // Use the new notification format
                        String udpMessage = "friend_request:" + myicqno + ":" + myNickname;
                        sendUdpNotification(friendIp, udpMessage);
                        
                        System.out.println("Sent UDP friend request notification to " + friendicqno);
                    }
                    nameResult.close();
                    getNameStmt.close();
                } else {
                    System.out.println("Friend " + friendicqno + " is offline (IP: " + friendIp + ", Status: " + friendStatus + ")");
                }
                
            } else {
                System.out.println("Failed to create friend request");
                out.println("request_failed");
            }
            
        } catch (ClassNotFoundException e) {
            System.out.println("Database driver error: " + e.getMessage());
            out.println("error:database");
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            e.printStackTrace();
            out.println("error:database");
        } finally {
            // Close resources
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        
    } catch (NumberFormatException e) {
        System.out.println("Invalid number format. Friend: " + friendJicqStr + ", My: " + myJicqStr);
        out.println("error:invalid_format");
    } catch (Exception e) {
        System.out.println("Unexpected error in addfriend: " + e.getMessage());
        e.printStackTrace();
        out.println("error:unexpected");
    }
    
    System.out.println("=== END FRIEND REQUEST PROCESSING ===");
} // End addfriend

// Helper method to notify both users about new friendship


// Helper method to notify both users about new friendship


// Handle accepting friend request
else if (str.equals("acceptfriendrequest")) {
    System.out.println("Processing friend request acceptance...");
    try {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Read parameters
        int requesterJicq = Integer.parseInt(in.readLine());  // Who sent the request
        int myJicq = Integer.parseInt(in.readLine());         // Me (accepting)
        
        System.out.println("User " + myJicq + " accepting friend request from " + requesterJicq);
        
        // Check if pending request exists
        String checkRequestSql = "SELECT 1 FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'";
        PreparedStatement checkRequestStmt = conn.prepareStatement(checkRequestSql);
        checkRequestStmt.setInt(1, requesterJicq);
        checkRequestStmt.setInt(2, myJicq);
        ResultSet requestCheck = checkRequestStmt.executeQuery();
        
        if (!requestCheck.next()) {
            out.println("no_request");
            System.out.println("No pending friend request found");
            conn.close();
            return;
        }
        
        // Update request status to accepted
        String updateRequestSql = "UPDATE friend_requests SET status = 'accepted' WHERE requester_jicq = ? AND recipient_jicq = ?";
        PreparedStatement updateRequestStmt = conn.prepareStatement(updateRequestSql);
        updateRequestStmt.setInt(1, requesterJicq);
        updateRequestStmt.setInt(2, myJicq);
        updateRequestStmt.executeUpdate();
        
        // Add friendship in both directions
        String insertFriend1Sql = "INSERT INTO friend (icqno, friend) VALUES (?, ?)";
        PreparedStatement insertFriend1Stmt = conn.prepareStatement(insertFriend1Sql);
        insertFriend1Stmt.setInt(1, myJicq);
        insertFriend1Stmt.setInt(2, requesterJicq);
        insertFriend1Stmt.executeUpdate();
        
        String insertFriend2Sql = "INSERT INTO friend (icqno, friend) VALUES (?, ?)";
        PreparedStatement insertFriend2Stmt = conn.prepareStatement(insertFriend2Sql);
        insertFriend2Stmt.setInt(1, requesterJicq);
        insertFriend2Stmt.setInt(2, myJicq);
        insertFriend2Stmt.executeUpdate();
        
        // Send friend information to acceptor
        out.println("accepted");
        
        // Get friend's information
        String friendInfoSql = "SELECT nickname, icqno, ip, status, pic, email, sex, info FROM icq WHERE icqno = ?";
        PreparedStatement friendInfoStmt = conn.prepareStatement(friendInfoSql);
        friendInfoStmt.setInt(1, requesterJicq);
        ResultSet friendInfo = friendInfoStmt.executeQuery();
        
        if (friendInfo.next()) {
            out.println(friendInfo.getString("nickname"));
            out.println(friendInfo.getInt("icqno"));
            out.println(friendInfo.getString("ip"));
            out.println(friendInfo.getBoolean("status") ? "1" : "0");
            out.println(friendInfo.getInt("pic"));
            out.println(friendInfo.getString("email"));
            out.println(friendInfo.getString("sex"));
            out.println(friendInfo.getString("info"));
        }
        out.println("over");
        
        System.out.println("Friend request accepted: " + myJicq + " and " + requesterJicq + " are now friends");
        
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.println("error");
        System.out.println("Error in acceptfriendrequest: " + e.getMessage());
    }
    System.out.println("End acceptfriendrequest processing");
} // End acceptfriendrequest

// Handle rejecting friend request
else if (str.equals("rejectfriendrequest")) {
    System.out.println("Processing friend request rejection...");
    try {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Read parameters
        int requesterJicq = Integer.parseInt(in.readLine());  // Who sent the request
        int myJicq = Integer.parseInt(in.readLine());         // Me (rejecting)
        
        System.out.println("User " + myJicq + " rejecting friend request from " + requesterJicq);
        
        // Update request status to rejected
        String updateRequestSql = "UPDATE friend_requests SET status = 'rejected' WHERE requester_jicq = ? AND recipient_jicq = ?";
        PreparedStatement updateRequestStmt = conn.prepareStatement(updateRequestSql);
        updateRequestStmt.setInt(1, requesterJicq);
        updateRequestStmt.setInt(2, myJicq);
        int rowsUpdated = updateRequestStmt.executeUpdate();
        
        if (rowsUpdated > 0) {
            out.println("rejected");
            System.out.println("Friend request rejected");
        } else {
            out.println("no_request");
            System.out.println("No pending friend request found to reject");
        }
        
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.println("error");
        System.out.println("Error in rejectfriendrequest: " + e.getMessage());
    }
    System.out.println("End rejectfriendrequest processing");
} // End rejectfriendrequest
// Handle getting pending friend requests
else if (str.equals("getpendingrequests")) {
    System.out.println("Getting pending friend requests...");
    try {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        int myJicq = Integer.parseInt(in.readLine());
        System.out.println("Getting pending requests for user: " + myJicq);
        
        // Get pending friend requests
        String pendingSql = "SELECT r.requester_jicq, i.nickname " +
                           "FROM friend_requests r " +
                           "JOIN icq i ON r.requester_jicq = i.icqno " +
                           "WHERE r.recipient_jicq = ? AND r.status = 'pending'";
        PreparedStatement pendingStmt = conn.prepareStatement(pendingSql);
        pendingStmt.setInt(1, myJicq);
        ResultSet pendingResult = pendingStmt.executeQuery();
        
        int count = 0;
        while (pendingResult.next()) {
            out.println(pendingResult.getInt("requester_jicq") + ":" + pendingResult.getString("nickname"));
            count++;
        }
        out.println("over");
        
        System.out.println("Found " + count + " pending requests for user " + myJicq);
        
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.println("error");
        System.out.println("Error in getpendingrequests: " + e.getMessage());
    }
    System.out.println("End getpendingrequests processing");
} // End getpendingrequests
// Handle checking pending friend requests count
else if (str.equals("checkpending")) {
    System.out.println("Checking pending friend requests count...");
    try {
        Class.forName("org.postgresql.Driver");
        Connection conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        int myJicq = Integer.parseInt(in.readLine());
        
        // Count pending friend requests
        String countSql = "SELECT COUNT(*) as count FROM friend_requests WHERE recipient_jicq = ? AND status = 'pending'";
        PreparedStatement countStmt = conn.prepareStatement(countSql);
        countStmt.setInt(1, myJicq);
        ResultSet countResult = countStmt.executeQuery();
        
        if (countResult.next()) {
            out.println(countResult.getInt("count"));
            System.out.println("User " + myJicq + " has " + countResult.getInt("count") + " pending requests");
        } else {
            out.println("0");
        }
        
        conn.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.println("0");
        System.out.println("Error in checkpending: " + e.getMessage());
    }
    System.out.println("End checkpending processing");
} // End checkpending

                // Handle if another user adds me, I add them back
                else if (str.equals("addnewfriend")) {
                    System.out.println("add");
                    try {
                        Class.forName("org.postgresql.Driver");
                        Connection c6 = DriverManager.getConnection(
                            "jdbc:postgresql://localhost:5432/javaicq",
                            "postgres",
                            "admin"
                        );
                        // Connect to database, add record to friend table based on user and friend numbers
                        int friendicqno = Integer.parseInt(in.readLine());
                        int myicqno = Integer.parseInt(in.readLine());
                        String addfriend = "insert into friend values(?,?)";
                        PreparedStatement prepare6 = c6.prepareCall(addfriend);
                        prepare6.clearParameters();
                        prepare6.setInt(1, myicqno);
                        prepare6.setInt(2, friendicqno);
                        int r6 = prepare6.executeUpdate();
                        if (r6 == 1) System.out.println("ok addfriend");
                        else System.out.println("false addfriend");
                        String friendinfo = "select nickname,icqno,ip,status,pic,email,sex,info from icq where icqno=?";
                        // If successful, send friend's basic info (like nickname) to user
                        PreparedStatement prepare5 = c6.prepareCall(friendinfo);
                        prepare5.clearParameters();
                        prepare5.setInt(1, friendicqno);
                        ResultSet r5 = prepare5.executeQuery();
                        boolean status;
                        while (r5.next()) {
                            out.println(r5.getString("nickname"));
                            out.println(r5.getInt("icqno"));
                            out.println(r5.getString("ip"));
                            status = r5.getBoolean("status");
                            if (status) out.println("1");
                            else out.println("0");
                            out.println(r5.getInt("pic"));
                            out.println(r5.getString("email"));
                            out.println(r5.getString("sex"));
                            out.println(r5.getString("info"));
                        }
                        out.println("over");
                        r5.close();
                        c6.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("false");
                    }
                    System.out.println("over addnewfriend");
                } // End handling mutual friend add

                // Handle user deleting a friend
                else if (str.equals("delfriend")) {
                    System.out.println("del");
                    try {
                        Class.forName("org.postgresql.Driver");
                        Connection c7 = DriverManager.getConnection(
                            "jdbc:postgresql://localhost:5432/javaicq",
                            "postgres",
                            "admin"
                        );
                        // Connect to database, delete record from friend table based on user and friend numbers
                        int friendicqno = Integer.parseInt(in.readLine());
                        System.out.println(friendicqno);
                        int myicqno = Integer.parseInt(in.readLine());
                        System.out.println(myicqno);
                        String addfriend = "delete from friend where icqno=? and friend=?";
                        PreparedStatement prepare7 = c7.prepareCall(addfriend);
                        prepare7.clearParameters();
                        prepare7.setInt(1, myicqno);
                        prepare7.setInt(2, friendicqno);
                        int r7 = prepare7.executeUpdate();
                        if (r7 == 1) System.out.println("ok delfriend"); // Success
                        else System.out.println("false delfriend"); // Failure
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("del false");
                    }
                } // End delete friend

                // Handle user logout
else if (str.equals("logout")) {
    System.out.println("=== LOGOUT PROCESS STARTED ===");
    
    // Read user's JICQ number
    String userIdStr = in.readLine();
    
    if (userIdStr == null) {
        System.out.println("Logout failed: Null user ID received");
        out.println("error");
        System.out.println("=== LOGOUT PROCESS ENDED ===");
        return;
    }
    
    try {
        int userId = Integer.parseInt(userIdStr);
        System.out.println("Processing logout for user: " + userId);
        
        // Check if user is actually online
        String currentIp = Server.getOnlineUserIp(userId);
        if (currentIp != null) {
            System.out.println("User " + userId + " is currently online from IP: " + currentIp);
        } else {
            System.out.println("User " + userId + " was not found in online users map (may already be offline)");
        }
        
        // Remove from online users map
        String removedIp = Server.onlineUsers.remove(userId);
        if (removedIp != null) {
            System.out.println("Removed user " + userId + " from online users map (IP: " + removedIp + ")");
        } else {
            System.out.println("User " + userId + " was not in online users map");
        }
        
        // Update database with offline status
        Server.updateUserOnlineStatus(userId, "null", 0); // 0 = offline
        
        // Also update the ip field to "null" in database
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
            String sql = "UPDATE icq SET ip = 'null', status = false WHERE icqno = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            int rowsUpdated = pstmt.executeUpdate();
            System.out.println("Updated database: set user " + userId + " to offline (rows affected: " + rowsUpdated + ")");
        } catch (SQLException e) {
            System.out.println("Error updating database for logout of user " + userId + ": " + e.getMessage());
        } finally {
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        
        // Notify friends that this user is offline
        Server.notifyFriendsOffline(userId);
        
        out.println("ok");
        System.out.println("Successfully processed logout for user: " + userId);
        
    } catch (NumberFormatException e) {
        System.out.println("Logout failed: Invalid user ID format: " + userIdStr);
        out.println("error");
    } catch (Exception e) {
        System.out.println("Unexpected error during logout: " + e.getMessage());
        e.printStackTrace();
        out.println("error");
    }
    
    System.out.println("=== LOGOUT PROCESS ENDED ===");
} // End logout handling

                // Handle who added me as friend so I can notify them when I come online
                // Handle who added me as friend so I can notify them when I come online
else if (str.equals("getwhoaddme")) {
    try {
        Class.forName("org.postgresql.Driver");
        Connection c9 = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        int myicqno = Integer.parseInt(in.readLine());
        System.out.println(myicqno + " is online......");
        
        // Get IPs of friends who added me AND users who sent me friend requests
        String getwhoaddme = 
            "SELECT DISTINCT ip FROM icq WHERE icqno IN (" +
            "    SELECT friend FROM friend WHERE icqno = ? " +  // My friends
            "    UNION " +
            "    SELECT requester_jicq FROM friend_requests WHERE recipient_jicq = ? AND status = 'pending'" +  // Pending requesters
            ") AND ip IS NOT NULL AND ip != 'null' AND ip != ''";
        
        PreparedStatement prepare6 = c9.prepareStatement(getwhoaddme);
        prepare6.clearParameters();
        prepare6.setInt(1, myicqno);
        prepare6.setInt(2, myicqno);
        ResultSet r6 = prepare6.executeQuery();
        
        while (r6.next()) {
            String ip = r6.getString("ip");
            if (ip != null && !ip.trim().isEmpty()) {
                out.println(ip.trim());
                System.out.println("Found IP to notify: " + ip.trim());
            }
        }
        
        out.println("over");
        c9.close();
        
    } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Error in getwhoaddme: " + e.getMessage());
    }
} // End online notification handling

                System.out.println("Echoing: " + str);
            }
            System.out.println("Close...");
        } catch (IOException e) {
            // Catch exception
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}

// Add this class definition BEFORE the Server class
class UdpServerThread extends Thread {
    private DatagramSocket socket;
    private boolean running;
    
    public UdpServerThread() throws SocketException {
        socket = new DatagramSocket(5001);
        System.out.println("UDP Server started on port 5001");
    }
    
    public void run() {
        running = true;
        byte[] buffer = new byte[1024];
        
        while (running) {
            try {
                // Receive UDP packet
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                String senderIp = packet.getAddress().getHostAddress();
                
                System.out.println("UDP Server received from " + senderIp + ": " + message);
                
                // Parse and relay message
                if (message.startsWith("relay:")) {
                    // Format: relay:targetJicq:actualMessage
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        try {
                            int targetJicq = Integer.parseInt(parts[1]);
                            String actualMessage = parts[2];
                            
                            // Get target IP from onlineUsers map
                            String targetIp = Server.getOnlineUserIp(targetJicq);
                            
                            if (targetIp != null && !targetIp.equals("null")) {
                                // Forward to target
                                byte[] forwardData = actualMessage.getBytes();
                                DatagramPacket forwardPacket = new DatagramPacket(
                                    forwardData, forwardData.length,
                                    InetAddress.getByName(targetIp), 5001);
                                socket.send(forwardPacket);
                                
                                System.out.println("Forwarded message to user " + targetJicq + 
                                                 " at IP " + targetIp);
                            } else {
                                System.out.println("Target user " + targetJicq + " is offline or IP unknown");
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid target JICQ format: " + parts[1]);
                        }
                    }
                }
                // Handle online/offline notifications
                else if (message.startsWith("online:")) {
                    // Format: online:userId
                    String[] parts = message.split(":");
                    if (parts.length == 2) {
                        try {
                            int userId = Integer.parseInt(parts[1]);
                            // Get all friends of this user and notify them
                            notifyFriendsOnline(userId, senderIp);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid user ID format in online notification");
                        }
                    }
                }
                else if (message.startsWith("offline:")) {
                    // Format: offline:userId
                    String[] parts = message.split(":");
                    if (parts.length == 2) {
                        try {
                            int userId = Integer.parseInt(parts[1]);
                            // Get all friends of this user and notify them
                            Server.notifyFriendsOffline(userId);
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid user ID format in offline notification");
                        }
                    }
                }
                // Handle file transfer requests
                else if (message.startsWith("filerequest:")) {
                    // Format: filerequest:targetJicq:filename
                    String[] parts = message.split(":", 3);
                    if (parts.length == 3) {
                        try {
                            int targetJicq = Integer.parseInt(parts[1]);
                            String filename = parts[2];
                            String targetIp = Server.getOnlineUserIp(targetJicq);
                            
                            if (targetIp != null && !targetIp.equals("null")) {
                                String forwardMessage = "readyreceive" + filename;
                                byte[] forwardData = forwardMessage.getBytes();
                                DatagramPacket forwardPacket = new DatagramPacket(
                                    forwardData, forwardData.length,
                                    InetAddress.getByName(targetIp), 5001);
                                socket.send(forwardPacket);
                                
                                System.out.println("Forwarded file request to user " + targetJicq);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid target JICQ format in file request");
                        }
                    }
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                if (running) {
                    System.out.println("UDP Server error: " + e.getMessage());
                }
            }
        }
        
        socket.close();
    }
    
    // Helper method to notify friends when user comes online
    private void notifyFriendsOnline(int userId, String userIp) {
        System.out.println("Notifying friends that user " + userId + " is online from IP " + userIp);
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/javaicq",
                "postgres",
                "admin"
            );
            
            // Get all friends of this user
            String sql = "SELECT friend FROM friend WHERE icqno = ? " +
                        "UNION " +
                        "SELECT icqno FROM friend WHERE friend = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            rs = pstmt.executeQuery();
            
            int notifiedFriends = 0;
            while (rs.next()) {
                int friendId = rs.getInt(1);
                
                // Check if friend is online
                String friendIp = Server.getOnlineUserIp(friendId);
                if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty()) {
                    // Send UDP online notification to friend
                    try {
                        String udpMessage = "online" + userId;
                        byte[] udpData = udpMessage.getBytes();
                        DatagramPacket packet = new DatagramPacket(udpData, udpData.length,
                                InetAddress.getByName(friendIp), 5001);
                        socket.send(packet);
                        notifiedFriends++;
                        System.out.println("Sent online notification to friend " + friendId);
                    } catch (Exception e) {
                        System.out.println("Failed to notify friend " + friendId + ": " + e.getMessage());
                    }
                }
            }
            
            System.out.println("Notified " + notifiedFriends + " friends about user " + userId + " coming online");
            
        } catch (Exception e) {
            System.out.println("Error notifying friends: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
    }
    
    // Helper method to notify friends when user goes offline
    
    
    public void stopServer() {
        running = false;
        socket.close();
    }
}

public class Server {
    
    public static Map<Integer, String> onlineUsers = new ConcurrentHashMap<>();
private static UdpServerThread udpServer;
    
    // Static method to get online user IP
    public static String getOnlineUserIp(int jicq) {
        if (Server.onlineUsers == null) {
            return null;
        }
        String ip = Server.onlineUsers.get(jicq);
        return ip; // Returns null if user not online
    }

// Call this when user logs in
private static void setUserOnline(int jicq, String ip) {
    onlineUsers.put(jicq, ip);
    System.out.println("User " + jicq + " set online with IP: " + ip);
}

// Call this when user logs out
private static void setUserOffline(int jicq) {
    onlineUsers.remove(jicq);
    System.out.println("User " + jicq + " set offline");
}
    private static void sendUdpOfflineNotification(String friendIp, int offlineUserId) {
        try {
            String udpMessage = "offline" + offlineUserId;
            byte[] udpData = udpMessage.getBytes();
            
            int udpPort = 5001;
            
            DatagramSocket udpSocket = new DatagramSocket();
            DatagramPacket packet = new DatagramPacket(udpData, udpData.length, 
                    InetAddress.getByName(friendIp), udpPort);
            udpSocket.send(packet);
            udpSocket.close();
            
            System.out.println("Sent UDP offline notification to " + friendIp + ":" + udpPort + 
                              " about user " + offlineUserId + " going offline");
            
        } catch (Exception e) {
            System.out.println("Failed to send UDP offline notification to " + friendIp + 
                              " about user " + offlineUserId + ": " + e.getMessage());
        }
    }

public static void notifyFriendsOffline(int userId) {
    System.out.println("Notifying friends that user " + userId + " is going offline");
    
    Connection conn = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    
    try {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Get all friends of this user - use correct column names
        String sql = "SELECT friend FROM friend WHERE icqno = ? " +
                    "UNION " +
                    "SELECT icqno FROM friend WHERE friend = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setInt(1, userId);
        pstmt.setInt(2, userId);
        rs = pstmt.executeQuery();
        
        int notifiedFriends = 0;
        while (rs.next()) {
            int friendId = rs.getInt(1);
            
            // Check if friend is online
            String friendIp = Server.getOnlineUserIp(friendId);
            if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty()) {
                // Send UDP offline notification to friend
                sendUdpOfflineNotification(friendIp, userId);
                notifiedFriends++;
                System.out.println("Sent offline notification to friend " + friendId + " at IP: " + friendIp);
            }
        }
        
        System.out.println("Notified " + notifiedFriends + " friends about user " + userId + " going offline");
        
    } catch (Exception e) {
        System.out.println("Error getting friend list for user " + userId + ": " + e.getMessage());
        e.printStackTrace();
    } finally {
        try { if (rs != null) rs.close(); } catch (SQLException e) {}
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}

    
public static void updateUserOnlineStatus(int jicq, String ip, int status) {
    Connection conn = null;
    PreparedStatement pstmt = null;
    
    try {
        Class.forName("org.postgresql.Driver");
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Use boolean true/false instead of integer 1/0
        String sql = "UPDATE icq SET ip = ?, status = ? WHERE icqno = ?";
        pstmt = conn.prepareStatement(sql);
        pstmt.setString(1, ip);
        pstmt.setBoolean(2, status == 1);  // Convert 1 to true, 0 to false
        pstmt.setInt(3, jicq);
        int rows = pstmt.executeUpdate();
        
        System.out.println("Updated user " + jicq + " status to " + 
                          (status == 1 ? "online" : "offline") + 
                          " with IP: " + ip + " (rows affected: " + rows + ")");
        
    } catch (Exception e) {
        System.out.println("Error updating online status for user " + jicq + ": " + e.getMessage());
        e.printStackTrace();
    } finally {
        try { if (pstmt != null) pstmt.close(); } catch (SQLException e) {}
        try { if (conn != null) conn.close(); } catch (SQLException e) {}
    }
}



    // Database initialization method - PostgreSQL version
    private static void initializeDatabase() {
    Connection conn = null;
    Statement stmt = null;
    
    try {
        // PostgreSQL driver
        Class.forName("org.postgresql.Driver");
        
        // First connect to postgres database
        conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/postgres",
            "postgres",
            "admin"
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
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
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
        
        // Create friend_requests table
        String createFriendRequestsTable = 
            "CREATE TABLE IF NOT EXISTS friend_requests (" +
            "    request_id SERIAL PRIMARY KEY," +
            "    requester_jicq INT NOT NULL," +
            "    recipient_jicq INT NOT NULL," +
            "    status VARCHAR(10) DEFAULT 'pending'," +
            "    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "    FOREIGN KEY (requester_jicq) REFERENCES icq(icqno)," +
            "    FOREIGN KEY (recipient_jicq) REFERENCES icq(icqno)," +
            "    UNIQUE (requester_jicq, recipient_jicq, status)" +
            ")";
        stmt.executeUpdate(createFriendRequestsTable);
        System.out.println("friend_requests table checked/created successfully");
        
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

    public static void main(String args[]) throws IOException {
        initializeDatabase();
        
        // Start TCP Server
        ServerSocket s = new ServerSocket(8080, 0, InetAddress.getByName("0.0.0.0"));
        System.out.println("TCP Server started on all interfaces: " + s);
        
        // Start UDP Server
        try {
            udpServer = new UdpServerThread();
            System.out.println("UDP Server thread started");
        } catch (SocketException e) {
            System.err.println("Failed to start UDP server on port 5001: " + e.getMessage());
            System.err.println("Make sure port 5001 is available and not blocked by firewall");
            e.printStackTrace();
        }
        
        try {
            while (true) {
                Socket socket = s.accept(); // Continuously listen for client requests
                System.out.println("TCP Connection accepted:" + socket);
                try {
                    new ServerThread(socket); // Create new thread
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            s.close();
            if (udpServer != null) {
                udpServer.stopServer();
            }
        }
    }
}