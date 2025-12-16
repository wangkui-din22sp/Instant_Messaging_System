package com;

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
import java.sql.SQLException;
import java.sql.Statement;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.Thread;
import java.net.SocketException;
import java.util.concurrent.atomic.AtomicInteger;

// ============== Database Manager ==============
class DatabaseManager {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/javaicq";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "admin";
    
    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("PostgreSQL Driver not found: " + e.getMessage());
        }
    }
    
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }
    
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    // Ignore close errors
                }
            }
        }
    }
}

// ============== User Session ==============
class UserSession {
    String ip;
    int udpPort;
    Socket tcpSocket;
    long lastActive;
    String nickname;
    
    public UserSession(String ip, int udpPort, Socket tcpSocket, String nickname) {
        this.ip = ip;
        this.udpPort = udpPort;
        this.tcpSocket = tcpSocket;
        this.nickname = nickname;
        this.lastActive = System.currentTimeMillis();
    }
    
    public void updateActivity() {
        this.lastActive = System.currentTimeMillis();
    }
}

// ============== Notification Service ==============
class NotificationService {
    private static final int UDP_PORT = 5001;
    
    public static void sendUdpNotification(String targetIp, String message) {
        sendUdpNotification(targetIp, UDP_PORT, message);
    }
    
    public static void sendUdpNotification(String targetIp, int port, String message) {
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
            System.out.println("Failed to send UDP notification to " + targetIp + 
                             ": " + e.getMessage());
        }
    }
    
    public static void notifyFriendsOnline(int userId) {
        System.out.println("Notifying friends that user " + userId + " is online");
        
        String userNickname = UserManager.getUserNickname(userId);
        String userIp = UserManager.getOnlineUserIp(userId);
        
        if (userIp == null) return;
        
        for (Integer friendId : UserManager.getFriendIds(userId)) {
            String friendIp = UserManager.getOnlineUserIp(friendId);
            if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty()) {
                // Send online notification
                String message = "online:" + userId + ":" + userNickname;
                sendUdpNotification(friendIp, message);
                
                // Also send my current status to friend
                String statusMessage = "friend_online:" + userId + ":1"; // 1 = online
                sendUdpNotification(friendIp, statusMessage);
                
                System.out.println("Notified friend " + friendId + " at " + friendIp + 
                                 " about user " + userId + " online");
            }
        }
    }
    
    public static void notifyFriendsOffline(int userId) {
        System.out.println("Notifying friends that user " + userId + " is offline");
        
        for (Integer friendId : UserManager.getFriendIds(userId)) {
            String friendIp = UserManager.getOnlineUserIp(friendId);
            if (friendIp != null && !friendIp.equals("null") && !friendIp.isEmpty()) {
                String message = "offline:" + userId;
                sendUdpNotification(friendIp, message);
                
                // Update friend's UI
                String statusMessage = "friend_offline:" + userId + ":0"; // 0 = offline
                sendUdpNotification(friendIp, statusMessage);
                
                System.out.println("Notified friend " + friendId + " at " + friendIp + 
                                 " about user " + userId + " offline");
            }
        }
    }
}

// ============== User Manager ==============
class UserManager {
    private static final Map<Integer, UserSession> onlineUsers = new ConcurrentHashMap<>();
    private static final AtomicInteger activeConnections = new AtomicInteger(0);
    
    public static synchronized void userLogin(int userId, String ip, Socket socket, String nickname) {
        // Get existing session or create new
        UserSession session = new UserSession(ip, 5001, socket, nickname);
        onlineUsers.put(userId, session);
        activeConnections.incrementAndGet();
        
        // Update database
        updateUserOnlineStatus(userId, ip, true);
        
        System.out.println("User " + userId + " (" + nickname + ") logged in from " + ip);
        System.out.println("Active connections: " + activeConnections.get());
    }
    
