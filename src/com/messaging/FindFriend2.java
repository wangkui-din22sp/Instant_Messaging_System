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

	//////////////// //
	JPopupMenu findmenu = new JPopupMenu();

	JMenuItem look = new JMenuItem();

	JMenuItem add = new JMenuItem();

	public FindFriend2(int whoami, String host, int port) {//
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			serverhost = host;
			servport = port;
			myid = whoami;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}//
		try {
			socket = new Socket(InetAddress.getByName(serverhost), servport);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
			sendSocket = new DatagramSocket();
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
        } while (!s.equals("over"));
        
        // read their jicqno - second set of data
        int theirjicq, picinfo;
        String sta;
        for (int x = 0; x < nickname.size(); x++) {
            theirjicq = Integer.parseInt(in.readLine());
            jicq.add(theirjicq);
            
            picinfo = Integer.parseInt(in.readLine());
            pic.add(picinfo);
            
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
void add_mousePressed(MouseEvent e) {
    int dd = list2.getSelectedIndex();
    
    // Check if a friend is actually selected
    if (dd == -1) {
        JOptionPane.showMessageDialog(this, "请先选择一个好友!", "提示", 
                JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Check if vectors have data at the selected index
    if (dd >= jicq.size() || dd >= nickname.size() || dd >= ip.size()) {
        JOptionPane.showMessageDialog(this, "数据错误，请重新搜索!", "错误", 
                JOptionPane.ERROR_MESSAGE);
        return;
    }

    // 添加好友到数据库
    out.println("addfriend");
    out.println(jicq.get(dd));
    out.println(myid);
    
    try {
        String thename = in.readLine();
        if (thename.equals("same")) {
            JOptionPane.showMessageDialog(this, "好友已存在", "提示",
                    JOptionPane.INFORMATION_MESSAGE);
        } else if (thename.equals("add")) {
            // Add to temporary vectors
            tmpjicq.add(jicq.get(dd));
            tmpname.add(nickname.get(dd));
            tmpip.add(ip.get(dd));
            tmppic.add(pic.get(dd));
            tmpemail.add(emails.get(dd));
            tmpinfo.add(infos.get(dd));
            
            // Send notification to the added friend
            String whoips = ip.get(dd).toString().trim();
            String s = "oneaddyou" + myid;
            byte[] data = s.getBytes();
            sendPacket = new DatagramPacket(data, data.length, 
                    InetAddress.getByName(whoips), sendPort);
            sendSocket.send(sendPacket);
            
            JOptionPane.showMessageDialog(this, "好友添加成功!", "成功",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    } catch (IOException e2) {
        JOptionPane.showMessageDialog(this, "添加好友失败: " + e2.getMessage(), 
                "错误", JOptionPane.ERROR_MESSAGE);
        e2.printStackTrace();
    }
}

void look_mousePressed(MouseEvent e) {
    int selectedIndex = list2.getSelectedIndex();
    
    // Check if a friend is actually selected
    if (selectedIndex == -1) {
        JOptionPane.showMessageDialog(this, "请先选择一个好友!", "提示", 
                JOptionPane.WARNING_MESSAGE);
        return;
    }
    
    // Check if vectors have data at the selected index
    if (selectedIndex >= nickname.size() || selectedIndex >= sex.size() || 
        selectedIndex >= place.size() || selectedIndex >= emails.size() || 
        selectedIndex >= infos.size()) {
        JOptionPane.showMessageDialog(this, "数据错误，无法显示好友信息!", "错误", 
                JOptionPane.ERROR_MESSAGE);
        return;
    }
    
    // Get all the friend information
    String friendName = (String) nickname.get(selectedIndex);
    String friendSex = (String) sex.get(selectedIndex);
    String friendPlace = (String) place.get(selectedIndex);
    String friendEmail = (String) emails.get(selectedIndex);
    String friendInfo = (String) infos.get(selectedIndex);
    String friendStatus = (String) status.get(selectedIndex);
    Integer friendJicq = (Integer) jicq.get(selectedIndex);
    
    // Create a detailed information message
    String infoMessage = 
        "好友详细信息:\n\n" +
        "昵称: " + friendName + "\n" +
        "JICQ号码: " + friendJicq + "\n" +
        "性别: " + friendSex + "\n" +
        "地区: " + friendPlace + "\n" +
        "邮箱: " + friendEmail + "\n" +
        "状态: " + (friendStatus.equals("1") ? "在线" : "离线") + "\n" +
        "个人简介: " + friendInfo;
    
    // Display the information in a dialog
    JOptionPane.showMessageDialog(this, infoMessage, "好友信息 - " + friendName, 
            JOptionPane.INFORMATION_MESSAGE);
}
	//add friend end
}
