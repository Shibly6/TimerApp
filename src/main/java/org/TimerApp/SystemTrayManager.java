package org.TimerApp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class SystemTrayManager {
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private JFrame frame;

    public SystemTrayManager(JFrame frame) {
        this.frame = frame;
        if (SystemTray.isSupported()) {
            systemTray = SystemTray.getSystemTray();
            Image trayImage = new ImageIcon(getClass().getResource("/icon.png")).getImage()
                    .getScaledInstance(16, 16, Image.SCALE_SMOOTH);

            PopupMenu trayPopupMenu = new PopupMenu();
            MenuItem showItem = new MenuItem("Show");
            MenuItem exitItem = new MenuItem("Exit");

            showItem.addActionListener(e -> {
                frame.setVisible(true);
                frame.setState(Frame.NORMAL);
            });

            exitItem.addActionListener(e -> System.exit(0));

            trayPopupMenu.add(showItem);
            trayPopupMenu.addSeparator();
            trayPopupMenu.add(exitItem);

            trayIcon = new TrayIcon(trayImage, "Timer App", trayPopupMenu);
            trayIcon.setImageAutoSize(true);

            trayIcon.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    if (e.getClickCount() == 2) {
                        frame.setVisible(true);
                        frame.setState(Frame.NORMAL);
                    }
                }
            });

            try {
                systemTray.add(trayIcon);
            } catch (AWTException e) {
                System.err.println("TrayIcon could not be added.");
            }

            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    frame.setVisible(false);
                }
            });
        }
    }
}