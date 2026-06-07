package com.messaging;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.ListModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

// Main window
public class MainWin extends JFrame implements Runnable {
    JPanel contentPane;

    String filepath;
    String filename;
    String file;

    // Friend information
    Vector friendnames = new Vector();
    int friendnum; // friend number

    private String[] picsonline = new String[] { "1.jpg", "3.jpg", "5.jpg", "7.jpg" };
    private String[] picsoffline = new String[] { "2.jpg", "4.jpg", "6.jpg", "8.jpg" };

    Vector friendjicq = new Vector();
    Vector udpport = new Vector();
    Vector friendips = new Vector();
    Vector friendemail = new Vector();
    Vector friendsex = new Vector();
    Vector friendinfo = new Vector();
    Vector picno = new Vector();
    Vector status = new Vector();

    // Temporary friend information during update
    Vector tempname = new Vector();
    Vector tempjicq = new Vector();
    Vector tempip = new Vector();
    Vector temppic = new Vector();
    Vector tempstatus = new Vector();
    Vector whoaddmesip = new Vector();
    Vector tempemail = new Vector();
    Vector tempinfo = new Vector();

    int index; 
    int index3; 
    int index4; 
    boolean fromunknow = false;

    // Friend search window
    FindFriend2 findf;
    JDialog hello = new JDialog();
    JDialog OneAddyou = new JDialog();
    JDialog DirectAdd = new JDialog();
    int tempgetjicq; 

    JDialog about = new JDialog();
    JDialog senddata = new JDialog();
    JDialog getdata = new JDialog();
    JButton ok = new JButton();

    JPopupMenu jPopupMenu1 = new JPopupMenu();
    JMenuItem sendmessage = new JMenuItem();
    JMenuItem getmessage = new JMenuItem();
    JMenuItem sendfile = new JMenuItem();
    JMenuItem lookinfo = new JMenuItem();
    JMenuItem chatrecord = new JMenuItem();
    JMenuItem delfriend = new JMenuItem();

    JLabel name = new JLabel();
    JTextField nametext = new JTextField();
    JLabel icq = new JLabel();
    JTextField icqno = new JTextField();
    JButton send = new JButton();
    JButton cancel = new JButton();
    JTextArea sendtext = new JTextArea();
    
    JList list;

    // Top Menu Bar UI Components
    JMenuBar mb = new JMenuBar();
    JMenu contactsMenu = new JMenu("Contacts");
    JMenu notificationsMenu = new JMenu("Notifications");
    JMenu systemMenu = new JMenu("System");
    
    JMenuItem exititem = new JMenuItem("Exit");
    JMenuItem find = new JMenuItem("Find Contact");
    JMenuItem direct = new JMenuItem("Add by ID");
    JMenuItem update = new JMenuItem("Refresh List");
    JMenuItem online = new JMenuItem("Pending Requests");
    JMenuItem myinfo = new JMenuItem("System Messages");
    
    // Network objects
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    DatagramPacket sendPacket, receivePacket;
    DatagramSocket sendSocket, receiveSocket;
    int udpPORT = 5001;
    int sendPort = 5001;
    String server;
    int serverport;
    byte array[] = new byte[512];
    Thread thread;
    int myjicq;
    String received = ""; 

    JLabel jLabel3 = new JLabel();
    JTextField getfromname = new JTextField();
    JLabel jLabel4 = new JLabel();
    JTextField getfromjicq = new JTextField();
    JTextArea getinfo = new JTextArea();
    JButton getok = new JButton();
    String theip;
    
    JLabel jLabel1 = new JLabel();
    JTextField helloname = new JTextField();
    JLabel jLabel5 = new JLabel();
    JTextField hellojicq = new JTextField();
    JLabel jLabel6 = new JLabel();
    JTextField helloemail = new JTextField();
    JLabel jLabel7 = new JLabel();
    JTextArea helloinfo = new JTextArea();
    JButton hellook = new JButton();
    JLabel jLabel10 = new JLabel();
    JLabel oneaddme = new JLabel();
    JButton addit = new JButton();
    JButton iknow = new JButton();
    JLabel jLabel11 = new JLabel();
    JLabel jLabel12 = new JLabel();
    JLabel jLabel13 = new JLabel();
    JTextField hellosex = new JTextField();
    JTextField friendid = new JTextField();
    JButton directaddok = new JButton();

    private void sendRelayToServer(String message) {
        try {
            byte[] data = message.getBytes();
            DatagramPacket packet = new DatagramPacket(data, message.length(),
                    InetAddress.getByName(server), udpPORT);
            sendSocket.send(packet);
            System.out.println("Sent relay message to server: " + message);
        } catch (Exception e) {
            System.out.println("Failed to send relay to server: " + e.getMessage());
        }
    }

