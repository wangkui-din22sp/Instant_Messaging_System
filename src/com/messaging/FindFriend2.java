package com.messaging;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Vector;
import java.net.UnknownHostException;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;

public class FindFriend2 extends JFrame {//
    JLabel jLabel1 = new JLabel();

    JButton find2 = new JButton();

    JButton jButton1 = new JButton();

    JButton jButton2 = new JButton();

    JButton jButton3 = new JButton();

    JList list2;

    ////////////////// /
    Vector nickname = new Vector();

    Vector sex = new Vector();

    Vector place = new Vector();

    Vector jicq = new Vector();

    Vector ip = new Vector();

    Vector pic = new Vector();

    Vector status = new Vector();

    Vector emails = new Vector();

    Vector infos = new Vector();

    // 
    Vector tmpjicq = new Vector();//jicqid

    Vector tmpname = new Vector();//jicqname

    Vector tmpip = new Vector();//ip

    Vector tmppic = new Vector();//pic info

    Vector tmpstatus = new Vector();//status

    Vector tmpemail = new Vector();

    Vector tmpinfo = new Vector();

    // 
    Socket socket;

    BufferedReader in;

    PrintWriter out;

    int myid;

    String serverhost;

    int servport;

    DatagramPacket sendPacket;

    DatagramSocket sendSocket;

    int sendPort = 5001;

    ////////////////// //
    JPopupMenu findmenu = new JPopupMenu();

    JMenuItem look = new JMenuItem();