    public static synchronized void userLogout(int userId) {
        UserSession session = onlineUsers.remove(userId);
        if (session != null) {
            activeConnections.decrementAndGet();
            updateUserOnlineStatus(userId, "null", false);
            System.out.println("User " + userId + " logged out");
            System.out.println("Active connections: " + activeConnections.get());
        }
    }
    
    public static void updateUserOnlineStatus(int userId, String ip, boolean online) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE icq SET ip = ?, status = ?, last_seen = CURRENT_TIMESTAMP WHERE icqno = ?")) {
            
            pstmt.setString(1, online ? ip : "null");
            pstmt.setBoolean(2, online);
            pstmt.setInt(3, userId);
            pstmt.executeUpdate();
            
            System.out.println("Updated user " + userId + " status to " + 
                             (online ? "online" : "offline"));
            
        } catch (SQLException e) {
            System.out.println("Error updating user status: " + e.getMessage());
        }
    }
    
    public static String getOnlineUserIp(int userId) {
        UserSession session = onlineUsers.get(userId);
        return session != null ? session.ip : null;
    }
    
    public static UserSession getOnlineUserSession(int userId) {
        return onlineUsers.get(userId);
    }
    
    public static String getUserNickname(int userId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT nickname FROM icq WHERE icqno = ?")) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("nickname");
            }
        } catch (SQLException e) {
            System.out.println("Error getting user nickname: " + e.getMessage());
        }
        return "Unknown";
    }
    
    public static java.util.Set<Integer> getFriendIds(int userId) {
        java.util.Set<Integer> friendIds = new java.util.HashSet<>();
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT friend FROM friend WHERE icqno = ? " +
                 "UNION " +
                 "SELECT icqno FROM friend WHERE friend = ?")) {
            
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                friendIds.add(rs.getInt(1));
            }
            
        } catch (SQLException e) {
            System.out.println("Error getting friend IDs: " + e.getMessage());
        }
        
        return friendIds;
    }
    
    public static boolean isUserOnline(int userId) {
        return onlineUsers.containsKey(userId);
    }
    
    public static int getOnlineUserCount() {
        return activeConnections.get();
    }
}

// ============== Server Thread ==============
class ServerThread extends Thread {
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int userId;
    private boolean authenticated = false;
    
    public ServerThread(Socket s) throws IOException {
        socket = s;
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
        start();
    }
    
