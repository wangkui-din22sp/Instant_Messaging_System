package com.messaging;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

import java.net.*;
import java.io.*;

public class New extends JFrame {//Login window
    JPanel contentPane;

    
    String server;//Server address

    int serport;//Port

    private Socket socket;

    private BufferedReader in;//Input stream

    private PrintWriter out;

    
    JPanel jPanel1 = new JPanel();

    JLabel jLabel1 = new JLabel();

    JLabel jLabel2 = new JLabel();

    JTextField jicq = new JTextField();

    JLabel jLabel3 = new JLabel();

    JPasswordField password = new JPasswordField();

    JPanel jPanel2 = new JPanel();

    JButton login = new JButton();

    JButton newuser = new JButton();

    JButton quit = new JButton();

    JLabel jLabel6 = new JLabel();

    JTextField servername = new JTextField();

    JLabel jLabel7 = new JLabel();

    JTextField serverport = new JTextField();

    sunPanel sunPanel = new sunPanel();

    //Constructor
    public New() {
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            jbInit();
            server = servername.getText().toString().trim();
            serport = Integer.parseInt(serverport.getText().trim());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void jbInit() throws Exception {

        contentPane = (JPanel) this.getContentPane();
        sunPanel.setLayout(null);
        
        this.setResizable(false);
        this.setSize(new Dimension(800, 800));
        this.setTitle("New JICQ");
        jPanel1.setBounds(new Rectangle(2, 3, 348, 110));
        jPanel1.setLayout(null);
        jLabel1.setText("Please Login with your JICQ credentials:");
        jLabel1.setBounds(new Rectangle(5, 7, 103, 18));
        jLabel2.setText("JICQ No:");
        jLabel2.setBounds(new Rectangle(7, 66, 58, 18));
        jicq.setBounds(new Rectangle(68, 65, 97, 22));
        jLabel3.setText("Password:");
        jLabel3.setBounds(new Rectangle(173, 66, 67, 18));
        password.setBounds(new Rectangle(237, 63, 94, 22));
        jPanel2.setBounds(new Rectangle(8, 154, 347, 151));
        jPanel2.setLayout(null);
        login.setText("Login");
        login.setBounds(new Rectangle(25, 120, 79, 29));
        login.setOpaque(false);
        
        login.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                login_mouseClicked(e);
            }
        });
        newuser.setText("New User");
        newuser.setBounds(new Rectangle(125, 120, 79, 29));
        newuser.setOpaque(false);
        newuser.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                newuser_mouseClicked(e);
            }
        });
        quit.setText("Quit");
        quit.setBounds(new Rectangle(225, 120, 79, 29));
        quit.setOpaque(false);
        quit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                quit_mouseClicked(e);
            }
        });
        jLabel6.setText("Server IP:");
        jLabel6.setBounds(new Rectangle(20, 62, 41, 18));
        servername.setText("0.0.0.0");
        servername.setBounds(new Rectangle(73, 62, 102, 22));
        jLabel7.setText("Port:");
        jLabel7.setBounds(new Rectangle(191, 62, 41, 18));
        serverport.setText("8080");
        serverport.setBounds(new Rectangle(241, 62, 90, 30));

        jPanel1.add(jLabel1);
        jPanel1.add(jLabel2);
        jPanel1.add(jicq);
        jPanel1.add(jLabel3);
        jPanel1.add(password);
        jPanel1.add(password);

        jPanel2.add(login);
        jPanel2.add(quit);
        jPanel2.add(newuser);

        jPanel2.add(jLabel6);
        jPanel2.add(servername);
        jPanel2.add(jLabel7);
        jPanel2.add(serverport);
        jPanel1.setOpaque(false);
        jPanel2.setOpaque(false);

        sunPanel.add(jPanel1);
        sunPanel.add(jPanel2);
        contentPane.add(sunPanel, BorderLayout.CENTER);

    }

    protected void processWindowEvent(WindowEvent e) {
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {//Main method
        New f = new New();
        f.setVisible(true);

    }

    void login_mouseClicked(MouseEvent e) {
    try {
        System.out.println("Connecting to server: " + server + ":" + serport);
        Socket socket = new Socket(InetAddress.getByName(server), serport);
        System.out.println("Connected to server successfully");

        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter out = new PrintWriter(new BufferedWriter(
                new OutputStreamWriter(socket.getOutputStream())), true);
        
        // Convert password from char[] to String
        String passwordStr = new String(password.getPassword());
        
        out.println("login");
        out.println(jicq.getText());
        out.println(passwordStr);

        String str = in.readLine();
        System.out.println("Login response: " + str);
        
        if (str == null) {
            JOptionPane.showMessageDialog(this, "No response from server", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else if (str.equals("false")) {
            JOptionPane.showMessageDialog(this, "Login failed: Invalid JICQ number or password", "Error",
                    JOptionPane.ERROR_MESSAGE);
        } else {
            this.dispose();
            int g = Integer.parseInt(jicq.getText());
            MainWin f2 = new MainWin(g, server, serport);
            f2.setVisible(true);
        }
        
        socket.close();
        
    } catch (IOException e1) {
        JOptionPane.showMessageDialog(this, 
            "Cannot connect to server: " + e1.getMessage() + 
            "\n\nPlease check:\n1. Server is running at " + server + ":" + serport + 
            "\n2. Your internet connection\n3. Firewall settings", 
            "Connection Error", JOptionPane.ERROR_MESSAGE);
        e1.printStackTrace();
    }
}

    void newuser_mouseClicked(MouseEvent e) {
        this.dispose();
        JDialog d = new Register(server, serport);
        d.pack();
        d.setLocationRelativeTo(this);
        d.setSize(400, 400);
        d.show();
    }

    void quit_mouseClicked(MouseEvent e) {
        System.exit(0);
    }

}