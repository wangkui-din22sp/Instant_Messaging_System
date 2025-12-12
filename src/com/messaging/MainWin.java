package com.messaging;

import java.awt.AWTEvent;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
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
import java.net.SocketException;
import java.util.Vector;

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

//Main window
public class MainWin extends JFrame implements Runnable {
    JPanel contentPane;

    String filepath;

    String filename;

    String file;

    //Friend information
    Vector friendnames = new Vector();

    int friendnum;//friend number

    private String[] picsonline = new String[] { "1.jpg", "3.jpg", "5.jpg",
            "7.jpg" };

    private String[] picsoffline = new String[] { "2.jpg", "4.jpg", "6.jpg",
            "8.jpg" };

    Vector friendjicq = new Vector();

    Vector udpport = new Vector();

    Vector friendips = new Vector();

    Vector friendemail = new Vector();

    Vector friendsex = new Vector();

    Vector friendinfo = new Vector();

    Vector picno = new Vector();

    Vector status = new Vector();


    //Temporary friend information during update
    Vector tempname = new Vector();

    Vector tempjicq = new Vector();

    Vector tempip = new Vector();

    Vector temppic = new Vector();

    Vector tempstatus = new Vector();

    Vector whoaddmesip = new Vector();//get whoaddme as friend

    Vector tempemail = new Vector();

    Vector tempinfo = new Vector();

    int index;//get list index

    int index3;//get firiend onlineinfo

    int index4;//get message from info

    boolean fromunknow = false;

    //Friend search window
    FindFriend2 findf;

    JDialog hello = new JDialog();

    JDialog OneAddyou = new JDialog();

    JDialog DirectAdd = new JDialog();

    int tempgetjicq;//get the tempgetjicq
    /////////////////////////////friend info
    //Friend information

    ImageIcon icon1 = new ImageIcon("cab_small.gif");

    ImageIcon icon6 = new ImageIcon("sun_small.gif");

    ImageIcon icon3 = new ImageIcon("17.jpg");

    JButton jButton1 = new JButton();

    
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

    JLabel name = new JLabel();

    JTextField nametext = new JTextField();

    JLabel icq = new JLabel();

    JTextField icqno = new JTextField();

    JButton send = new JButton();

    JButton cancel = new JButton();

    JTextArea sendtext = new JTextArea();

    JList list;

    JLabel jLabel2 = new JLabel();

    FlowLayout flowLayout1 = new FlowLayout();

    JMenuBar mb = new JMenuBar();

    JMenu filemenu = new JMenu("File");

    JMenuItem exititem = new JMenuItem("Exit");

    JMenuItem find = new JMenuItem("Find Friend");
    JMenuItem direct = new JMenuItem("Direct Add");

    JMenuItem update = new JMenuItem("Update Friend List");

    JMenuItem online = new JMenuItem("Online Notification");

    JMenuItem myinfo = new JMenuItem("My Information");

    
    //Friend information
    Socket socket;

    BufferedReader in;

    PrintWriter out;

    DatagramPacket sendPacket, receivePacket;

    DatagramSocket sendSocket, receiveSocket;

    int udpPORT = 5001;

    int sendPort = 5000;

    String server;

    int serverport;

    byte array[] = new byte[512];

    Thread thread;

    int myjicq;

    String received;

    //Friend information
    JLabel jLabel3 = new JLabel();

    JTextField getfromname = new JTextField();

    JLabel jLabel4 = new JLabel();

    JTextField getfromjicq = new JTextField();

    JTextArea getinfo = new JTextArea();

    JButton getok = new JButton();

    String theip;

    
    JMenuItem delfriend = new JMenuItem();

    
    JLabel jLabel1 = new JLabel();

    JTextField helloname = new JTextField();

    JLabel jLabel5 = new JLabel();

    JTextField hellojicq = new JTextField();

    JLabel jLabel6 = new JLabel();

    JTextField helloemail = new JTextField();

    JLabel jLabel7 = new JLabel();

    JTextArea helloinfo = new JTextArea();

    JButton jButton3 = new JButton();

    JButton hellook = new JButton();

    JLabel jLabel8 = new JLabel();

    JLabel jLabel9 = new JLabel();

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

    

