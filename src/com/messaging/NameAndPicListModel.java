package com.messaging;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;

//List model with names and pictures
public class NameAndPicListModel extends DefaultListModel {
    public NameAndPicListModel(Vector friendnames, String[] pics) {
        for (int i = 0; i < friendnames.size(); ++i) {
            addElement(new Object[] { friendnames.get(i),
                    new ImageIcon(pics[i]) });
        }
    }

    public String getName(Object object) {
        Object[] array = (Object[]) object;
        return (String) array[0];
    }

    public Icon getIcon(Object object) {
        Object[] array = (Object[]) object;
        return (Icon) array[1];
    }
}

class NameAndPicListCellRenderer extends JLabel implements ListCellRenderer {
    private Border lineBorder = BorderFactory.createLineBorder(Color.red, 2),
            emptyBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);

    public NameAndPicListCellRenderer() {
        setOpaque(true);
    }

    public Component getListCellRendererComponent(JList list, Object value,
            int index, boolean isSelected, boolean cellHasFocus) {
        NameAndPicListModel model = (NameAndPicListModel) list.getModel();
        setText(model.getName(value));
        setIcon(model.getIcon(value));
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

class MainWin_list_mouseAdapter extends java.awt.event.MouseAdapter {
    MainWin adaptee;

    MainWin_list_mouseAdapter(MainWin adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.list_mouseClicked(e);
    }
}

class MainWin_direct_mouseAdapter extends java.awt.event.MouseAdapter {
    MainWin adaptee;

    MainWin_direct_mouseAdapter(MainWin adaptee) {
        this.adaptee = adaptee;
    }
    
}

class MainWin_ok_mouseAdapter extends java.awt.event.MouseAdapter {
    MainWin adaptee;

    MainWin_ok_mouseAdapter(MainWin adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.ok_mouseClicked(e);
    }
}

class MainWin_sendmessage_mouseAdapter extends java.awt.event.MouseAdapter {
    MainWin adaptee;

    MainWin_sendmessage_mouseAdapter(MainWin adaptee) {
        this.adaptee = adaptee;
    }

    public void mousePressed(MouseEvent e) {
        adaptee.sendmessage_mousePressed(e);
    }
}

class MainWin_cancel_mouseAdapter extends java.awt.event.MouseAdapter {
    MainWin adaptee;

    MainWin_cancel_mouseAdapter(MainWin adaptee) {
        this.adaptee = adaptee;
    }

    public void mouseClicked(MouseEvent e) {
        adaptee.cancel_mouseClicked(e);
    }
}