    private void sendPendingFriendRequests(int userId, String userIp) {
        System.out.println("Checking pending friend requests for user: " + userId);
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT fr.requester_jicq, i.nickname " +
                 "FROM friend_requests fr " +
                 "JOIN icq i ON fr.requester_jicq = i.icqno " +
                 "WHERE fr.recipient_jicq = ? AND fr.status = 'pending'")) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            int pendingCount = 0;
            while (rs.next()) {
                pendingCount++;
                int requesterId = rs.getInt("requester_jicq");
                String requesterName = rs.getString("nickname");
                
                System.out.println("Found pending request from: " + requesterId + " (" + requesterName + ")");
                
                // Send UDP notification for each pending request
                String udpMessage = "friend_request:" + requesterId + ":" + requesterName;
                NotificationService.sendUdpNotification(userIp, udpMessage);
            }
            
            if (pendingCount > 0) {
                System.out.println("Sent " + pendingCount + " pending friend request notifications");
            }
            
        } catch (SQLException e) {
            System.out.println("Error checking pending requests: " + e.getMessage());
        }
    }
    
    private boolean authenticateUser(int userId, String password) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT * FROM icq WHERE icqno = ? AND password = ?")) {
            
            pstmt.setInt(1, userId);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            
            return rs.next();
            
        } catch (SQLException e) {
            System.out.println("Authentication error: " + e.getMessage());
            return false;
        }
    }
    
    private String registerUser(String[] userData) {
        if (userData.length < 8) return "error:invalid_data";
        
        try (Connection conn = DatabaseManager.getConnection()) {
            int icqno = Integer.parseInt(userData[0]);
            String nickname = userData[1].trim();
            String password = userData[2].trim();
            String email = userData[3].trim();
            String info = userData[4].trim();
            String place = userData[5].trim();
            int picindex = Integer.parseInt(userData[6]);
            String sex = userData[7].trim();
            String clientIP = socket.getInetAddress().getHostAddress();
            
            // Check if user already exists
            try (PreparedStatement checkStmt = conn.prepareStatement(
                 "SELECT 1 FROM icq WHERE icqno = ?")) {
                checkStmt.setInt(1, icqno);
                if (checkStmt.executeQuery().next()) {
                    return "error:user_exists";
                }
            }
            
            // Insert new user
            try (PreparedStatement insertStmt = conn.prepareStatement(
                 "INSERT INTO icq(icqno,nickname,password,email,info,place,pic,sex,ip) " +
                 "VALUES(?,?,?,?,?,?,?,?,?)")) {
                
                insertStmt.setInt(1, icqno);
                insertStmt.setString(2, nickname);
                insertStmt.setString(3, password);
                insertStmt.setString(4, email);
                insertStmt.setString(5, info);
                insertStmt.setString(6, place);
                insertStmt.setInt(7, picindex);
                insertStmt.setString(8, sex);
                insertStmt.setString(9, clientIP);
                
                int rows = insertStmt.executeUpdate();
                if (rows == 1) {
                    return "success:" + icqno;
                }
            }
            
        } catch (Exception e) {
            System.out.println("Registration error: " + e.getMessage());
            e.printStackTrace();
        }
        
        return "error:registration_failed";
    }
    
    private void handleFriendRequest(int requesterId, int targetId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Check if already friends
            try (PreparedStatement checkStmt = conn.prepareStatement(
                 "SELECT 1 FROM friend WHERE (icqno = ? AND friend = ?) OR (icqno = ? AND friend = ?)")) {
                checkStmt.setInt(1, requesterId);
                checkStmt.setInt(2, targetId);
                checkStmt.setInt(3, targetId);
                checkStmt.setInt(4, requesterId);
                
                if (checkStmt.executeQuery().next()) {
                    out.println("already_friends");
                    return;
                }
            }
            
            // Check if pending request exists
            try (PreparedStatement checkReqStmt = conn.prepareStatement(
                 "SELECT 1 FROM friend_requests WHERE requester_jicq = ? AND recipient_jicq = ? AND status = 'pending'")) {
                checkReqStmt.setInt(1, requesterId);
                checkReqStmt.setInt(2, targetId);
                
                if (checkReqStmt.executeQuery().next()) {
                    out.println("request_exists");
                    return;
                }
            }
            
            // Create friend request
            try (PreparedStatement insertStmt = conn.prepareStatement(
                 "INSERT INTO friend_requests(requester_jicq, recipient_jicq, status) VALUES(?,?, 'pending')")) {
                insertStmt.setInt(1, requesterId);
                insertStmt.setInt(2, targetId);
                
                if (insertStmt.executeUpdate() > 0) {
                    out.println("request_sent");
                    
                    // Notify target user if online
                    String targetIp = UserManager.getOnlineUserIp(targetId);
                    if (targetIp != null) {
                        String requesterName = UserManager.getUserNickname(requesterId);
                        String message = "friend_request:" + requesterId + ":" + requesterName;
                        NotificationService.sendUdpNotification(targetIp, message);
                    }
                } else {
                    out.println("request_failed");
                }
            }
            
        } catch (SQLException e) {
            out.println("error");
            System.out.println("Friend request error: " + e.getMessage());
        }
    }
    
    private void handleAcceptFriendRequest(int requesterId, int acceptorId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            conn.setAutoCommit(false);
            
            try {
                // Update request status
                try (PreparedStatement updateStmt = conn.prepareStatement(
                     "UPDATE friend_requests SET status = 'accepted' " +
                     "WHERE requester_jicq = ? AND recipient_jicq = ?")) {
                    updateStmt.setInt(1, requesterId);
                    updateStmt.setInt(2, acceptorId);
                    updateStmt.executeUpdate();
                }
                
                // Add friendship in both directions
                try (PreparedStatement insertStmt1 = conn.prepareStatement(
                     "INSERT INTO friend(icqno, friend) VALUES(?, ?)")) {
                    insertStmt1.setInt(1, acceptorId);
                    insertStmt1.setInt(2, requesterId);
                    insertStmt1.executeUpdate();
                }
                
                try (PreparedStatement insertStmt2 = conn.prepareStatement(
                     "INSERT INTO friend(icqno, friend) VALUES(?, ?)")) {
                    insertStmt2.setInt(1, requesterId);
                    insertStmt2.setInt(2, acceptorId);
                    insertStmt2.executeUpdate();
                }
                
                conn.commit();
                out.println("accepted");
                
                // Send friend info
                sendFriendInfo(requesterId);
                
                // Notify requester
                String requesterIp = UserManager.getOnlineUserIp(requesterId);
                if (requesterIp != null) {
                    String acceptorName = UserManager.getUserNickname(acceptorId);
                    String message = "friend_accepted:" + acceptorId + ":" + acceptorName;
                    NotificationService.sendUdpNotification(requesterIp, message);
                }
                
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
            
        } catch (SQLException e) {
            out.println("error");
            System.out.println("Accept friend error: " + e.getMessage());
        }
    }
    
    private void sendFriendInfo(int friendId) {
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT nickname, icqno, ip, status, pic, email, sex, info FROM icq WHERE icqno = ?")) {
            
            pstmt.setInt(1, friendId);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                out.println(rs.getString("nickname"));
                out.println(rs.getInt("icqno"));
                out.println(rs.getString("ip"));
                out.println(rs.getBoolean("status") ? "1" : "0");
                out.println(rs.getInt("pic"));
                out.println(rs.getString("email"));
                out.println(rs.getString("sex"));
                out.println(rs.getString("info"));
            }
            
        } catch (SQLException e) {
            System.out.println("Error sending friend info: " + e.getMessage());
        }
    }
    
    private void handleFindUsers() {
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet result = stmt.executeQuery(
                 "SELECT nickname, sex, place, ip, email, info, status FROM icq")) {
            
            while (result.next()) {
                out.println(result.getString("nickname"));
                out.println(result.getString("sex"));
                out.println(result.getString("place"));
                out.println(result.getString("ip"));
                out.println(result.getString("email"));
                out.println(result.getString("info"));
                out.println(result.getBoolean("status") ? "1" : "0");
            }
            
            out.println("over");
            
            // Send JICQ numbers and avatars
            try (ResultSet iset = stmt.executeQuery("SELECT icqno, pic, status FROM icq")) {
                while (iset.next()) {
                    out.println(iset.getInt("icqno"));
                    out.println(iset.getInt("pic"));
                    out.println(iset.getBoolean("status") ? "1" : "0");
                }
            }
            
        } catch (SQLException e) {
            System.out.println("Error finding users: " + e.getMessage());
            out.println("error");
        }
    }
    
    private void handleDeleteFriend(int friendId, int userId) {
        try (Connection conn = DatabaseManager.getConnection()) {
            // Delete friendship in both directions
            try (PreparedStatement pstmt1 = conn.prepareStatement(
                 "DELETE FROM friend WHERE icqno = ? AND friend = ?")) {
                pstmt1.setInt(1, userId);
                pstmt1.setInt(2, friendId);
                pstmt1.executeUpdate();
            }
            
            try (PreparedStatement pstmt2 = conn.prepareStatement(
                 "DELETE FROM friend WHERE icqno = ? AND friend = ?")) {
                pstmt2.setInt(1, friendId);
                pstmt2.setInt(2, userId);
                pstmt2.executeUpdate();
            }
            
            out.println("deleted");
            
            // Notify friend if online
            String friendIp = UserManager.getOnlineUserIp(friendId);
            if (friendIp != null) {
                String message = "friend_removed:" + userId;
                NotificationService.sendUdpNotification(friendIp, message);
            }
            
        } catch (SQLException e) {
            out.println("error");
            System.out.println("Delete friend error: " + e.getMessage());
        }
    }
    
    public void run() {
        try {
            while (true) {
                String command = in.readLine();
                if (command == null || command.equals("end")) {
                    break;
                }
                
                System.out.println("Received command: " + command);
                
                switch (command) {
                    case "login":
                        handleLogin();
                        break;
                        
                    case "new":
                        handleRegistration();
                        break;
                        
                    case "find":
                        handleFindUsers();
                        break;
                        
                    case "addfriend":
                        handleAddFriend();
                        break;
                        
                    case "acceptfriendrequest":
                        handleAcceptRequest();
                        break;
                        
                    case "rejectfriendrequest":
                        handleRejectRequest();
                        break;
                        
                    case "getpendingrequests":
                        handleGetPendingRequests();
                        break;
                        
                    case "delfriend":
                        handleDeleteFriend();
                        break;
                        
                    case "logout":
                        handleLogout();
                        break;
                        
                    case "getwhoaddme":
                        handleGetWhoAddedMe();
                        break;
                        
                    case "setudpport":
                        handleSetUdpPort();
                        break;
                        
                    default:
                        out.println("unknown_command");
                        break;
                }
            }
            
        } catch (IOException e) {
            System.out.println("Client disconnected: " + e.getMessage());
        } finally {
            // Ensure user is logged out if connection closes
            if (authenticated) {
                UserManager.userLogout(userId);
                NotificationService.notifyFriendsOffline(userId);
            }
            
            try {
                socket.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }
    
    private void handleLogin() throws IOException {
        String jicqStr = in.readLine();
        String password = in.readLine();
        
        if (jicqStr == null || password == null) {
            out.println("false");
            return;
        }
        
        try {
            int userId = Integer.parseInt(jicqStr);
            
            if (authenticateUser(userId, password)) {
                this.userId = userId;
                this.authenticated = true;
                
                String userIp = socket.getInetAddress().getHostAddress();
                String nickname = UserManager.getUserNickname(userId);
                
                UserManager.userLogin(userId, userIp, socket, nickname);
                NotificationService.notifyFriendsOnline(userId);
                sendPendingFriendRequests(userId, userIp);
                
                out.println("ok");
            } else {
                out.println("false");
            }
            
        } catch (NumberFormatException e) {
            out.println("false");
        }
    }
    
    private void handleRegistration() throws IOException {
        String[] userData = new String[8];
        for (int i = 0; i < 8; i++) {
            userData[i] = in.readLine();
            if (userData[i] == null) {
                out.println("error:missing_data");
                return;
            }
        }
        
        String result = registerUser(userData);
        out.println(result);
    }
    
    private void handleAddFriend() throws IOException {
        int friendId = Integer.parseInt(in.readLine());
        int userId = Integer.parseInt(in.readLine());
        handleFriendRequest(userId, friendId);
    }
    
    private void handleAcceptRequest() throws IOException {
        int requesterId = Integer.parseInt(in.readLine());
        int acceptorId = Integer.parseInt(in.readLine());
        handleAcceptFriendRequest(requesterId, acceptorId);
    }
    
    private void handleRejectRequest() throws IOException {
        int requesterId = Integer.parseInt(in.readLine());
        int rejectorId = Integer.parseInt(in.readLine());
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "UPDATE friend_requests SET status = 'rejected' " +
                 "WHERE requester_jicq = ? AND recipient_jicq = ?")) {
            
            pstmt.setInt(1, requesterId);
            pstmt.setInt(2, rejectorId);
            pstmt.executeUpdate();
            out.println("rejected");
            
        } catch (SQLException e) {
            out.println("error");
        }
    }
    
    private void handleGetPendingRequests() throws IOException {
        int userId = Integer.parseInt(in.readLine());
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT r.requester_jicq, i.nickname " +
                 "FROM friend_requests r " +
                 "JOIN icq i ON r.requester_jicq = i.icqno " +
                 "WHERE r.recipient_jicq = ? AND r.status = 'pending'")) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                out.println(rs.getInt("requester_jicq") + ":" + rs.getString("nickname"));
            }
            out.println("over");
            
        } catch (SQLException e) {
            out.println("error");
        }
    }
    
    private void handleDeleteFriend() throws IOException {
        int friendId = Integer.parseInt(in.readLine());
        int userId = Integer.parseInt(in.readLine());
        handleDeleteFriend(friendId, userId);
    }
    
    private void handleLogout() throws IOException {
        String userIdStr = in.readLine();
        if (userIdStr == null) {
            out.println("error");
            return;
        }
        
        try {
            int userId = Integer.parseInt(userIdStr);
            UserManager.userLogout(userId);
            NotificationService.notifyFriendsOffline(userId);
            authenticated = false;
            out.println("ok");
            
        } catch (NumberFormatException e) {
            out.println("error");
        }
    }
    
    private void handleGetWhoAddedMe() throws IOException {
        int userId = Integer.parseInt(in.readLine());
        
        // Get friends and pending requesters who are currently online
        java.util.Set<Integer> onlineFriendIds = new java.util.HashSet<>();
        
        // Add friends
        onlineFriendIds.addAll(UserManager.getFriendIds(userId));
        
        // Add pending requesters
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                 "SELECT requester_jicq FROM friend_requests " +
                 "WHERE recipient_jicq = ? AND status = 'pending'")) {
            
            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                onlineFriendIds.add(rs.getInt("requester_jicq"));
            }
            
        } catch (SQLException e) {
            System.out.println("Error getting who added me: " + e.getMessage());
        }
        
        // Send IPs of online users
        for (int friendId : onlineFriendIds) {
            String ip = UserManager.getOnlineUserIp(friendId);
            if (ip != null && !ip.equals("null") && !ip.isEmpty()) {
                out.println(ip);
            }
        }
        out.println("over");
    }
    
    private void handleSetUdpPort() throws IOException {
        int userId = Integer.parseInt(in.readLine());
        String udpPortStr = in.readLine();
        System.out.println("User " + userId + " UDP port: " + udpPortStr);
        out.println("ok");
    }
}