    public void ConnectServer(int myid) {
        try {
            socket = new Socket(InetAddress.getByName(server), serverport);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);

            out.println("friend");
            out.println(myid);
            friendnum = Integer.parseInt(in.readLine());
            String friendname = " ";

            String friendjicqno, friendip, friendstatus, picinfo, email, sex, infos;
            do {
                friendname = in.readLine();
                if (friendname.equals("over"))
                    break;
                friendnames.add(friendname);
                friendjicqno = in.readLine();
                friendjicq.add(new Integer(friendjicqno));
                friendip = in.readLine();
                friendips.add(friendip);
                friendstatus = in.readLine();
                status.add(friendstatus);
                picinfo = in.readLine();
                picno.add(new Integer(picinfo));
                email = in.readLine();
                friendemail.add(email);
                sex = in.readLine();
                friendsex.add(sex);
                infos = in.readLine();
                friendinfo.add(infos);
            } while (!friendname.equals("over"));
            out.println(udpPORT);
        } catch (IOException e1) {
            System.out.println("false");
            return; 
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                DefaultListModel mm = (DefaultListModel) list.getModel();
                int picid;
                for (int p = 0; p < friendnames.size(); p++) {
                    picid = Integer.parseInt(picno.get(p).toString());
                    if (status.get(p).equals("1")) {
                        mm.addElement(new Object[] { friendnames.get(p),
                                new ImageIcon(picsonline[picid]) });
                    } else {
                        mm.addElement(new Object[] { friendnames.get(p),
                                new ImageIcon(picsoffline[picid]) });
                    }
                }
            }
        });
    }
    
    public void run() {
        while (true) {
            try {
                for (int x = 0; x < 512; x++)
                    array[x] = ' ';
                receivePacket = new DatagramPacket(array, array.length);
                receiveSocket.receive(receivePacket);
                byte[] data = receivePacket.getData();
                String infofromip = receivePacket.getAddress().getHostAddress()
                        .toString().trim();
                int receivedLength = receivePacket.getLength();
                index3 = 0;
                received = new String(data, 0, receivedLength);
                received = received.trim();  
                
                boolean fromServer = infofromip.equals(server) || 
                                   infofromip.equals("127.0.0.1") && server.equals("localhost");
                
                if (received.equals("test from " + myjicq)) {
                    continue; 
                }
                
                String tempstr;
                int tx;
                
                if (received.length() >= 6 && received.startsWith("online")) {
                    tempstr = received.substring(6).trim();
                    tempgetjicq = Integer.parseInt(tempstr);
                    
                    boolean found = false;
                    for (int i = 0; i < friendjicq.size(); i++) {
                        tx = Integer.parseInt(friendjicq.get(i).toString());
                        if (tempgetjicq == tx) {
                            index3 = i;
                            found = true;
                            break;
                        }
                    }
                    
                    if (found) {
                        DefaultListModel mm3 = (DefaultListModel) list.getModel();
                        int picid = Integer.parseInt(picno.get(index3).toString());
                        mm3.setElementAt(new Object[] { friendnames.get(index3),
                                new ImageIcon(picsonline[picid]) }, index3);
                    }
                }
                else if (received.length() >= 7 && received.startsWith("offline")) {
                    tempstr = received.substring(7).trim();
                    tempgetjicq = Integer.parseInt(tempstr);
                    
                    boolean found = false;
                    for (int i = 0; i < friendjicq.size(); i++) {
                        tx = Integer.parseInt(friendjicq.get(i).toString());
                        if (tempgetjicq == tx) {
                            index3 = i;
                            found = true;
                            break;
                        }
                    }
                    
                    if (found) {
                        friendips.setElementAt("null", index3);
                        DefaultListModel mm3 = (DefaultListModel) list.getModel();
                        int picid = Integer.parseInt(picno.get(index3).toString());
                        mm3.setElementAt(new Object[] { friendnames.get(index3),
                                new ImageIcon(picsoffline[picid]) }, index3);
                    }
                }
                else if (received.length() >= 9 && received.startsWith("oneaddyou")) {
                    tempstr = received.substring(9).trim();
                    final int addingUserJicq = Integer.parseInt(tempstr);
                    tempgetjicq = addingUserJicq;
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            oneaddme.setText("User " + addingUserJicq + " added you as a friend!");
                            OneAddyou.setBounds(400, 300, 250, 200);
                            OneAddyou.setLocationRelativeTo(MainWin.this);
                            OneAddyou.setVisible(true);
                        }
                    });
                }
                else if (received.length() >= 9 && received.startsWith("readysend")) {
                    SendFile sf = new SendFile(theip, file);
                    sf.fileClient();
                }
                else if (received.startsWith("friend_request:")) {
                    String[] parts = received.split(":", 3);
                    if (parts.length == 3) {
                        try {
                            final int requesterId = Integer.parseInt(parts[1].trim());
                            final String requesterName = parts[2];
                            
                            SwingUtilities.invokeLater(() -> {
                                oneaddme.setText(requesterName + " (" + requesterId + ") wants to be friends.");
                                tempgetjicq = requesterId;
                                OneAddyou.setLocationRelativeTo(MainWin.this);
                                OneAddyou.setVisible(true);
                            });
                        } catch (NumberFormatException e) { }
                    }
                }
                else if (received.startsWith("friend_added:")) {
                    String[] parts = received.split(":", 3);
                    if (parts.length == 3) {
                        try {
                            final int friendId = Integer.parseInt(parts[1]);
                            final String friendName = parts[2];
                            
                            SwingUtilities.invokeLater(() -> {
                                JOptionPane.showMessageDialog(MainWin.this,
                                    "You are now friends with " + friendName + " (JICQ: " + friendId + ")",
                                    "New Contact",
                                    JOptionPane.INFORMATION_MESSAGE);
                            });
                        } catch (NumberFormatException e) { }
                    }
                }
                else if (received.length() >= 12 && received.startsWith("readyreceive")) {
                    FileDialog fdsave = new FileDialog(this, "Save File", 1);
                    fdsave.setVisible(true);
                    String dir = fdsave.getDirectory();
                    String name = received.substring(12);
                    fdsave.setFile(name);
                    
                    String filename = received.substring(12);
                    file = (dir != null ? dir : "") + filename;
                    
                    GetFile gf = new GetFile(dir, "receive");
                    gf.fileServer();
                }
                else if (received.length() > 0) {
                    if (received.startsWith("from:")) {
                        String[] parts = received.split(":", 3);
                        if (parts.length == 3) {
                            try {
                                int senderJicq = Integer.parseInt(parts[1]);
                                String actualMessage = parts[2];
                                
                                boolean found = false;
                                String friendName = "Unknown";
                                int foundIndex = -1;
                                
                                for (int i = 0; i < friendjicq.size(); i++) {
                                    int friendId = Integer.parseInt(friendjicq.get(i).toString());
                                    if (friendId == senderJicq) {
                                        friendName = friendnames.get(i).toString().trim();
                                        foundIndex = i;
                                        found = true;
                                        break;
                                    }
                                }
                                
                                if (found) {
                                    final String displayName = friendName;
                                    final String message = actualMessage;
                                    final int idx = foundIndex;
                                    
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            JOptionPane.showMessageDialog(MainWin.this, 
                                                "Message from " + displayName + ":\n" + message, 
                                                "New Message", JOptionPane.INFORMATION_MESSAGE);
                                            index4 = idx;
                                        }
                                    });
                                } else {
                                    final String message = actualMessage;
                                    SwingUtilities.invokeLater(new Runnable() {
                                        public void run() {
                                            JOptionPane.showMessageDialog(MainWin.this, 
                                                "Message from unknown sender (JICQ: " + parts[1] + "):\n" + message, 
                                                "Unknown Message", JOptionPane.INFORMATION_MESSAGE);
                                            fromunknow = true;
                                        }
                                    });
                                }
                                continue; 
                            } catch (NumberFormatException e) { }
                        }
                    }
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }
    
    public void CreatUDP() {
        try {
            receiveSocket = new DatagramSocket(udpPORT);
            sendSocket = receiveSocket; 
            
            String testMsg = "online:" + myjicq;
            byte[] testData = testMsg.getBytes("UTF-8");
            DatagramPacket testPacket = new DatagramPacket(testData, testData.length, 
                    InetAddress.getByName(server), udpPORT);
            sendSocket.send(testPacket);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MainWin(int s, String sername, int serport) {
        udpPORT = 5001;  
        sendPort = 5001;
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            myjicq = s;
            server = sername;
            serverport = serport;
            
            jbInit();
            
            findf = new FindFriend2(myjicq, server, serverport);
            findf.setBounds(200, 150, 300, 300);
            
            this.setVisible(true);
            this.setLocationRelativeTo(null);
            
            Thread networkThread = new Thread(new Runnable() {
                public void run() {
                    try {
                        ConnectServer(myjicq);
                        CreatUDP();
                        
                        try {
                            out.println("setudpport");
                            out.println(myjicq);
                            out.println(udpPORT);
                        } catch (Exception ex) { }
                        
                        try {
                            out.println("online");
                            out.println(myjicq);
                        } catch (Exception ex) { }
                        
                        thread = new Thread(MainWin.this);
                        thread.start();
                        
                        checkPendingFriendRequestsImmediate();
                        
                        try {
                            String onlineMsg = "online:" + myjicq;
                            sendRelayToServer(onlineMsg);
                        } catch (Exception ex) { }
                        
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                JOptionPane.showMessageDialog(MainWin.this,
                                    "Error connecting to server: " + e.getMessage(),
                                    "Connection Error",
                                    JOptionPane.ERROR_MESSAGE);
                            }
                        });
                    }
                }
            });
            networkThread.start();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkPendingFriendRequestsImmediate() {
        try {
            out.println("getpendingrequests");
            out.println(myjicq);
            
            String pendingCount = in.readLine();
            int count = Integer.parseInt(pendingCount);
            
            if (count > 0) {
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        JOptionPane.showMessageDialog(MainWin.this, 
                            "You have " + count + " pending friend request(s).\nPlease check 'Pending Requests' in the Notifications menu.",
                            "Pending Requests", JOptionPane.INFORMATION_MESSAGE);
                    }
                });
            }
        } catch (IOException ex) { }
    }

    private void jbInit() throws Exception {
        
        // 1. Friend Request Notification Dialog
        OneAddyou.setTitle("Friend Request");
        OneAddyou.getContentPane().setLayout(null);
        OneAddyou.getContentPane().setBackground(new Color(245, 245, 250));
        OneAddyou.setSize(280, 200);
        OneAddyou.setResizable(false);
        OneAddyou.setModal(true);
        OneAddyou.setLocationRelativeTo(this);

        jLabel10.setText("New Friend Request:");
        jLabel10.setBounds(new Rectangle(15, 15, 200, 20));
        jLabel10.setFont(new Font("Arial", Font.BOLD, 12));
        oneaddme.setBounds(new Rectangle(15, 50, 250, 25));
        oneaddme.setForeground(new Color(0, 102, 204));
        oneaddme.setFont(new Font("Arial", Font.PLAIN, 12));

        addit.setText("Accept");
        addit.setBounds(new Rectangle(30, 110, 90, 30));
        addit.setBackground(new Color(100, 180, 100));
        addit.setForeground(Color.WHITE);
        addit.setFocusPainted(false);
        addit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { addit_mouseClicked(e); }
        });

        iknow.setText("Decline");
        iknow.setBounds(new Rectangle(145, 110, 90, 30));
        iknow.setBackground(new Color(180, 100, 100));
        iknow.setForeground(Color.WHITE);
        iknow.setFocusPainted(false);
        iknow.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { iknow_mouseClicked(e); }
        });

        OneAddyou.getContentPane().add(jLabel10, null);
        OneAddyou.getContentPane().add(oneaddme, null);
        OneAddyou.getContentPane().add(addit, null);
        OneAddyou.getContentPane().add(iknow, null);

        // 2. Main Menu Bar Setup (Top Menu)
        contactsMenu.add(find);
        contactsMenu.add(direct);
        contactsMenu.addSeparator();
        contactsMenu.add(update);
        
        notificationsMenu.add(online);
        notificationsMenu.add(myinfo);
        
        systemMenu.add(exititem);

        mb.add(contactsMenu);
        mb.add(notificationsMenu);
        mb.add(systemMenu);
        
        this.setJMenuBar(mb); // Move menu to the top of the frame natively

        // 3. Main Frame Configuration
        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.setBorder(new EmptyBorder(10, 10, 10, 10)); 
        this.getContentPane().setBackground(new Color(245, 245, 250));
        this.setResizable(true); 
        this.setSize(new Dimension(300, 550)); 
        this.setTitle("JICQ Client");

        // 4. Top Area: User Info
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        JLabel myIdLabel = new JLabel(" My JICQ ID: " + myjicq, JLabel.LEFT);
        myIdLabel.setFont(new Font("Arial", Font.BOLD, 14));
        myIdLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        topPanel.add(myIdLabel, BorderLayout.CENTER);
        contentPane.add(topPanel, BorderLayout.NORTH);

        // 5. Center Area: Contact List
        ListModel model = new NameAndPicListModel(friendnames, picsonline);
        ListCellRenderer renderer = new NameAndPicListCellRenderer();
        list = new JList(model);
        list.setBackground(Color.WHITE);
        list.setCellRenderer(renderer);
        list.addMouseListener(new MainWin_list_mouseAdapter(this));
        
        JScrollPane listScrollPane = new JScrollPane(list);
        listScrollPane.setBorder(BorderFactory.createTitledBorder("Contacts"));
        contentPane.add(listScrollPane, BorderLayout.CENTER);

        // 6. Menu Events Setup
        find.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { find_mousePressed(e); }
        });
        direct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { direct_mousePressed(e); }
        });
        myinfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { myinfo_mousePressed(e); }
        });
        online.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { online_mousePressed(e); }
        });
        update.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { update_mousePressed(e); }
        });
        exititem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) { System.exit(0); }
        });

        // 7. Context Menu Setup (Right Click on Contact)
        sendmessage.setText("Send Message");
        sendmessage.addMouseListener(new MainWin_sendmessage_mouseAdapter(this));
        getmessage.setText("View Message");
        getmessage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { getmessage_mousePressed(e); }
        });
        sendfile.setText("Send File");
        sendfile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { sendfile_mousePressed(e); }
        });
        lookinfo.setText("View Profile");
        lookinfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { lookinfo_mousePressed(e); }
        });
        delfriend.setText("Delete Contact");
        delfriend.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) { delfriend_mousePressed(e); }
        });

        jPopupMenu1.add(sendmessage);
        jPopupMenu1.add(getmessage);
        jPopupMenu1.add(sendfile);
        jPopupMenu1.add(lookinfo);
        jPopupMenu1.addSeparator();
        jPopupMenu1.add(delfriend);

        // 8. Dialog: Send Message
        Container senddiapane = senddata.getContentPane();
        senddiapane.setLayout(null);
        senddata.setTitle("Send Message");
        name.setText("Nickname:");
        name.setBounds(new Rectangle(15, 15, 70, 20));
        nametext.setBounds(new Rectangle(85, 15, 100, 22));
        nametext.setEditable(false);
        icq.setText("JICQ ID:");
        icq.setBounds(new Rectangle(200, 15, 60, 20));
        icqno.setBounds(new Rectangle(260, 15, 100, 22));
        icqno.setEditable(false);
        
        sendtext.setBounds(new Rectangle(15, 50, 350, 130));
        sendtext.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        send.setText("Send");
        send.setBounds(new Rectangle(80, 195, 100, 30));
        send.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { send_mouseClicked(e); }
        });
        cancel.setText("Cancel");
        cancel.setBounds(new Rectangle(210, 195, 100, 30));
        cancel.addMouseListener(new MainWin_cancel_mouseAdapter(this));
        
        senddiapane.add(send);
        senddiapane.add(cancel);
        senddiapane.add(sendtext);
        senddiapane.add(name);
        senddiapane.add(nametext);
        senddiapane.add(icq);
        senddiapane.add(icqno);
        senddata.setSize(400, 280);

        // 9. Dialog: View Message
        getdata.getContentPane().setLayout(null);
        getdata.setTitle("Received Message");
        jLabel3.setText("From:");
        jLabel3.setBounds(new Rectangle(15, 15, 50, 20));
        getfromname.setBounds(new Rectangle(65, 15, 100, 22));
        getfromname.setEditable(false);
        jLabel4.setText("JICQ ID:");
        jLabel4.setBounds(new Rectangle(180, 15, 60, 20));
        getfromjicq.setBounds(new Rectangle(240, 15, 100, 22));
        getfromjicq.setEditable(false);
        
        getinfo.setBounds(new Rectangle(15, 50, 350, 140));
        getinfo.setEditable(false);
        getinfo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        getok.setText("Close");
        getok.setBounds(new Rectangle(150, 205, 90, 30));
        getok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { getok_mouseClicked(e); }
        });
        getdata.getContentPane().add(getinfo);
        getdata.getContentPane().add(getok);
        getdata.getContentPane().add(jLabel3);
        getdata.getContentPane().add(getfromname);
        getdata.getContentPane().add(jLabel4);
        getdata.getContentPane().add(getfromjicq);
        getdata.setSize(400, 280);

        // 10. Dialog: Contact Profile
        hello.getContentPane().setLayout(null);
        hello.setTitle("Contact Profile");
        jLabel1.setText("Nickname:");
        jLabel1.setBounds(new Rectangle(15, 20, 70, 20));
        helloname.setBounds(new Rectangle(85, 20, 100, 22));
        helloname.setEditable(false);
        jLabel5.setText("JICQ ID:");
        jLabel5.setBounds(new Rectangle(200, 20, 60, 20));
        hellojicq.setBounds(new Rectangle(260, 20, 100, 22));
        hellojicq.setEditable(false);
        
        jLabel6.setText("Email:");
        jLabel6.setBounds(new Rectangle(15, 60, 50, 20));
        helloemail.setBounds(new Rectangle(65, 60, 130, 22));
        helloemail.setEditable(false);
        
        jLabel13.setText("Gender:");
        jLabel13.setBounds(new Rectangle(210, 60, 50, 20));
        hellosex.setBounds(new Rectangle(265, 60, 95, 22));
        hellosex.setEditable(false);
        
        jLabel7.setText("About Me:");
        jLabel7.setBounds(new Rectangle(15, 100, 80, 20));
        helloinfo.setBounds(new Rectangle(15, 130, 345, 80));
        helloinfo.setEditable(false);
        helloinfo.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        
        hellook.setText("Close");
        hellook.setBounds(new Rectangle(145, 230, 90, 30));
        hellook.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { hellook_mouseClicked(e); }
        });
        hello.getContentPane().add(jLabel1);
        hello.getContentPane().add(helloname);
        hello.getContentPane().add(jLabel5);
        hello.getContentPane().add(hellojicq);
        hello.getContentPane().add(jLabel6);
        hello.getContentPane().add(helloemail);
        hello.getContentPane().add(jLabel7);
        hello.getContentPane().add(jLabel13);
        hello.getContentPane().add(hellosex);
        hello.getContentPane().add(helloinfo);
        hello.getContentPane().add(hellook);
        hello.setSize(390, 320);

        // 11. Dialog: Direct Add
        DirectAdd.getContentPane().setLayout(null);
        DirectAdd.setTitle("Add Contact");
        jLabel11.setText("Enter JICQ ID to add new contact:");
        jLabel11.setBounds(new Rectangle(20, 20, 220, 20));
        jLabel12.setText("JICQ ID:");
        jLabel12.setBounds(new Rectangle(20, 60, 60, 20));
        friendid.setBounds(new Rectangle(85, 60, 140, 22));
        
        directaddok.setText("Submit");
        directaddok.setBounds(new Rectangle(80, 110, 100, 30));
        directaddok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) { directaddok_mouseClicked(e); }
        });
        DirectAdd.getContentPane().add(jLabel11);
        DirectAdd.getContentPane().add(jLabel12);
        DirectAdd.getContentPane().add(friendid);
        DirectAdd.getContentPane().add(directaddok);
        DirectAdd.setSize(280, 200);
    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (sendSocket != null && !sendSocket.isClosed()) {
                try {
                    String s = "offline:" + myjicq;
                    byte[] data = s.getBytes();
                    sendPacket = new DatagramPacket(data, s.length(),
                            InetAddress.getByName(server), udpPORT);
                    sendSocket.send(sendPacket);
                } catch (IOException e2) { }
            }
            if (out != null) {
                out.println("logout");
                out.println(myjicq);
            }
            closeQuietly(sendSocket);
            closeQuietly(receiveSocket);
            closeQuietly(socket);
            System.exit(0);
        }
    }

    private void closeQuietly(Socket socket) {
        if (socket != null) { try { socket.close(); } catch (IOException e) {} }
    }

    private void closeQuietly(DatagramSocket datagramSocket) {
        if (datagramSocket != null) { datagramSocket.close(); }
    }

    void list_mouseClicked(MouseEvent e) { 
        jPopupMenu1.show(this, e.getX() + 20, e.getY() + 20);
    }

    void direct_mousePressed(MouseEvent e) {
        DirectAdd.setLocationRelativeTo(MainWin.this);
        DirectAdd.setVisible(true);
    }

    void cancel_mouseClicked(MouseEvent e) {
        senddata.dispose();
    }

    void ok_mouseClicked(MouseEvent e) {
        if (about != null) {
            about.dispose();
        }
    }

    void sendmessage_mousePressed(MouseEvent e) {
        index = list.getSelectedIndex();
        if(index < 0) return;
        nametext.setText(friendnames.get(index).toString());
        icqno.setText(friendjicq.get(index).toString());
        theip = friendips.get(index).toString();
        senddata.setLocationRelativeTo(MainWin.this);
        senddata.setVisible(true);
    }

    void find_mousePressed(MouseEvent e) {
        findf.setLocationRelativeTo(MainWin.this);
        findf.setVisible(true);
    }

    void send_mouseClicked(MouseEvent e) { 
        try {
            String s = sendtext.getText().trim();
            if (s.isEmpty()) return;
            
            index = list.getSelectedIndex();
            if(index < 0) return;
            String friendJicq = friendjicq.get(index).toString();
            
            String relayMessage = "relay:" + friendJicq + ":from:" + myjicq + ":" + s;
            byte[] data = relayMessage.getBytes("UTF-8");
            
            sendPacket = new DatagramPacket(data, data.length, 
                    InetAddress.getByName(server), udpPORT);
            sendSocket.send(sendPacket);

        } catch (Exception e2) {
            e2.printStackTrace();
        }
        sendtext.setText("");
        senddata.dispose();
    }

    void getmessage_mousePressed(MouseEvent e) { 
        if (received == null || received.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "No new messages.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        String message = received.trim();
        index = list.getSelectedIndex();
        if (index == index4)
            getinfo.append(message + "\n"); 
        else
            getinfo.append(" "); 
            
        if(index >= 0) {
            getfromname.setText(friendnames.get(index).toString());
            getfromjicq.setText(friendjicq.get(index).toString());
        }
        getdata.setLocationRelativeTo(MainWin.this);
        getdata.setVisible(true);
    }
    
    void getok_mouseClicked(MouseEvent e) {
        getinfo.setText(" ");
        getdata.dispose();
        received = " ";
    }

    void update_mousePressed(MouseEvent e) {
        tempname = findf.tmpname;
        tempjicq = findf.tmpjicq;
        tempip = findf.tmpip;
        temppic = findf.tmppic;
        tempstatus = findf.tmpstatus;
        tempemail = findf.tmpemail;
        tempinfo = findf.tmpinfo;
        
        if (tempname.isEmpty()) {
            JOptionPane.showMessageDialog(this, "No new contacts to update.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        DefaultListModel mm2 = (DefaultListModel) list.getModel();
        int picid = 0;
        for (int p = 0; p < tempname.size(); p++) {
            if (p < temppic.size()) {
                picid = Integer.parseInt(temppic.get(p).toString());
            } else {
                picid = 0;
            }
            
            if (tempstatus.get(p).equals("1")) {
                mm2.addElement(new Object[] { tempname.get(p), new ImageIcon(picsonline[picid]) });
            } else {
                mm2.addElement(new Object[] { tempname.get(p), new ImageIcon(picsoffline[picid]) });
            }
        }
        
        for (int k = 0; k < tempname.size(); k++) {
            friendnames.add(tempname.get(k));
            friendjicq.add(tempjicq.get(k));
            friendips.add(tempip.get(k));
            if (k < temppic.size()) picno.add(temppic.get(k)); else picno.add(new Integer(0));
            if (k < tempstatus.size()) status.add(tempstatus.get(k)); else status.add("0");
            if (k < tempemail.size()) friendemail.add(tempemail.get(k)); else friendemail.add("");
            if (k < tempinfo.size()) friendinfo.add(tempinfo.get(k)); else friendinfo.add("");
        }
        
        findf.tmpip.clear();
        findf.tmpjicq.clear();
        findf.tmpname.clear();
        findf.tmppic.clear();
        findf.tmpstatus.clear();
        findf.tmpemail.clear();
        findf.tmpinfo.clear();
    }

    void delfriend_mousePressed(MouseEvent e) {
        int index2 = list.getSelectedIndex();
        if(index2 < 0) return;

        out.println("delfriend");
        out.println(friendjicq.get(index2).toString().trim());
        out.println(myjicq);
        
        DefaultListModel mm = (DefaultListModel) list.getModel();
        mm.removeElementAt(index2);
        friendnames.removeElementAt(index2);
        friendips.removeElementAt(index2);
        friendjicq.removeElementAt(index2);
        picno.removeElementAt(index2);
        status.removeElementAt(index2);
        friendemail.removeElementAt(index2);
        friendinfo.removeElementAt(index2);
    }

    void online_mousePressed(MouseEvent e) {
        try {
            String s = "online:" + myjicq;
            s = s.trim();
            byte[] data = s.getBytes();
            if (sendSocket != null) {
                sendPacket = new DatagramPacket(data, s.length(), InetAddress.getByName(server), udpPORT);
                sendSocket.send(sendPacket);
            }
        } catch (Exception e2) { }
        
        out.println("getpendingrequests");
        out.println(myjicq);
        
        try {
            String pendingCount = in.readLine();
            int count = Integer.parseInt(pendingCount);
            
            java.util.List<String> pendingList = new java.util.ArrayList<>();
            for (int i = 0; i < count; i++) {
                pendingList.add(in.readLine());
            }
            
            for (String requestData : pendingList) {
                String[] parts = requestData.split(":");
                if (parts.length >= 2) {
                    int requesterId = Integer.parseInt(parts[0]);
                    String requesterName = parts[1];
                    
                    Object[] options = {"Accept", "Decline"};
                    int choice = JOptionPane.showOptionDialog(this, 
                        "Friend request from " + requesterName + " (" + requesterId + ").\nDo you want to accept?", 
                        "Pending Request", 
                        JOptionPane.YES_NO_OPTION, 
                        JOptionPane.QUESTION_MESSAGE, 
                        null, options, options[0]);
                    
                    if (choice == JOptionPane.YES_OPTION) { 
                        out.println("acceptfriendrequest");
                        out.println(requesterId);
                        out.println(myjicq);
                        
                        String response = in.readLine();
                        if ("accepted".equals(response)) {
                            String thename = in.readLine();
                            String thejicqno = in.readLine();
                            String theip = in.readLine();
                            String thestatus = in.readLine();
                            String picinfo = in.readLine();
                            String email = in.readLine();
                            String sex = in.readLine();
                            String infos = in.readLine();
                            in.readLine(); 
                            
                            friendnames.add(thename);
                            friendjicq.add(new Integer(thejicqno));
                            friendips.add(theip);
                            status.add(thestatus);
                            picno.add(new Integer(picinfo));
                            friendemail.add(email);
                            friendsex.add(sex);
                            friendinfo.add(infos);
                            
                            DefaultListModel mm2 = (DefaultListModel) list.getModel();
                            int picid = Integer.parseInt(picinfo);
                            if ("1".equals(thestatus)) {
                                mm2.addElement(new Object[] { thename, new ImageIcon(picsonline[picid]) });
                            } else {
                                mm2.addElement(new Object[] { thename, new ImageIcon(picsoffline[picid]) });
                            }
                            JOptionPane.showMessageDialog(this, "Successfully added " + thename + "!");
                        }
                    } else if (choice == JOptionPane.NO_OPTION) { 
                        out.println("rejectfriendrequest");
                        out.println(requesterId);
                        out.println(myjicq);
                        in.readLine();
                    }
                }
            }
        } catch (Exception e2) { }
    }

    void myinfo_mousePressed(MouseEvent e) {
        if (fromunknow) {
            String message = received.trim();
            getinfo.setText(" ");
            getinfo.append(message);
            getdata.setLocationRelativeTo(MainWin.this);
            getdata.setVisible(true);
        }
    }

    void lookinfo_mousePressed(MouseEvent e) {
        index = list.getSelectedIndex();
        if(index < 0) return;
        helloname.setText(friendnames.get(index).toString());
        hellojicq.setText(friendjicq.get(index).toString());
        helloemail.setText(friendemail.get(index).toString());
        hellosex.setText(friendsex.get(index).toString());
        helloinfo.setText(friendinfo.get(index).toString().trim());
        hello.setLocationRelativeTo(MainWin.this);
        hello.setVisible(true);
    }

    void sendfile_mousePressed(MouseEvent e) {
        java.awt.FileDialog fd = new java.awt.FileDialog(this, "Select File to Send");
        fd.setVisible(true);
        if(fd.getFile() == null) return;
        filepath = fd.getDirectory();
        filename = fd.getFile().toString();
        file = filepath + filename;
        index = list.getSelectedIndex();
        
        String friendJicq = friendjicq.get(index).toString();
        String s = "filerequest:" + friendJicq + ":readyreceive" + filename;
        byte[] data = s.getBytes();
        try {
            sendPacket = new DatagramPacket(data, s.length(), InetAddress.getByName(server), udpPORT);
            sendSocket.send(sendPacket);
        } catch (Exception e2) { }
    }

    void hellook_mouseClicked(MouseEvent e) {
        hello.dispose();
    }

    void addit_mouseClicked(MouseEvent e) { 
        out.println("addnewfriend");
        out.println(tempgetjicq);
        out.println(myjicq);
        
        try {
            String thename = in.readLine();
            if (!thename.equals("over")) {
                friendnames.add(thename);
                String thejicqno = in.readLine();
                friendjicq.add(new Integer(thejicqno));
                String theip = in.readLine();
                friendips.add(theip);
                String thestatus = in.readLine();
                status.add(thestatus);
                String picinfo = in.readLine();
                picno.add(new Integer(picinfo));
                String email = in.readLine();
                friendemail.add(email);
                String sex = in.readLine();
                friendsex.add(sex);
                String infos = in.readLine();
                friendinfo.add(infos);
                
                DefaultListModel mm2 = (DefaultListModel) list.getModel();
                int picid = Integer.parseInt(picinfo);
                if (thestatus.equals("1")) {
                    mm2.addElement(new Object[] { thename, new ImageIcon(picsonline[picid]) });
                } else {
                    mm2.addElement(new Object[] { thename, new ImageIcon(picsoffline[picid]) });
                }
                JOptionPane.showMessageDialog(this, "Contact added successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        OneAddyou.dispose();
    }

    void iknow_mouseClicked(MouseEvent e) {
        OneAddyou.dispose();
    }

    void directaddok_mouseClicked(MouseEvent e) {
        out.println("addnewfriend");
        out.println(friendid.getText().trim());
        out.println(myjicq);
        String thename = " ";
        try {
            String thejicqno, theip, thestatus, picinfo, email, sex, infos;
            do {
                thename = in.readLine();
                if (thename.equals("over"))
                    break;
                friendnames.add(thename);
                thejicqno = in.readLine();
                friendjicq.add(new Integer(thejicqno));
                theip = in.readLine();
                friendips.add(theip);
                thestatus = in.readLine();
                status.add(thestatus);
                picinfo = in.readLine();
                picno.add(new Integer(picinfo));
                email = in.readLine();
                friendemail.add(email);
                sex = in.readLine();
                friendsex.add(sex);
                infos = in.readLine();
                friendinfo.add(infos);
            } while (!thename.equals("over"));
        } catch (IOException e1) { }
        
        int dddd = friendnames.size() - 1;
        if(dddd >= 0) {
            DefaultListModel mm2 = (DefaultListModel) list.getModel();
            int picid = Integer.parseInt(picno.get(dddd).toString());
            if (status.get(dddd).equals("1")) {
                mm2.addElement(new Object[] { friendnames.get(dddd), new ImageIcon(picsonline[picid]) });
            } else {
                mm2.addElement(new Object[] { friendnames.get(dddd), new ImageIcon(picsoffline[picid]) });
            }
        }
        DirectAdd.dispose();
    }
}