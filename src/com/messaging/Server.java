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
import java.util.Vector;
import java.sql.SQLException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.HashMap;
import java.util.Map;


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
// Helper method to get online user's IP (add to Server class)



// Helper method to notify friends when a user goes offline
private void notifyFriendsOffline(int userId) {
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

// Update the existing sendUdpNotification method or create a new one
// This method can send any type of UDP message
private void sendUdpNotification(String targetIp, String message, int port) {
    try {
        byte[] udpData = message.getBytes();
        
        DatagramSocket udpSocket = new DatagramSocket();
        DatagramPacket packet = new DatagramPacket(udpData, udpData.length, 
                InetAddress.getByName(targetIp), port);
        udpSocket.send(packet);
        udpSocket.close();
        
        System.out.println("Sent UDP notification to " + targetIp + ":" + port + 
                          " with message: " + message);
        
    } catch (Exception e) {
        System.out.println("Failed to send UDP notification to " + targetIp + ":" + port + 
                          ": " + e.getMessage());
    }
}

// Helper method with default port (5001)
private void sendUdpNotification(String targetIp, String message) {
    sendUdpNotification(targetIp, message, 5001);
}

// Helper method to send UDP offline notification
private void sendUdpOfflineNotification(String friendIp, int offlineUserId) {
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
private static void updateUserOnlineStatus(int jicq, String ip, int status) {
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
                if (str == null) {
                // Client disconnected, break out of the loop
                System.out.println("Client disconnected or sent null");
                break;
}
                if (str.equals("end")) break; // If "end", close connection
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
                Server.onlineUsers = new HashMap<>();
            }
            Server.onlineUsers.put(userId, userIp);
            System.out.println("User " + userId + " is now online at IP: " + userIp);
            
            // Update database with online status - ALSO USE POSTGRESQL
            updateUserOnlineStatus(userId, userIp, 1); // 1 = online
            
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
else if (str.equals("addfriend")) {
    System.out.println("Processing friend request...");
    
    // Read friend JICQ (the one being added)
    String friendJicqStr = in.readLine();
    // Read my JICQ (the one making the request)
    String myJicqStr = in.readLine();
    
    System.out.println("User " + myJicqStr + " wants to add friend " + friendJicqStr);
    
    if (friendJicqStr == null || myJicqStr == null) {
        out.println("error");
        System.out.println("Error: Null values in friend request");
        return;
    }
    
    try {
        int friendJicq = Integer.parseInt(friendJicqStr);
        int myJicq = Integer.parseInt(myJicqStr);
        
        // Check if trying to add self
        if (friendJicq == myJicq) {
            out.println("cannot_add_self");
            System.out.println("Cannot add self as friend");
            return;
        }
        
        // Check if already friends
        if (areFriends(myJicq, friendJicq)) {
            out.println("already_friends");
            System.out.println("Users are already friends");
            return;
        }
        
        // Check if request already exists
        if (hasPendingRequest(myJicq, friendJicq)) {
            out.println("request_exists");
            System.out.println("Friend request already exists");
            return;
        }
        
        // Check if reverse request exists
        if (hasPendingRequest(friendJicq, myJicq)) {
            out.println("reverse_request_exists");
            System.out.println("Reverse friend request exists");
            return;
        }
        
        // Create the friend request in database
        if (createFriendRequest(myJicq, friendJicq)) {
            System.out.println("Friend request created from " + myJicq + " to " + friendJicq);
            
            // Get friend's information for notification
            String friendNickname = getUserName(friendJicq);
            String myNickname = getUserName(myJicq);
            
            System.out.println("Friend " + friendJicq + " (" + friendNickname + ") is online at IP " + getOnlineUserIp(friendJicq));
            
            // Check if friend is online and send UDP notification
            String friendIp = getOnlineUserIp(friendJicq);
            if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty()) {
                System.out.println("Should send UDP notification about friend request from " + myJicq + " (" + myNickname + ")");
                
                // Send UDP notification to the friend
                try {
                    String udpMessage = "oneaddyou" + myJicq;
                    byte[] udpData = udpMessage.getBytes();
                    
                    // Note: UDP port 5001 is where clients listen for notifications
                    // If your client uses a different port, adjust this
                    int udpPort = 5001; 
                    
                    DatagramSocket udpSocket = new DatagramSocket();
                    DatagramPacket packet = new DatagramPacket(udpData, udpData.length, 
                            InetAddress.getByName(friendIp), udpPort);
                    udpSocket.send(packet);
                    udpSocket.close();
                    
                    System.out.println("Sent UDP notification to " + friendIp + ":" + udpPort + " for request from " + myJicq);
                    
                    // Also update friend's online status in the database
                    updateUserOnlineStatus(myJicq, friendIp, 1); // 1 = online
                    
                } catch (Exception e) {
                    System.out.println("Failed to send UDP notification: " + e.getMessage());
                    // Don't fail the request if UDP fails
                }
            } else {
                System.out.println("Friend " + friendJicq + " is offline, notification stored for later");
            }
            
            out.println("request_sent");
            System.out.println("Successfully sent friend request from " + myJicq + " to " + friendJicq);
        } else {
            out.println("request_failed");
            System.out.println("Failed to create friend request in database");
        }
        
    } catch (NumberFormatException e) {
        out.println("error");
        System.out.println("Error parsing JICQ numbers: " + e.getMessage());
    } catch (Exception e) {
        out.println("error");
        System.out.println("Unexpected error in addfriend: " + e.getMessage());
        e.printStackTrace();
    }
    
    System.out.println("End addfriend processing");
}
                // Handle user adding a friend
                // Handle user sending a friend request
else if (str.equals("addfriend")) {
    System.out.println("Processing friend request...");
    try {
        Class.forName("org.postgresql.Driver");
        Connection c6 = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
        
        // Read parameters
        int friendicqno = Integer.parseInt(in.readLine());  // Friend to add
        int myicqno = Integer.parseInt(in.readLine());      // My JICQ
        
        System.out.println("User " + myicqno + " wants to add friend " + friendicqno);
        
        // Check if user exists
        String checkUserSql = "SELECT 1 FROM icq WHERE icqno = ?";
        PreparedStatement checkUserStmt = c6.prepareStatement(checkUserSql);
        checkUserStmt.setInt(1, friendicqno);
        ResultSet userCheck = checkUserStmt.executeQuery();
        
        if (!userCheck.next()) {
            out.println("user_not_found");
            System.out.println("Friend " + friendicqno + " not found");
            c6.close();
            return;
        }
        
        // Check if already friends (in either direction)
        String checkFriendSql = "SELECT 1 FROM friend WHERE (icqno = ? AND friend = ?) OR (icqno = ? AND friend = ?)";
        PreparedStatement checkFriendStmt = c6.prepareStatement(checkFriendSql);
        checkFriendStmt.setInt(1, myicqno);
        checkFriendStmt.setInt(2, friendicqno);
        checkFriendStmt.setInt(3, friendicqno);
        checkFriendStmt.setInt(4, myicqno);
        ResultSet friendCheck = checkFriendStmt.executeQuery();
        
        if (friendCheck.next()) {
            out.println("already_friends");
            System.out.println("Already friends: " + myicqno + " and " + friendicqno);
            c6.close();
            return;
        }
        
        // Check if pending request already exists
        String checkRequestSql = "SELECT 1 FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'";
        PreparedStatement checkRequestStmt = c6.prepareStatement(checkRequestSql);
        checkRequestStmt.setInt(1, myicqno);
        checkRequestStmt.setInt(2, friendicqno);
        ResultSet requestCheck = checkRequestStmt.executeQuery();
        
        if (requestCheck.next()) {
            out.println("request_exists");
            System.out.println("Friend request already exists from " + myicqno + " to " + friendicqno);
            c6.close();
            return;
        }
        
        // Check if there's a reverse request (friend wants to add me)
        String checkReverseSql = "SELECT 1 FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'";
        PreparedStatement checkReverseStmt = c6.prepareStatement(checkReverseSql);
        checkReverseStmt.setInt(1, friendicqno);
        checkReverseStmt.setInt(2, myicqno);
        ResultSet reverseCheck = checkReverseStmt.executeQuery();
        
        // Create friend request
        String insertRequestSql = "INSERT INTO friend_requests (requester_jicq, recipient_jicq, status) VALUES (?, ?, 'pending')";
        PreparedStatement insertRequestStmt = c6.prepareStatement(insertRequestSql);
        insertRequestStmt.setInt(1, myicqno);
        insertRequestStmt.setInt(2, friendicqno);
        int rowsInserted = insertRequestStmt.executeUpdate();
        
        if (rowsInserted == 1) {
            out.println("request_sent");
            System.out.println("Friend request created from " + myicqno + " to " + friendicqno);
            
            // Get friend's IP address to send UDP notification
            String getIpSql = "SELECT ip, status FROM icq WHERE icqno = ?";
            PreparedStatement getIpStmt = c6.prepareStatement(getIpSql);
            getIpStmt.setInt(1, friendicqno);
            ResultSet ipResult = getIpStmt.executeQuery();
            
            if (ipResult.next()) {
                String friendIp = ipResult.getString("ip");
                boolean friendStatus = ipResult.getBoolean("status");
                
                // Send UDP notification if friend is online
                if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty() && friendStatus) {
                    try {
                        // Get my nickname for the notification
                        String getMyNameSql = "SELECT nickname FROM icq WHERE icqno = ?";
                        PreparedStatement getNameStmt = c6.prepareStatement(getMyNameSql);
                        getNameStmt.setInt(1, myicqno);
                        ResultSet nameResult = getNameStmt.executeQuery();
                        
                        if (nameResult.next()) {
                            String myNickname = nameResult.getString("nickname");
                            
                            // Send UDP notification (this would be handled by a UDP server component)
                            // For now, we'll log it
                            System.out.println("Friend " + friendicqno + " is online at IP " + friendIp + 
                                             ". Should send UDP notification about friend request from " + myNickname);
                        }
                    } catch (Exception e) {
                        System.out.println("Error getting nickname: " + e.getMessage());
                    }
                }
            }
        } else {
            out.println("request_failed");
            System.out.println("Failed to create friend request");
        }
        
        c6.close();
    } catch (Exception e) {
        e.printStackTrace();
        out.println("error");
        System.out.println("Error in addfriend: " + e.getMessage());
    }
    System.out.println("End addfriend processing");
} // End addfriend (now friend request)

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
        updateUserOnlineStatus(userId, "null", 0); // 0 = offline
        
        // Also update the ip field to "null" in database
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DriverManager.getConnection(
            "jdbc:postgresql://localhost:5432/javaicq",
            "postgres",
            "admin"
        );
            String sql = "UPDATE icq SET ip = 'null', status = 0 WHERE jicqno = ?";
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
        notifyFriendsOffline(userId);
        
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

public class Server {
    
    public static Map<Integer, String> onlineUsers = new HashMap<>();
    // Static method to get online user IP
public static String getOnlineUserIp(int jicq) {
    if (Server.onlineUsers == null) {
        return null;
    }
    String ip = Server.onlineUsers.get(jicq);
    return ip; // Returns null if user not online
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
        // Bind to all network interfaces (0.0.0.0) instead of just localhost
        ServerSocket s = new ServerSocket(8080, 0, InetAddress.getByName("0.0.0.0"));
        System.out.println("Server started on all interfaces: " + s);
        try {
            while (true) {
                Socket socket = s.accept();//Continuously listen for client requests
                System.out.println("Connection accepted:" + socket);
                try {
                    new ServerThread(socket);//Create new thread
                } catch (IOException e) {
                    socket.close();
                }
            }
        } finally {
            s.close();
        }//Catch or finally
    }
}//End of server program