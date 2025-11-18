package com.messaging;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Vector;
import java.sql.SQLException;

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

    public void run() { // Thread listening function
        try {
            while (true) {
                String str = in.readLine(); // Get input string
                if (str.equals("end")) break; // If "end", close connection
                else if (str.equals("login")) { // If login request
                    try {
                        //Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); // Load database driver
                        //Connection c = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", ""); // Connect to database
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
                        
                        String sql = "select nickname,password from icq where icqno=?"; // Prepare to select nickname and password from database
                        PreparedStatement prepare = c.prepareCall(sql); // Set database query condition
                        String icqno = in.readLine();
                        int g = Integer.parseInt(icqno); // Get input JICQ number
                        System.out.println("The number you entered is: " + icqno);
                        String passwd = in.readLine().trim(); // Get input password
                        System.out.println("The login password you entered is: " + passwd);
                        prepare.clearParameters();
                        prepare.setInt(1, g); // Set parameter
                        ResultSet r = prepare.executeQuery(); // Execute database query
                        if (r.next()) { // Compare if input number and password match
                            String pass = r.getString("password").trim();
                            System.out.println("Login successful!");
                            if (passwd.regionMatches(0, pass, 0, pass.length())) {
                                out.println("ok"); // If match, tell client "ok"
                                // And update database user to online
                                // And register user's IP address
                                // Register IP address
                                String setip = "update icq set ip=? where icqno=?";
                                PreparedStatement prest = c.prepareCall(setip);
                                prest.clearParameters();
                                prest.setString(1, socket.getInetAddress().getHostAddress());
                                prest.setInt(2, g);
                                int set = prest.executeUpdate();
                                System.out.println("ip=" + socket.getInetAddress().getHostAddress() + " " + set);
                                // Set status online
                                String status = "update icq set status=1 where icqno=?";
                                PreparedStatement prest2 = c.prepareCall(status);
                                prest2.clearParameters();
                                prest2.setInt(1, g);
                                int set2 = prest2.executeUpdate();
                                System.out.println("status = 1 " + set2);
                            } else {
                                out.println("false"); // Otherwise tell client failure
                            }
                            r.close();
                            c.close();
                        } else {
                            out.println("false"); // Login failed
                            System.out.println("Login failed!");
                            r.close();
                            c.close();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    socket.close();
                } // End login

                // Handle client new user registration request
                else if (str.equals("new")) {
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver"); // Load database driver
                        // Connection c2 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", ""); // Connect to database
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c2 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
                        String newsql = "insert into icq(icqno,nickname,password,email,info,place,pic,sex) values(?,?,?,?,?,?,?,?)";
                        // Prepare to accept user's nickname, password, email, personal info, hometown, avatar, etc.
                        PreparedStatement prepare2 = c2.prepareCall(newsql);
                        int icqno = Integer.parseInt(in.readLine());
                        String nickname = in.readLine().trim();
                        String password = in.readLine().trim();
                        String email = in.readLine().trim();
                        String info = in.readLine().trim();
                        String place = in.readLine().trim();
                        int picindex = Integer.parseInt(in.readLine());
                        String sex = in.readLine().trim();
                        prepare2.clearParameters();
                        prepare2.setInt(1, icqno);
                        prepare2.setString(2, nickname);
                        prepare2.setString(3, password);
                        prepare2.setString(4, email);
                        prepare2.setString(5, info);
                        prepare2.setString(6, place);
                        prepare2.setInt(7, picindex);
                        prepare2.setString(8, sex);
                        int r3 = prepare2.executeUpdate(); // Execute database insert
                        String sql2 = "select icqno from icq where nickname=?";
                        // Tell client their registered number
                        PreparedStatement prepare3 = c2.prepareCall(sql2);
                        prepare3.clearParameters();
                        prepare3.setString(1, nickname);
                        ResultSet r2 = prepare3.executeQuery();
                        while (r2.next()) {
                            no = r2.getInt(1);
                            System.out.println(no);
                        }
                        out.println(no);
                        out.println("ok");
                        c2.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        out.println("false");
                    }
                    socket.close();
                } // End new user registration

                // Handle user search for friends
                else if (str.equals("find")) {
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c3 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c3 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
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
                            out.println(result.getString("status"));
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
                else if (str.equals("friend")) {
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c4 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c4 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
                        // Connect to friend table and return user's friend list
                        String friend = "select friend from friend where icqno=?";
                        PreparedStatement prepare4 = c4.prepareCall(friend);
                        prepare4.clearParameters();
                        int icqno = Integer.parseInt(in.readLine());
                        System.out.println(icqno);
                        prepare4.setInt(1, icqno);
                        ResultSet r4 = prepare4.executeQuery();
                        Vector friendno = new Vector(); // Vector to store friend numbers
                        while (r4.next()) {
                            friendno.add(new Integer(r4.getInt(1)));
                        }
                        // Read friend info
                        // Tell client their friends' nickname, number, IP, status, avatar, personal info, etc.
                        out.println(friendno.size());
                        for (int i = 0; i < friendno.size(); i++) {
                            String friendinfo = "select nickname,icqno,ip,status,pic,email,sex,info from icq where icqno=?";
                            PreparedStatement prepare5 = c4.prepareCall(friendinfo);
                            prepare5.clearParameters();
                            prepare5.setObject(1, friendno.get(i));
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
                            r5.close();
                        } // End for
                        // Send complete
                        out.println("over");
                        System.out.println("Successfully read friend information!");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("Failed to read friend information!");
                    }
                } // End read friend info

                // Handle user adding a friend
                else if (str.equals("addfriend")) {
                    System.out.println("add");
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c6 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c6 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
                        // Connect to database, add record to friend table based on user and friend numbers
                        int friendicqno = Integer.parseInt(in.readLine());
                        int myicqno = Integer.parseInt(in.readLine());
                        String getwhoaddme = "select friend from friend where icqno=?";
                        PreparedStatement prepare6 = c6.prepareCall(getwhoaddme);
                        prepare6.clearParameters();
                        prepare6.setInt(1, myicqno);
                        ResultSet r6 = prepare6.executeQuery();
                        Vector who = new Vector();
                        while (r6.next()) {
                            who.add(new Integer(r6.getInt(1)));
                        }
                        int s = 0;
                        for (int index = 0; index < who.size(); index++) {
                            if (friendicqno == Integer.parseInt(who.get(index).toString())) {
                                out.println("same");
                                s = 1;
                                break;
                            }
                        }
                        if (s == 0) {
                            out.println("add");
                            String addfriend = "insert into friend values(?,?)";
                            PreparedStatement prepare7 = c6.prepareCall(addfriend);
                            prepare7.clearParameters();
                            prepare7.setInt(1, myicqno);
                            prepare7.setInt(2, friendicqno);
                            int r7 = prepare7.executeUpdate();
                            if (r7 == 1) System.out.println("ok addfriend");
                            else System.out.println("false addfriend");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("false");
                    }
                    System.out.println("over addfriend");
                } // End addfriend

                // Handle if another user adds me, I add them back
                else if (str.equals("addnewfriend")) {
                    System.out.println("add");
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c6 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c6 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
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
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c7 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c7 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
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
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c8 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c8 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
                        // Connect to database, set status to 0 and clear IP for the user
                        int myicqno = Integer.parseInt(in.readLine());
                        String status = "update icq set status=0 , ip=' ' where icqno=?";
                        PreparedStatement prest8 = c8.prepareCall(status);
                        prest8.clearParameters();
                        prest8.setInt(1, myicqno);
                        int r8 = prest8.executeUpdate();
                        if (r8 == 1) System.out.println(myicqno + " logged off. Server...");
                        else System.out.println("false logout");
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("logout false");
                    }
                } // End logout handling

                // Handle who added me as friend so I can notify them when I come online
                else if (str.equals("getwhoaddme")) {
                    try {
                        // Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
                        // Connection c9 = DriverManager.getConnection("jdbc:odbc:javaicq", "sa", "");
                        Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        Connection c9 = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
                        // Connect to database, find who added me based on my number
                        int myicqno = Integer.parseInt(in.readLine());
                        System.out.println(myicqno + " is online......");
                        String getwhoaddme = "select friend from friend where icqno=?";
                        PreparedStatement prepare6 = c9.prepareCall(getwhoaddme);
                        prepare6.clearParameters();
                        prepare6.setInt(1, myicqno);
                        ResultSet r6 = prepare6.executeQuery();
                        Vector who = new Vector();
                        while (r6.next()) {
                            who.add(new Integer(r6.getInt(1)));
                        } // End while
                        // Then send these friends' IP addresses to user so they know I'm online
                        for (int i = 0; i < who.size(); i++) {
                            String whoinfo = "select ip from icq where icqno=? ";
                            PreparedStatement prepare = c9.prepareCall(whoinfo);
                            prepare.clearParameters();
                            prepare.setObject(1, who.get(i));
                            ResultSet r = prepare.executeQuery();
                            while (r.next()) {
                                out.println(r.getString("ip"));
                            }
                            r.close();
                        } // End for
                        out.println("over");
                        c9.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.out.println("false");
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
    
    // Database initialization method
    private static void initializeDatabase() {
        Connection conn = null;
        Statement stmt = null;
        
        try {
            // First connect without database name to create the database
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
            conn = DriverManager.getConnection("jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true");
            stmt = conn.createStatement();
            
            // Create database if it doesn't exist
            String createDBSQL = "IF NOT EXISTS(SELECT name FROM master.dbo.sysdatabases WHERE name = 'javaicq') CREATE DATABASE javaicq";
            stmt.executeUpdate(createDBSQL);
            System.out.println("Database checked/created successfully");
            
            // Close connection and reconnect to the new database
            stmt.close();
            conn.close();
            
            // Now connect to the javaicq database
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                        // Use Windows Authentication
                        // Remove username and password, use integratedSecurity=true
                        conn = DriverManager.getConnection(
                            "jdbc:sqlserver://localhost:1433;databaseName=javaicq;trustServerCertificate=true;encrypt=false;integratedSecurity=true"
                        );
            
            stmt = conn.createStatement();
            
            // Create icq table
            String createIcqTable = 
                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='icq' AND xtype='U') " +
                "CREATE TABLE icq (" +
                "    icqno INT PRIMARY KEY," +
                "    nickname VARCHAR(50)," +
                "    password VARCHAR(50)," +
                "    email VARCHAR(100)," +
                "    info VARCHAR(255)," +
                "    place VARCHAR(100)," +
                "    pic INT," +
                "    sex VARCHAR(10)," +
                "    ip VARCHAR(50)," +
                "    status BIT DEFAULT 0" +
                ")";
            stmt.executeUpdate(createIcqTable);
            System.out.println("icq table checked/created successfully");
            
            // Create friend table
            String createFriendTable = 
                "IF NOT EXISTS (SELECT * FROM sysobjects WHERE name='friend' AND xtype='U') " +
                "CREATE TABLE friend (" +
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
    

	public static void main(String args[]) throws IOException {
        initializeDatabase();
		ServerSocket s = new ServerSocket(8080);//Create socket on port 8080
		System.out.println("Server start.." + s);
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