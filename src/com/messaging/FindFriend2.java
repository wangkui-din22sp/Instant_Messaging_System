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

public class FindFriend2 extends JFrame {//锟斤拷锟揭猴拷锟斤拷锟斤拷
	JLabel jLabel1 = new JLabel();

	JButton find2 = new JButton();

	JButton jButton1 = new JButton();

	JButton jButton2 = new JButton();

	JButton jButton3 = new JButton();

	JList list2;

	////////////////// /锟斤拷锟斤拷锟角猴拷锟窖碉拷锟截称ｏ拷锟皆憋拷锟斤拷锟较�
	Vector nickname = new Vector();

	Vector sex = new Vector();

	Vector place = new Vector();

	Vector jicq = new Vector();

	Vector ip = new Vector();

	Vector pic = new Vector();

	Vector status = new Vector();

	Vector emails = new Vector();

	Vector infos = new Vector();

	// 锟斤拷锟斤拷锟斤拷时锟斤拷锟斤拷锟斤拷训锟斤拷爻疲锟斤拷员锟斤拷锟斤拷息
	Vector tmpjicq = new Vector();//jicqid

	Vector tmpname = new Vector();//jicqname

	Vector tmpip = new Vector();//ip

	Vector tmppic = new Vector();//pic info

	Vector tmpstatus = new Vector();//status

	Vector tmpemail = new Vector();

	Vector tmpinfo = new Vector();

	// 锟斤拷锟铰达拷锟斤拷锟斤拷锟斤拷锟斤拷乇锟斤拷锟�
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

	public FindFriend2(int whoami, String host, int port) {//锟斤拷锟揭猴拷锟斤拷锟洁构锟届函锟斤拷
		enableEvents(AWTEvent.WINDOW_EVENT_MASK);
		try {
			serverhost = host;
			servport = port;
			myid = whoami;
			jbInit();
		} catch (Exception e) {
			e.printStackTrace();
		}//锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
		try {
			socket = new Socket(InetAddress.getByName(serverhost), servport);

			in = new BufferedReader(new InputStreamReader(socket
					.getInputStream()));
			out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					socket.getOutputStream())), true);
			sendSocket = new DatagramSocket();
		} catch (IOException e1) {
		}
	}

	private void jbInit() throws Exception {//锟斤拷锟斤拷锟角筹拷锟斤拷锟斤拷锟�
		jLabel1.setText("锟斤拷锟斤拷锟斤拷注锟斤拷锟斤拷锟斤拷锟�");
		jLabel1.setBounds(new Rectangle(11, 11, 211, 18));
		this.getContentPane().setLayout(new FlowLayout());
		find2.setText("锟斤拷锟斤拷");
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
		ListModel model = new FindListModel(nickname, sex, place, status);//锟叫憋拷模锟斤拷
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
		look.setText("锟介看锟斤拷锟斤拷");
		look.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				look_mousePressed(e);
			}
		});
		add.setText("锟斤拷为锟斤拷锟斤拷");
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
	}//锟斤拷锟斤拷锟角关闭憋拷锟斤拷锟斤拷

	protected void processWindowEvent(WindowEvent e) {
		super.processWindowEvent(e);
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			this.dispose();
			this.hide();
		}
	}

	// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷筒锟斤拷液锟斤拷锟斤拷锟斤拷锟�
	void find2_mouseClicked(MouseEvent e) {
		out.println("find");
		DefaultListModel mm = (DefaultListModel) list2.getModel();
		//////////////// /find friend info
		try {
			String s = " ";
			// 锟接凤拷锟斤拷锟斤拷锟斤拷取锟斤拷锟斤拷锟斤拷息
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
				status.add(in.readLine());
			} while (!s.equals("over"));
			//end find info
			// read their jicqno
			int theirjicq, picinfo, sta;
			for (int x = 0; x < nickname.size(); x++) {
				theirjicq = Integer.parseInt(in.readLine());

				jicq.add(new Integer(theirjicq));
				picinfo = Integer.parseInt(in.readLine());
				pic.add(new Integer(picinfo));
				sta = Integer.parseInt(in.readLine());

				status.add(new Integer(sta));

			}
			// 锟斤拷锟叫憋拷锟斤拷锟斤拷示
			for (int i = 0; i < nickname.size(); i++) {
				mm.addElement(new Object[] { nickname.get(i), sex.get(i),
						place.get(i), status.get(i) });
			}//for

		} catch (IOException e4) {
			System.out.println("false");
		}
	}

	// 锟斤拷示锟斤拷锟揭猴拷锟窖菜碉拷
	void list2_mousePressed(MouseEvent e) {
		findmenu.show(this, e.getX() + 20, e.getY() + 50);

	}

	//add frined
	// 锟斤拷锟铰斤拷锟斤拷锟接的猴拷锟窖存储锟斤拷锟斤拷时矢锟斤拷
	void add_mousePressed(MouseEvent e) {
		// add friend to database
		int dd;
		dd = list2.getSelectedIndex();

		// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷雍锟斤拷锟斤拷锟斤拷锟�
		out.println("addfriend");
		out.println(jicq.get(dd));
		out.println(myid);
		try { //锟斤拷锟铰革拷锟竭客伙拷锟斤拷锟斤拷锟轿�锟斤拷锟斤拷
			String thename = in.readLine();
			if (thename.equals("same"))
				JOptionPane.showMessageDialog(this, "锟斤拷锟矫伙拷锟窖撅拷锟斤拷锟斤拷暮锟斤拷锟�", "Warning",
						JOptionPane.INFORMATION_MESSAGE);

			if (thename.equals("add")) {
				tmpjicq.add(jicq.get(dd));
				tmpname.add(nickname.get(dd));
				tmpip.add(ip.get(dd));
				tmppic.add(pic.get(dd));
				tmpstatus.add(status.get(dd));
				tmpemail.add(emails.get(dd));
				tmpinfo.add(infos.get(dd));

				String whoips;
				String s = "oneaddyou" + myid;
				s.trim();
				System.out.println(s);
				byte[] data = s.getBytes();
				whoips = ip.get(dd).toString().trim();
				sendPacket = new DatagramPacket(data, s.length(), InetAddress
						.getByName(whoips), sendPort);
				sendSocket.send(sendPacket);

			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		//}catch(IOException df){};

	}

	void look_mousePressed(MouseEvent e) {
	}
	//add friend end
}