    JMenuItem add = new JMenuItem();

public FindFriend2(int whoami, String host, int port) {
    enableEvents(AWTEvent.WINDOW_EVENT_MASK);
    try {
        serverhost = host;
        servport = port;
        myid = whoami;
        jbInit();
    } catch (Exception e) {
        e.printStackTrace();
    }
    try {
        socket = new Socket(InetAddress.getByName(serverhost), servport);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                socket.getOutputStream())), true);
        sendSocket = new DatagramSocket(); // Use ephemeral port for sending
        
        // Optional: Check for pending friend requests when window opens
        // Uncomment if you want this feature
        // checkPendingRequests();
    } catch (IOException e1) {
        JOptionPane.showMessageDialog(this, "无法连接到服务器: " + e1.getMessage(), 
                "连接错误", JOptionPane.ERROR_MESSAGE);
        e1.printStackTrace();
    }
}

    private void jbInit() throws Exception {//
        jLabel1.setText("List all registered users");
        jLabel1.setBounds(new Rectangle(11, 11, 211, 18));
        this.getContentPane().setLayout(new FlowLayout());
        this.setTitle("Find Friends");
        find2.setText("List all users");
        find2.setBounds(new Rectangle(8, 289, 79, 29));
        find2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                find2_mouseClicked(e);
            }
        });

        jButton1.setBounds(new Rectangle(110, 288, 79, 29));

        jButton2.setBounds(new Rectangle(211, 285, 79, 29));
        jButton3.setText("cancel");
        jButton3.setBounds(new Rectangle(317, 289, 79, 29));
        nickname = new Vector();
        sex = new Vector();
        place = new Vector();
        status = new Vector();
        ListModel model = new FindListModel(nickname, sex, place, status);//
        ListCellRenderer renderer = new FindListCellRenderer();
        list2 = new JList(model);
        list2.setSize(200, 200);
        list2.setBackground(new Color(255, 255, 210));
        list2.setAlignmentX((float) 1.0);
        list2.setAlignmentY((float) 1.0);
        list2.setCellRenderer(renderer);
        list2.setVisibleRowCount(7);
        list2.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                list2_mousePressed(e);
            }
        });
        look.setText("check friend info");
        add.setText("Send Friend Request"); // Updated text
        look.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                look_mousePressed(e);
            }
        });
        add.setText("add friend");
        add.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                add_mousePressed(e);
            }
        });
        this.getContentPane().add(jLabel1, null);
        this.getContentPane().add(new JScrollPane(list2));
        this.getContentPane().add(find2, null);

        this.getContentPane().add(jButton3, null);
        findmenu.add(look);
        findmenu.add(add);
    }//关闭窗口事件处理

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            this.dispose();
            this.hide();
        }
    }

    // 查找好友信息
    void find2_mouseClicked(MouseEvent e) {
    // Clear previous data first
    nickname.clear(); sex.clear(); place.clear(); ip.clear();
    emails.clear(); infos.clear(); status.clear(); jicq.clear(); pic.clear();
    
    DefaultListModel mm = (DefaultListModel) list2.getModel();
    mm.clear(); // Clear the list model
    
    out.println("find");
    
    try {
        String s;
        int count = 0;
        // read find info - first set of data
        do {
            s = in.readLine();
            if (s.equals("over"))
                break;
            nickname.add(s);
            sex.add(in.readLine());
            place.add(in.readLine());
            ip.add(in.readLine());
            emails.add(in.readLine());
            infos.add(in.readLine());
            status.add(in.readLine()); // String status
            count++;
        } while (!s.equals("over"));
        
        // read their jicqno - second set of data
        int theirjicq, picinfo;
        String sta;
        for (int x = 0; x < count; x++) {
            theirjicq = Integer.parseInt(in.readLine());
            jicq.add(new Integer(theirjicq));
            
            picinfo = Integer.parseInt(in.readLine());
            pic.add(new Integer(picinfo));
            
            sta = in.readLine(); // Read but don't store in status vector
            // status vector already has the string status from first set
        }
        
        // display found friends
        for (int i = 0; i < nickname.size(); i++) {
            mm.addElement(new Object[] { 
                nickname.get(i), sex.get(i), place.get(i), status.get(i) 
            });
        }
        
    } catch (IOException e4) {
        JOptionPane.showMessageDialog(this, "搜索失败: " + e4.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        e4.printStackTrace();
    }
}

    // 显示好友菜单
    void list2_mousePressed(MouseEvent e) {
        findmenu.show(this, e.getX() + 20, e.getY() + 50);

    }

    //add frined
    // 添加好友
// 添加好友 - Send friend request
void add_mousePressed(MouseEvent e) {
    int dd = list2.getSelectedIndex();
    
    // Check if a friend is selected
    if (dd == -1) {
        JOptionPane.showMessageDialog(this, "Please select a friend first!", "提示", 
                JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Check if vectors have data at the selected index
    if (dd >= jicq.size() || dd >= nickname.size() || dd >= ip.size() || dd >= pic.size()) {
        JOptionPane.showMessageDialog(this, "Data error, please search again!", "错误", 
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    // Get the friend's JICQ number
    Integer friendJicq = (Integer) jicq.get(dd);
    
    // Send friend request to server (not adding directly anymore)
    out.println("addfriend");
    out.println(friendJicq);  // Friend's JICQ number
    out.println(myid);        // Our JICQ number
    
    try {
        String response = in.readLine();
        System.out.println("Server response: " + response);
        
        // Handle different server responses
        if (response.equals("user_not_found")) {
            JOptionPane.showMessageDialog(this, "User not found!", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } 
        else if (response.equals("cannot_add_self")) {
            JOptionPane.showMessageDialog(this, "You cannot add yourself as a friend!", "Error",
                    JOptionPane.WARNING_MESSAGE);
        }
        else if (response.equals("already_friends")) {
            JOptionPane.showMessageDialog(this, "You are already friends with this user!", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.equals("request_exists")) {
            JOptionPane.showMessageDialog(this, "Friend request already sent! Waiting for response.", "Info",
                    JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.equals("reverse_request_exists")) {
            JOptionPane.showMessageDialog(this, 
                "This user has already sent you a friend request!\n" +
                "Please check your pending requests.", 
                "Info", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (response.equals("request_sent")) {
            // Friend request was successfully created
            JOptionPane.showMessageDialog(this, 
                "Friend request sent successfully!\n" +
                "The user will be notified and must accept your request.", 
                "Success", JOptionPane.INFORMATION_MESSAGE);
            
            // Optional: Remove from search results to avoid duplicate requests
            // This is optional - you can keep the user in the list
            DefaultListModel mm = (DefaultListModel) list2.getModel();
            if (dd < mm.getSize()) {
                // You could remove the item or just leave it
                // mm.remove(dd);
            }
        }
        else if (response.equals("request_failed")) {
            JOptionPane.showMessageDialog(this, "Failed to send friend request. Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        else if (response.equals("error")) {
            JOptionPane.showMessageDialog(this, "An error occurred. Please try again.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
        else {
            // Handle old response format (for backward compatibility)
            if (response.equals("same")) {
                JOptionPane.showMessageDialog(this, "Friend already exists", "Error",
                        JOptionPane.INFORMATION_MESSAGE);
            } else if (response.equals("add")) {
                // OLD BEHAVIOR - This shouldn't happen with the new server
                JOptionPane.showMessageDialog(this, 
                    "Added friend directly (old server behavior).\n" +
                    "Please update your server to the new version.", 
                    "Warning", JOptionPane.WARNING_MESSAGE);
                
                // Old behavior - add to temporary vectors
                String currentStatus = (String) status.get(dd);
                
                tmpjicq.add(friendJicq);
                tmpname.add(nickname.get(dd));
                tmpip.add(ip.get(dd));
                tmpemail.add(emails.get(dd));
                tmpinfo.add(infos.get(dd));
                tmpstatus.add(currentStatus);
                
                if (dd < pic.size()) {
                    tmppic.add(pic.get(dd));
                } else {
                    tmppic.add(new Integer(0));
                }
                
                JOptionPane.showMessageDialog(this, "Friend added successfully (old method)", "Successful",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Unknown response: " + response, "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    } catch (IOException e2) {
        JOptionPane.showMessageDialog(this, "Failed to send friend request: " + e2.getMessage(), 
                "Error", JOptionPane.ERROR_MESSAGE);
        e2.printStackTrace();
    }
}
// 查看好友详细信息
// Check for pending friend requests
public void checkPendingRequests() {
    try {
        out.println("checkpending");
        out.println(myid);
        
        String countStr = in.readLine();
        int pendingCount = Integer.parseInt(countStr);
        
        if (pendingCount > 0) {
            JOptionPane.showMessageDialog(this, 
                "You have " + pendingCount + " pending friend request(s).\n" +
                "Please check the main window for notifications.", 
                "Pending Requests", JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (IOException ex) {
        // Silent fail - not critical
        System.out.println("Could not check pending requests: " + ex.getMessage());
    } catch (NumberFormatException ex) {
        System.out.println("Invalid response for pending requests");
    }
}

void look_mousePressed(MouseEvent e) {
    int selectedIndex = list2.getSelectedIndex();
    
    // Check if a friend is actually selected
    if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(this, "Please select a user first!", "提示", 
                JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Check if vectors have data at the selected index
    if (selectedIndex >= nickname.size() || selectedIndex >= sex.size() || 
        selectedIndex >= place.size() || selectedIndex >= emails.size() || 
        selectedIndex >= infos.size()) {
        JOptionPane.showMessageDialog(this, "Data error, cannot display user information!", "错误", 
                JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Get all the user information
    String userName = (String) nickname.get(selectedIndex);
    String userSex = (String) sex.get(selectedIndex);
    String userPlace = (String) place.get(selectedIndex);
    String userEmail = (String) emails.get(selectedIndex);
    String userInfo = (String) infos.get(selectedIndex);
    String userStatus = (String) status.get(selectedIndex);
    Integer userJicq = (Integer) jicq.get(selectedIndex);
    
    // Create a detailed information message
    String infoMessage = 
        "User Information:\n\n" +
        "Name: " + userName + "\n" +
        "JICQ#: " + userJicq + "\n" +
        "Gender: " + userSex + "\n" +
        "Location: " + userPlace + "\n" +
        "Email: " + userEmail + "\n" +
        "Status: " + (userStatus.equals("1") ? "Online" : "Offline") + "\n" +
        "About: " + userInfo + "\n\n" +
        "Note: You need to send a friend request and wait for acceptance.";
    
    // Display the information in a dialog
    JOptionPane.showMessageDialog(this, infoMessage, "User Info - " + userName, 
            JOptionPane.INFORMATION_MESSAGE);
}
}