// ============== UDP Server Thread ==============
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
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                
                String message = new String(packet.getData(), 0, packet.getLength());
                String senderIp = packet.getAddress().getHostAddress();
                
                System.out.println("UDP received from " + senderIp + ": " + message);
                
                // Parse message type
                if (message.startsWith("relay:")) {
                    handleRelayMessage(message);
                } else if (message.startsWith("filerequest:")) {
                    handleFileRequest(message);
                } else if (message.startsWith("ping:")) {
                    handlePing(message, senderIp);
                }
                
            } catch (IOException e) {
                if (running) {
                    System.out.println("UDP Server error: " + e.getMessage());
                }
            }
        }
        
        socket.close();
    }
    
    private void handleRelayMessage(String message) {
        // Format: relay:targetUserId:actualMessage
        String[] parts = message.split(":", 3);
        if (parts.length == 3) {
            try {
                int targetUserId = Integer.parseInt(parts[1]);
                String actualMessage = parts[2];
                
                String targetIp = UserManager.getOnlineUserIp(targetUserId);
                if (targetIp != null) {
                    NotificationService.sendUdpNotification(targetIp, actualMessage);
                    System.out.println("Relayed message to user " + targetUserId);
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid target user ID format");
            }
        }
    }
    
    private void handleFileRequest(String message) {
        // Format: filerequest:targetUserId:filename:filesize
        String[] parts = message.split(":", 4);
        if (parts.length >= 3) {
            try {
                int targetUserId = Integer.parseInt(parts[1]);
                String filename = parts[2];
                String filesize = parts.length > 3 ? parts[3] : "unknown";
                
                String targetIp = UserManager.getOnlineUserIp(targetUserId);
                if (targetIp != null) {
                    String forwardMessage = "filerequest:" + filename + ":" + filesize;
                    NotificationService.sendUdpNotification(targetIp, forwardMessage);
                    System.out.println("Forwarded file request to user " + targetUserId);
                }
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid target user ID format in file request");
            }
        }
    }
    
    private void handlePing(String message, String senderIp) {
        // Format: ping:userId
        String[] parts = message.split(":");
        if (parts.length == 2) {
            try {
                int userId = Integer.parseInt(parts[1]);
                // Update user's last activity
                UserSession session = UserManager.getOnlineUserSession(userId);
                if (session != null) {
                    session.updateActivity();
                }
                // Send pong response
                String pongMessage = "pong:" + userId;
                NotificationService.sendUdpNotification(senderIp, pongMessage);
                
            } catch (NumberFormatException e) {
                System.out.println("Invalid user ID in ping");
            }
        }
    }
    
    public void stopServer() {
        running = false;
        socket.close();
    }
}

