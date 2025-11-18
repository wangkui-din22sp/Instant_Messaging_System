package com.messaging;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class sunPanel extends JPanel {
	ImageIcon sun = new ImageIcon("sun.jpg");

	private int sun_smallh = sun.getIconWidth();

	private int sun_smallw = sun.getIconHeight();

	FlowLayout flowLayout1 = new FlowLayout();

	public void paintComponent(Graphics g) {

		super.paintComponents(g);
		Dimension size = getSize();
		for (int row = 0; row < size.height; row += sun_smallh)
			for (int col = 2; col < size.width; col += sun_smallw)
				sun.paintIcon(this, g, row, col);
	}
}