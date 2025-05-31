package org.timer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class AlertManager {
    private JFrame frame;
    private Timer blinkTimer;
    private Timer beepTimer;
    private boolean soundEnabled = true;

    public AlertManager(JFrame frame) {
        this.frame = frame;
    }

    public void setSoundEnabled(boolean enabled) {
        this.soundEnabled = enabled;
    }

    public void startAlerts() {
        startBlinking();
        startBeeping();
    }

    public void stopAlerts() {
        if (blinkTimer != null && blinkTimer.isRunning()) {
            blinkTimer.stop();
            frame.getContentPane().setBackground(Color.WHITE);
        }
        if (beepTimer != null && beepTimer.isRunning()) {
            beepTimer.stop();
        }
    }

    private void startBlinking() {
        blinkTimer = new Timer(200, new ActionListener() {
            boolean isWhite = true;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (isWhite) {
                    frame.getContentPane().setBackground(Color.RED);
                } else {
                    frame.getContentPane().setBackground(Color.WHITE);
                }
                isWhite = !isWhite;
            }
        });
        blinkTimer.start();
    }

    private void startBeeping() {
        beepTimer = new Timer(500, new ActionListener() {
            int beepCount = 0;

            @Override
            public void actionPerformed(ActionEvent e) {
                if (soundEnabled) {
                    Toolkit.getDefaultToolkit().beep();
                }
                beepCount++;
                if (beepCount >= 10) {
                    beepTimer.stop();
                    blinkTimer.stop();
                    frame.getContentPane().setBackground(Color.WHITE);
                }
            }
        });
        beepTimer.start();
    }
}