// ============== Main Server Class ==============
public class Server {
    private static UdpServerThread udpServer;
    
    private static void initializeDatabase() {
        try (Connection conn = DriverManager.getConnection(
                "jdbc:postgresql://localhost:5432/postgres",
                "postgres",
                "admin")) {
            
            // Check if database exists
            try (Statement stmt = conn.createStatement()) {
                ResultSet rs = stmt.executeQuery(
                    "SELECT 1 FROM pg_database WHERE datname = 'javaicq'");
                
                if (!rs.next()) {
                    stmt.executeUpdate("CREATE DATABASE javaicq");
                    System.out.println("Database created");
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Database initialization failed: " + e.getMessage());
            return;
        }
        
        // Create tables in javaicq database
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create icq table
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
                "    status BOOLEAN DEFAULT false," +
                "    last_seen TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
            stmt.executeUpdate(createIcqTable);
            
            // Create friend table
            String createFriendTable = 
                "CREATE TABLE IF NOT EXISTS friend (" +
                "    icqno INT," +
                "    friend INT," +
                "    PRIMARY KEY (icqno, friend)" +
                ")";
            stmt.executeUpdate(createFriendTable);
            
            // Create friend_requests table
            String createFriendRequestsTable = 
                "CREATE TABLE IF NOT EXISTS friend_requests (" +
                "    request_id SERIAL PRIMARY KEY," +
                "    requester_jicq INT NOT NULL," +
                "    recipient_jicq INT NOT NULL," +
                "    status VARCHAR(10) DEFAULT 'pending'," +
                "    request_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
                "    UNIQUE (requester_jicq, recipient_jicq, status)" +
                ")";
            stmt.executeUpdate(createFriendRequestsTable);
            
            System.out.println("Database initialization completed");
            
        } catch (SQLException e) {
            System.err.println("Table creation failed: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) throws IOException {
        System.out.println("Starting JICQ Server...");
        
        // Initialize database
        initializeDatabase();
        
        // Start UDP server
        try {
            udpServer = new UdpServerThread();
            udpServer.start();
            System.out.println("UDP server started on port 5001");
        } catch (SocketException e) {
            System.err.println("Failed to start UDP server: " + e.getMessage());
        }
        
        // Start TCP server
        try (ServerSocket serverSocket = new ServerSocket(8080, 0, 
                InetAddress.getByName("0.0.0.0"))) {
            
            System.out.println("TCP server started on port 8080");
            System.out.println("Waiting for connections...");
            
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New connection from: " + 
                    clientSocket.getInetAddress().getHostAddress());
                
                try {
                    new ServerThread(clientSocket);
                } catch (IOException e) {
                    System.err.println("Failed to create server thread: " + e.getMessage());
                    clientSocket.close();
                }
            }
            
        } finally {
            if (udpServer != null) {
                udpServer.stopServer();
            }
            System.out.println("Server shutdown");
        }
    }
}