    /* Connect to server */
    public void ConnectServer(int myid) {
        try {
            socket = new Socket(InetAddress.getByName(server), serverport);

            in = new BufferedReader(new InputStreamReader(socket
                    .getInputStream()));
            out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    socket.getOutputStream())), true);

            
            //Retrieve friend information
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
        } catch (IOException e1) {
            System.out.println("false");
        }
        //Display friend information
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
        }//for
    }//connectto server
    
    //Temporary friend information during update

    public void run() {
        

        while (true) {
             System.out.println("UDP listener started on port " + udpPORT);
            try {
                
                for (int x = 0; x < 512; x++)
                    array[x] = ' ';
                // Receive data packet
            receivePacket = new DatagramPacket(array, array.length);
            receiveSocket.receive(receivePacket);
            byte[] data = receivePacket.getData();
            String infofromip = receivePacket.getAddress().getHostAddress()
                    .toString().trim();
            int receivedLength = receivePacket.getLength();
            index3 = 0;
            received = new String(data, 0, receivedLength);
            received.trim();
            
            System.out.println("Received " + receivedLength + " bytes from " + 
                               infofromip + ": " + received);

                String tempstr;
                int tx;
                //friend online
                if (received.substring(0, 6).equals("online")) {//Friend online notification
                    tempstr = received.substring(6).trim();
                    System.out.println("通知:" + tempstr + "上线了!");
                    tempgetjicq = Integer.parseInt(tempstr);
                    System.out.println("id jicq2" + tempgetjicq);
                    do {
                        tx = Integer
                                .parseInt(friendjicq.get(index3).toString());
                        System.out.println("通知好友" + tx + "上线了!");
                        if (tempgetjicq == tx)
                            break;
                        index3++;
                    } while (index3 < friendjicq.size());
                    friendips.setElementAt(infofromip, index3);
                    
                    DefaultListModel mm3 = (DefaultListModel) list.getModel();
                    int picid = Integer.parseInt(picno.get(index3).toString());
                    mm3.setElementAt(new Object[] { friendnames.get(index3),
                            new ImageIcon(picsonline[picid]) }, index3);
                }//end online
                
                //friend offline
                else if (received.substring(0, 7).equals("offline")) {//Friend offline notification
                    tempstr = received.substring(7).trim();
                    System.out.println("str" + tempstr);
                    tempgetjicq = Integer.parseInt(tempstr);
                    System.out.println("id" + tempgetjicq);
                    do {
                        tx = Integer
                                .parseInt(friendjicq.get(index3).toString());
                        System.out.println("tx" + tx);
                        if (tempgetjicq == tx)
                            break;
                        index3++;
                    } while (index3 < friendjicq.size());
                    infofromip = "null";
                    friendips.setElementAt(infofromip, index3);
                
                    System.out.println(index3);
                    DefaultListModel mm3 = (DefaultListModel) list.getModel();
                    int picid = Integer.parseInt(picno.get(index3).toString());
                    mm3.setElementAt(new Object[] { friendnames.get(index3),
                            new ImageIcon(picsoffline[picid]) }, index3);

                }//end friend offline
                //someone add me as friend
                else if (received.substring(0, 9).equals("oneaddyou")) {
    // Someone added me as a friend
    tempstr = received.substring(9).trim();
    System.out.println("Received add notification: " + tempstr);
    
    // FIX: Store the jicq in a final variable to avoid overwriting
    final int addingUserJicq = Integer.parseInt(tempstr);
    System.out.println("User " + addingUserJicq + " added you as a friend!");
    
    // Show notification in UI thread
    SwingUtilities.invokeLater(new Runnable() {
        public void run() {
            oneaddme.setText("用户 " + addingUserJicq + " 添加您为好友!");
            OneAddyou.setBounds(400, 300, 250, 200);
            OneAddyou.show();
        }
    });
    
} // end someone add me as friend
                else if (received.substring(0, 9).equals("readysend")) {
                    // else if (received.equals("readysend")) {
                    System.out.println(file);
                    SendFile sf = new SendFile(theip, file);
                    sf.fileClient();
                    System.out.println("Sending file to " + theip);
                
                } else if (received.substring(0, 12).equals("readyreceive")) {
                    FileDialog fdsave = new FileDialog(this, "Save File", 1);
                    fdsave.setVisible(true);
                    String dir = fdsave.getDirectory();
                    String name = received.substring(12);
                    fdsave.setFile(name);
                    String s = "readysend";
                    byte[] data2 = s.getBytes();
                    try {
                        sendPacket = new DatagramPacket(data2, s.length(),
                                InetAddress.getByName(infofromip), sendPort);
                        sendSocket.send(sendPacket);
                        System.out.println("Ready to send " + received);
                    } catch (Exception e2) {
                        System.out.println(e2.toString());
                    }

                    GetFile gf = new GetFile(dir, "receive");
                    gf.fileServer();
                } else {//Unknown sender
                    index4 = 0;
                    
                    do {
                        String friendip = friendips.get(index4).toString()
                                .trim();
                        if (infofromip.equals(friendip)) {
                            String nameinfo = friendnames.get(index4)
                                    .toString().trim();
                            JOptionPane.showMessageDialog(this, "来自" + nameinfo
                                    + "的消息", "ok",
                                    JOptionPane.INFORMATION_MESSAGE);
                            fromunknow = false;
                            break;
                        }//if
                        index4++;
                        if (index4 >= friendnames.size()) {
                            fromunknow = true;//Unknown sender
                            JOptionPane.showMessageDialog(this, "Unknown sender "
                                    + infofromip + " message", "ok",
                                    JOptionPane.INFORMATION_MESSAGE);
                        }
                    } while (index4 < friendnames.size());//while
                    System.out.println(index4);

                }
                ;

            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

    }//run end
    
    //Create UDP socket

    public void CreatUDP() {
        try {

            /** ************************************* */
            sendSocket = new DatagramSocket();
            System.out.println("Send socket created: " + sendSocket);
            receiveSocket = new DatagramSocket(udpPORT);
            System.out.println("Receive socket created: " + receiveSocket);
            /** ************************************* */

        } catch (SocketException se) {
            se.printStackTrace();
            System.out.println("Socket creation error");
        }
    }// creat udp end
    //main ****************

    public MainWin(int s, String sername, int serport) {//Constructor
        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        try {
            myjicq = s;
            server = sername;
            serverport = serport;
            jbInit();
            ConnectServer(myjicq);
            CreatUDP();
            findf = new FindFriend2(myjicq, server, serverport);
            findf.setBounds(200, 150, 300, 300);
            thread = new Thread(this);
            thread.start();
        }

        catch (Exception e) {
            e.printStackTrace();
        }
    }//end main*****

    /** Component initialization */
    private void jbInit() throws Exception {//Component initialization
// In jbInit() method of MainWin.java, add:
OneAddyou.getContentPane().setLayout(null);
OneAddyou.getContentPane().setBackground(new Color(88, 172, 165));
OneAddyou.setSize(250, 200);
OneAddyou.setResizable(false);

jLabel10.setText("好友请求");
jLabel10.setBounds(new Rectangle(7, 13, 143, 18));
oneaddme.setBounds(new Rectangle(7, 57, 247, 18));
addit.setText("接受");
addit.setBounds(new Rectangle(19, 124, 93, 29));
addit.addMouseListener(new java.awt.event.MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
        addit_mouseClicked(e);
    }
});
iknow.setText("知道了");
iknow.setBounds(new Rectangle(164, 124, 79, 29));
iknow.addMouseListener(new java.awt.event.MouseAdapter() {
    public void mouseClicked(MouseEvent e) {
        iknow_mouseClicked(e);
    }
});

// Add components to dialog
OneAddyou.getContentPane().add(jLabel10, null);
OneAddyou.getContentPane().add(oneaddme, null);
OneAddyou.getContentPane().add(addit, null);
OneAddyou.getContentPane().add(iknow, null);


        contentPane = (JPanel) this.getContentPane();
        contentPane.setLayout(flowLayout1);

        this.getContentPane().setBackground(new Color(205, 158, 253));
        this.setResizable(false);
        this.setSize(new Dimension(206, 420));
        this.setTitle("Instant Messaging System");
        this.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                this_mousePressed(e);
            }
        });

        ListModel model = new NameAndPicListModel(friendnames, picsonline);
        ListCellRenderer renderer = new NameAndPicListCellRenderer();
        list = new JList(model);
        list.setBackground(new Color(255, 255, 210));
        list.setAlignmentX((float) 1.0);
        list.setAlignmentY((float) 1.0);
        list.setCellRenderer(renderer);
        list.setVisibleRowCount(7);
        list.addMouseListener(new MainWin_list_mouseAdapter(this));
        list.setSize(380, 200);
        jButton1.setText("Add Friend");
        jButton1.setBorderPainted(false);
        jButton1.setContentAreaFilled(true);
        jButton1.setSize(180, 100);
        jButton1.setIcon(icon1);
        jButton1.setOpaque(false);
        jButton1.setPressedIcon(icon6);

        direct.setToolTipText("about");
        
        ok.setText("OK");
        ok.setBounds(new Rectangle(111, 89, 97, 29));
        ok.addMouseListener(new MainWin_ok_mouseAdapter(this));
        
        sendmessage.setText("Send Message");
        sendmessage
                .addMouseListener(new MainWin_sendmessage_mouseAdapter(this));
        getmessage.setText("Get Message");
        getmessage.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                getmessage_mousePressed(e);
            }
        });
        sendfile.setText("Send File");
        sendfile.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                sendfile_mousePressed(e);
            }
        });
        lookinfo.setText("View Info");
        lookinfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                lookinfo_mousePressed(e);
            }
        });
        
        Container dialogcon = about.getContentPane();
        about.setSize(200, 200);
        Container senddiapane = senddata.getContentPane();
        dialogcon.setLayout(null);
        dialogcon.setSize(100, 100);

    

        senddiapane.setLayout(null);
        name.setForeground(SystemColor.activeCaption);
        name.setText("Name");
        name.setBounds(new Rectangle(9, 44, 41, 18));
        nametext.setBounds(new Rectangle(52, 38, 90, 22));
        icq.setForeground(SystemColor.activeCaption);
        icq.setText("JAVA_ICQ");
        icq.setBounds(new Rectangle(163, 39, 64, 18));
        icqno.setBounds(new Rectangle(257, 37, 96, 22));
        
        send.setText("Send");
        send.setBounds(new Rectangle(39, 219, 110, 29));
        send.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                send_mouseClicked(e);
            }
        });
        senddiapane.setLayout(null);
    
        cancel.setText("Cancel");
        cancel.setBounds(new Rectangle(220, 219, 110, 29));
        cancel.addMouseListener(new MainWin_cancel_mouseAdapter(this));
        contentPane.setAlignmentX((float) 200.0);
        contentPane.setAlignmentY((float) 200.0);
        senddata.setResizable(false);
        senddata.getContentPane().setBackground(Color.lightGray);
        sendtext.setRows(10);
        sendtext.setMinimumSize(new Dimension(20, 10));
        sendtext.setMaximumSize(new Dimension(20, 10));
        sendtext.setBounds(new Rectangle(7, 71, 384, 141));
        jLabel2.setText("This is HG");
        jLabel2.setBounds(new Rectangle(20, 82, 89, 18));
        senddiapane.setBackground(new Color(58, 112, 165));
        
        getdata.getContentPane().setLayout(null);
        getdata.setSize(400, 300);
        jLabel3.setText("Name");
        jLabel3.setBounds(new Rectangle(14, 37, 41, 18));
        getfromname.setBounds(new Rectangle(56, 37, 90, 22));
        jLabel4.setText("JiCQ");
        jLabel4.setBounds(new Rectangle(164, 39, 41, 18));
        getfromjicq.setBounds(new Rectangle(224, 37, 104, 22));
        getinfo.setBounds(new Rectangle(18, 68, 325, 153));
        getok.setText("ok");
        getok.setBounds(new Rectangle(136, 240, 79, 29));
        getok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                getok_mouseClicked(e);
            }
        });
        getdata.getContentPane().setBackground(new Color(88, 112, 165));

        delfriend.setText("Delete Friend");
        delfriend.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                delfriend_mousePressed(e);
            }
        });

        hello.getContentPane().setLayout(null);
        jLabel1.setText("Name");
        jLabel1.setBounds(new Rectangle(11, 29, 41, 18));
        helloname.setBounds(new Rectangle(52, 27, 78, 22));
        jLabel5.setText("ICQ Number");
        jLabel5.setBounds(new Rectangle(145, 30, 61, 18));
        hellojicq.setBounds(new Rectangle(218, 28, 106, 22));
        jLabel6.setText("Email");
        jLabel6.setBounds(new Rectangle(11, 70, 100, 18));
        helloemail.setBounds(new Rectangle(80, 69, 138, 22));

        jLabel13.setText("Gender");
        jLabel13.setBounds(new Rectangle(220, 70, 51, 18));
        hellosex.setBounds(new Rectangle(266, 70, 61, 22));

        jLabel7.setText("Address");
        jLabel7.setBounds(new Rectangle(14, 106, 75, 18));
        helloinfo.setBounds(new Rectangle(13, 136, 301, 101));
        
        hellook.setText("OK");
        hellook.setBounds(new Rectangle(124, 245, 79, 29));
        hellook.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                hellook_mouseClicked(e);
            }
        });
        hello.getContentPane().setBackground(new Color(88, 172, 165));
        jLabel8.setText("Hello Information");
        jLabel8.setBounds(new Rectangle(14, 19, 196, 18));
        jLabel9.setText("Hello Information");
        jLabel9.setBounds(new Rectangle(12, 13, 186, 18));
        OneAddyou.getContentPane().setLayout(null);
        jLabel10.setText("Request Information");
        jLabel10.setBounds(new Rectangle(7, 13, 143, 18));
        oneaddme.setBounds(new Rectangle(7, 57, 247, 18));
        addit.setText("Add");
        addit.setBounds(new Rectangle(19, 124, 93, 29));
        addit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                addit_mouseClicked(e);
            }
        });
        iknow.setText("I Know");
        iknow.setBounds(new Rectangle(164, 124, 79, 29));
        iknow.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                iknow_mouseClicked(e);
            }
        });
        DirectAdd.getContentPane().setLayout(null);
        jLabel11.setText("Direct Add Friend");
        jLabel11.setBounds(new Rectangle(7, 19, 220, 18));
        jLabel12.setText("Friend ID");
        jLabel12.setBounds(new Rectangle(11, 58, 72, 18));
        friendid.setBounds(new Rectangle(83, 53, 118, 22));
        directaddok.setText("OK");
        directaddok.setBounds(new Rectangle(89, 109, 79, 29));
        directaddok.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                directaddok_mouseClicked(e);
            }
        });
        DirectAdd.setResizable(false);
        
        dialogcon.add(ok, null);
        dialogcon.add(jLabel2, null);
        about.setSize(100, 100);
        about.pack();
    
        mb.setOpaque(false);
        mb.add(new JLabel(new ImageIcon("smile.jpg")));
        mb.setBounds(new Rectangle(1, 21, 353, 166));
        mb.add(filemenu);
        contentPane.add(mb);
        contentPane.add(jButton1, null);
        contentPane.add(new JScrollPane(list));
        filemenu.setBounds(new Rectangle(320, 382, 189, 118));
        filemenu.add(find);
        filemenu.add(direct);
        filemenu.add(myinfo);
        filemenu.add(update);
        filemenu.add(online);
        filemenu.setOpaque(false);

        find.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                find_mousePressed(e);
            }
        });
        direct.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                direct_mousePressed(e);
            }
        });
        myinfo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                myinfo_mousePressed(e);
            }
        });
        online.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                online_mousePressed(e);
            }
        });

        update.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                update_mousePressed(e);
            }
        });
        
        mb.add(filemenu);
        contentPane.add(mb); // Add the menu bar to the content pane

        exititem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);

            }
        });

        jPopupMenu1.add(sendmessage);
        jPopupMenu1.add(getmessage);
        jPopupMenu1.add(sendfile);
        jPopupMenu1.add(lookinfo);
        jPopupMenu1.add(chatrecord);
        jPopupMenu1.add(delfriend);

        senddiapane.add(send, null);
        senddiapane.add(cancel, null);
        senddiapane.add(sendtext, null);
        senddiapane.add(name, null);
        senddiapane.add(nametext, null);
        senddiapane.add(icq, null);
        senddiapane.add(icqno, null);
        senddiapane.add(jLabel8, null);
        getdata.getContentPane().add(getinfo, null);
        getdata.getContentPane().add(getok, null);
        getdata.getContentPane().add(jLabel3, null);
        getdata.getContentPane().add(getfromname, null);
        getdata.getContentPane().add(jLabel4, null);
        getdata.getContentPane().add(getfromjicq, null);
        getdata.getContentPane().add(jLabel9, null);
        hello.getContentPane().add(jLabel1, null);
        hello.getContentPane().add(helloname, null);
        hello.getContentPane().add(jLabel5, null);
        hello.getContentPane().add(hellojicq, null);
        hello.getContentPane().add(jLabel6, null);
        hello.getContentPane().add(helloemail, null);
        hello.getContentPane().add(jLabel7, null);
        
        hello.getContentPane().add(jLabel13, null);
        hello.getContentPane().add(hellosex, null);
        hello.getContentPane().add(helloinfo, null);
        hello.getContentPane().add(jButton3, null);
        hello.getContentPane().add(hellook, null);
        OneAddyou.getContentPane().add(jLabel10, null);
        OneAddyou.getContentPane().add(oneaddme, null);
        OneAddyou.getContentPane().add(addit, null);
        OneAddyou.getContentPane().add(iknow, null);
        DirectAdd.getContentPane().add(jLabel11, null);
        DirectAdd.getContentPane().add(jLabel12, null);
        DirectAdd.getContentPane().add(friendid, null);
        DirectAdd.getContentPane().add(directaddok, null);
        senddata.pack();

    }

    protected void processWindowEvent(WindowEvent e) {//Handle window closing event
        super.processWindowEvent(e);
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            //tell who add me as friend offline
            try {
                String whoips;
                String s = "offline" + myjicq;
                s.trim();
                System.out.println(s);
                byte[] data = s.getBytes();
                for (int i = 0; i < whoaddmesip.size(); i++) {
                    whoips = whoaddmesip.get(i).toString().trim();
                    sendPacket = new DatagramPacket(data, s.length(),
                            InetAddress.getByName(whoips), sendPort);
                    sendSocket.send(sendPacket);//Notify friends who added me that I am offline
                }//for
            } catch (IOException e2) {
                sendtext.append(sendtext.getText());
                e2.printStackTrace();
            }
            //end offline

            //Send logout message to server
            out.println("logout");
            out.println(myjicq);
            
            System.exit(0);

        }
    }

    void this_mousePressed(MouseEvent e) {
        jButton1.setIcon(icon1);
    }

    void list_mouseClicked(MouseEvent e) {
        jPopupMenu1.show(this, e.getX() + 20, e.getY() + 20);
    }

    void direct_mousePressed(MouseEvent e) {//Direct Add Friend
        DirectAdd.setLocationRelativeTo(MainWin.this);
        DirectAdd.setSize(260, 200);
        DirectAdd.show();
        
    }

    void ok_mouseClicked(MouseEvent e) {
        about.dispose();
    }

    void cancel_mouseClicked(MouseEvent e) {
        senddata.dispose();
    }

    void sendmessage_mousePressed(MouseEvent e) {//Send Message
        senddata.setLocationRelativeTo(MainWin.this);
        senddata.setBounds(e.getX() + 50, e.getY() + 50, 400, 280);
        index = list.getSelectedIndex();
        System.out.println("Selected friend index" + index);
        nametext.setText(friendnames.get(index).toString());
        icqno.setText(friendjicq.get(index).toString());
        theip = friendips.get(index).toString();//ip address
        System.out.println("Selected friend's IP address:" + theip);
        senddata.show();

    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == find) {
            findf.show();
        }
    }

    void find_mousePressed(MouseEvent e) {//Show Find Friend Window

        findf.show();
    }//find

    void chatrecord_mouseClicked(MouseEvent e) {
        hello.show();
    }//Close chat record window

    void send_mouseClicked(MouseEvent e) {//Send Message
    
        try {
            String s = sendtext.getText().trim();
            System.out.println("Sending message:" + s);
            byte[] data = s.getBytes();
            System.out.println("Selected friend's IP address:" + theip);
            theip.trim();
            if (theip.equals("null") || theip.equals(" ") || theip.equals("0")) {
                JOptionPane.showMessageDialog(this, "Please select a friend to send a message", "ok",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                sendPacket = new DatagramPacket(data, s.length(), InetAddress
                        .getByName(theip), sendPort);
                sendSocket.send(sendPacket);
            }

        } catch (IOException e2) {
            sendtext.append(sendtext.getText());
            e2.printStackTrace();
        }
        sendtext.setText("");
        senddata.dispose();
        
    }//*******end send message

    void getmessage_mousePressed(MouseEvent e) {//Receive Message
        System.out.println("zhy receiving1...." + received.toString().trim());
        String message = received.toString().trim();
        System.out.println("zhy receiving2...." + received.trim());
        index = list.getSelectedIndex();
        if (index == index4)
            getinfo.append(message); //Display message from selected friend
        else
            getinfo.append(" "); //Display blank if not selected friend
            
        index = list.getSelectedIndex();
    
        getfromname.setText(friendnames.get(index).toString());
        getfromjicq.setText(friendjicq.get(index).toString());
        getdata.show();
    }

    void getok_mouseClicked(MouseEvent e) {//Close message window
        getinfo.setText(" ");
        getdata.dispose();
        received = " ";
    }

    //update friend info;
void update_mousePressed(MouseEvent e) {//Update friend information
    tempname = findf.tmpname;
    tempjicq = findf.tmpjicq;
    tempip = findf.tmpip;
    temppic = findf.tmppic;
    tempstatus = findf.tmpstatus;
    tempemail = findf.tmpemail;
    tempinfo = findf.tmpinfo;
    
    // Check if there are any new friends to update
    if (tempname.isEmpty()) {
        JOptionPane.showMessageDialog(this, 
            "没有找到新的好友可以更新", 
            "信息", 
            JOptionPane.INFORMATION_MESSAGE);
        return;
    }
    
    DefaultListModel mm2 = (DefaultListModel) list.getModel();
    int picid = 0;
    for (int p = 0; p < tempname.size(); p++) {
        // Ensure we have picture info for this friend
        if (p < temppic.size()) {
            picid = Integer.parseInt(temppic.get(p).toString());
        } else {
            picid = 0; // Default picture
        }
        
        if (tempstatus.get(p).equals("1")) {
            mm2.addElement(new Object[] { tempname.get(p),
                    new ImageIcon(picsonline[picid]) });
        } else {
            mm2.addElement(new Object[] { tempname.get(p),
                    new ImageIcon(picsoffline[picid]) });
        }
        
    }//for
    
    // Add to friend list vectors
    for (int k = 0; k < tempname.size(); k++) {
        friendnames.add(tempname.get(k));
        friendjicq.add(tempjicq.get(k));
        friendips.add(tempip.get(k));
        
        if (k < temppic.size()) {
            picno.add(temppic.get(k));
        } else {
            picno.add(new Integer(0));
        }
        
        if (k < tempstatus.size()) {
            status.add(tempstatus.get(k));
        } else {
            status.add("0"); // Default to offline
        }
        
        if (k < tempemail.size()) {
            friendemail.add(tempemail.get(k));
        } else {
            friendemail.add("");
        }
        
        if (k < tempinfo.size()) {
            friendinfo.add(tempinfo.get(k));
        } else {
            friendinfo.add("");
        }
    }//for
    
    // Clean temporary vectors
    findf.tmpip.clear();
    findf.tmpjicq.clear();
    findf.tmpname.clear();
    findf.tmppic.clear();
    findf.tmpstatus.clear();
    findf.tmpemail.clear();
    findf.tmpinfo.clear();
}

    //delete freind
    void delfriend_mousePressed(MouseEvent e) {//Delete friend
        out.println("delfriend");
        int index2;
        index2 = list.getSelectedIndex();

        out.println(friendjicq.get(index2).toString().trim());//the friendjicq
                                                              // to del
        out.println(myjicq);//my jicqno
        DefaultListModel mm = (DefaultListModel) list.getModel();
        mm.removeElementAt(index2);
        friendnames.removeElementAt(index2);
        friendips.removeElementAt(index2);
        friendjicq.removeElementAt(index2);
        picno.removeElementAt(index2);
        status.removeElementAt(index2);
        friendemail.removeElementAt(index2);
        friendinfo.removeElementAt(index2);
    }//delfriend
    //tell friend i am online

    void online_mousePressed(MouseEvent e) {
        out.println("getwhoaddme");
        out.println(myjicq);

        String whoip = " ";
        do {
            try {
                whoip = in.readLine().trim();
                if (whoip.equals("over"))
                    break;
                whoaddmesip.add(whoip);
            } catch (IOException s) {
                System.out.println("false getwhoaddme");
            }
        } while (!whoip.equals("over"));
        for (int i = 0; i < whoaddmesip.size(); i++) {
            System.out.println(whoaddmesip.get(i));
        }
        try {
            String whoips;
            String s = "online" + myjicq;
            s.trim();

            System.out.println(myjicq + " is online");
            byte[] data = s.getBytes();
            for (int i = 0; i < whoaddmesip.size(); i++) {
                whoips = whoaddmesip.get(i).toString().trim();
                sendPacket = new DatagramPacket(data, s.length(), InetAddress
                        .getByName(whoips), sendPort);
                sendSocket.send(sendPacket);
            }//for
        } catch (IOException e2) {
            sendtext.append(sendtext.getText());
            e2.printStackTrace();
            System.exit(1);
        }

    }//end tellfrienonline

    void myinfo_mousePressed(MouseEvent e) {//Show my information
        if (fromunknow) {
            String message = received.trim();
            getinfo.setText(" ");
            getinfo.append(message);
            getdata.show();
        }

    }

    void lookinfo_mousePressed(MouseEvent e) {//View friend's information
        hello.setLocationRelativeTo(MainWin.this);
        hello.setBounds(e.getX() + 50, e.getY() + 50, 380, 300);
        index = list.getSelectedIndex();
        helloname.setText(friendnames.get(index).toString());
        hellojicq.setText(friendjicq.get(index).toString());
        helloemail.setText(friendemail.get(index).toString());
        hellosex.setText(friendsex.get(index).toString());
        helloinfo.setText(friendinfo.get(index).toString().trim());
        hello.show();
    }

    void sendfile_mousePressed(MouseEvent e) {
        java.awt.FileDialog fd = new java.awt.FileDialog(this, "Select File to Send");
        fd.setVisible(true);
        filepath = fd.getDirectory();
        filename = fd.getFile().toString();
        file = filepath + filename;
        index = list.getSelectedIndex();
        theip = friendips.get(index).toString();//ip address
        if (theip.equals("null") || theip.equals(" ") || theip.equals("0")) {
            JOptionPane.showMessageDialog(this, "Please select a friend to send a file", "ok",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            String s = "readyreceive" + filename;
            byte[] data = s.getBytes();
            try {
                sendPacket = new DatagramPacket(data, s.length(), InetAddress
                        .getByName(theip), sendPort);
                sendSocket.send(sendPacket);
            } catch (Exception e2) {
                System.out.println(e2.toString());
            }
        }
    }

    void hellook_mouseClicked(MouseEvent e) {//Close friend's information window
        hello.dispose();
    }

    //add the one who add me as friend
    void addit_mouseClicked(MouseEvent e) { //Accept friend request, add friend information to friend list
    //Send friend request acceptance to server
        out.println("addnewfriend");
        out.println(tempgetjicq);
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
        } catch (IOException e1) {
            System.out.println("false");
        }
        int dddd = friendnames.size() - 1;
        DefaultListModel mm2 = (DefaultListModel) list.getModel();
        int picid;
        picid = Integer.parseInt(picno.get(dddd).toString());
        mm2.addElement(new Object[] { friendnames.get(dddd),
                new ImageIcon(picsonline[picid]) });
    }

    void iknow_mouseClicked(MouseEvent e) {
        OneAddyou.dispose();
    }

    void directaddok_mouseClicked(MouseEvent e) {//Directly add friend
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
        } catch (IOException e1) {
            System.out.println("false");
        }
        int dddd = friendnames.size() - 1;
        DefaultListModel mm2 = (DefaultListModel) list.getModel();
        int picid;
        
        picid = Integer.parseInt(picno.get(dddd).toString());
        if (status.get(dddd).equals("1")) {
            mm2.addElement(new Object[] { friendnames.get(dddd),
                    new ImageIcon(picsonline[picid]) });
        } else {
            mm2.addElement(new Object[] { friendnames.get(dddd),
                    new ImageIcon(picsoffline[picid]) });
        }
        DirectAdd.dispose();

    }//end directadd friend

}//end class MainWin