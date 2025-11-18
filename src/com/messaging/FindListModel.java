package com.messaging;

import java.awt.Color;
import java.awt.Component;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

//锟斤拷锟斤拷锟斤拷展DefaultListModel锟洁建锟斤拷锟叫憋拷
public class FindListModel extends DefaultListModel {
	public FindListModel(Vector nickname, Vector sex, Vector place,
			Vector Status) {
		for (int i = 0; i < nickname.size(); ++i) {
			addElement(new Object[] { nickname.get(i), sex.get(i),
					place.get(i), Status.get(i) });
		}
	}

	public String getName(Object object) {
		Object[] array = (Object[]) object;
		return (String) array[0];
	}

	public String getSex(Object object) {
		Object[] array = (Object[]) object;
		return (String) array[1];
	}

	public String getPlace(Object object) {
		Object[] array = (Object[]) object;
		return (String) array[2];
	}

	public String getStatus(Object object) {
		Object[] array = (Object[]) object;
		return (String) array[3];
	}
}

class FindListCellRenderer extends JLabel implements ListCellRenderer//锟斤拷锟斤拷锟角达拷锟斤拷锟叫憋拷锟斤拷染
{
	private Border lineBorder = BorderFactory.createLineBorder(Color.red, 2),
			emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

	public FindListCellRenderer() {
		setOpaque(true);
	}

	public Component getListCellRendererComponent(JList list, Object value,
			int index, boolean isSelected, boolean cellHasFocus) {
		FindListModel model = (FindListModel) list.getModel();
		setText(model.getName(value) + "  " + model.getSex(value) + "  "
				+ model.getPlace(value) + "  " + model.getStatus(value));
		if (isSelected) {
			setForeground(list.getSelectionForeground());
			setBackground(list.getSelectionBackground());
		} else {
			setForeground(list.getForeground());
			setBackground(list.getBackground());
		}
		if (cellHasFocus)
			setBorder(lineBorder);
		else
			setBorder(emptyBorder);
		return this;
	}
}