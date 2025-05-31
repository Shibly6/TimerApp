package org.timer;

import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class TimerLogic {
    private JSpinner hoursSpinner, minutesSpinner, secondsSpinner;
    private JProgressBar progressBar;
    private Timer timer;
    private int hours, minutes, seconds;
    private boolean isRunning = false;
    private int totalTime;
    private AlertManager alertManager;
    private JFrame frame;
    private boolean shouldShutdownOnComplete = false;

    public TimerLogic(JSpinner hoursSpinner, JSpinner minutesSpinner, JSpinner secondsSpinner,
                      JProgressBar progressBar, AlertManager alertManager, JFrame frame) {
        this.hoursSpinner = hoursSpinner;
        this.minutesSpinner = minutesSpinner;
        this.secondsSpinner = secondsSpinner;
        this.progressBar = progressBar;
        this.alertManager = alertManager;
        this.frame = frame;
        setupTimer();
    }

    public void setShouldShutdownOnComplete(boolean shutdown) {
        this.shouldShutdownOnComplete = shutdown;
    }

    private void setupTimer() {
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (seconds > 0) {
                    seconds--;
                } else if (minutes > 0) {
                    minutes--;
                    seconds = 59;
                } else if (hours > 0) {
                    hours--;
                    minutes = 59;
                    seconds = 59;
                } else {
                    timer.stop();
                    isRunning = false;
                    alertManager.startAlerts();

                    if (shouldShutdownOnComplete) {
                        initiateShutdown();
                    }
                }
                hoursSpinner.setValue(hours);
                minutesSpinner.setValue(minutes);
                secondsSpinner.setValue(seconds);
                updateProgressBar();
            }
        });
    }

//    private void initiateShutdown() {
//        int[] countdown = {20};
//        JDialog shutdownDialog = new JDialog(frame, "System Shutdown", true);
//        shutdownDialog.setSize(300, 150);
//        shutdownDialog.setLayout(new BorderLayout());
//        shutdownDialog.setLocationRelativeTo(frame);
//
//        JLabel countdownLabel = new JLabel("System will shutdown in " + countdown[0] + " seconds", SwingConstants.CENTER);
//        JButton cancelButton = new JButton("Cancel Shutdown");
//
//        Timer shutdownTimer = new Timer(1000, shutdownEvent -> {
//            countdown[0]--;
//            countdownLabel.setText("System will shutdown in " + countdown[0] + " seconds");
//
//            if (countdown[0] <= 0) {
//                ((Timer)shutdownEvent.getSource()).stop();
//                shutdownDialog.dispose();
//                try {
//                    Runtime.getRuntime().exec("shutdown -s -t 0");
//                } catch (Exception ex) {
//                    JOptionPane.showMessageDialog(frame, "Failed to shutdown the PC.", "Error", JOptionPane.ERROR_MESSAGE);
//                }
//            }
//        });
//
//        cancelButton.addActionListener(shutdownEvent -> {
//            shutdownTimer.stop();
//            shutdownDialog.dispose();
//        });
//
//        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
//        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
//        contentPanel.add(countdownLabel, BorderLayout.CENTER);
//        contentPanel.add(cancelButton, BorderLayout.SOUTH);
//
//        shutdownDialog.add(contentPanel);
//        shutdownDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
//
//        shutdownTimer.start();
//        shutdownDialog.setVisible(true);
//    }

    private void initiateShutdown() {
        int[] countdown = {20};
        JDialog shutdownDialog = new JDialog(frame, "System Shutdown", true);
        shutdownDialog.setSize(350, 200); // Increased size
        shutdownDialog.setLayout(new BorderLayout());
        shutdownDialog.setLocationRelativeTo(frame);

        JLabel countdownLabel = new JLabel("System will shutdown in " + countdown[0] + " seconds", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Larger, clearer font

        JButton cancelButton = new JButton("Cancel Shutdown");
        cancelButton.setPreferredSize(new Dimension(200, 40)); // Larger button

        Timer shutdownTimer = new Timer(1000, shutdownEvent -> {
            countdown[0]--;
            countdownLabel.setText("System will shutdown in " + countdown[0] + " seconds");

            if (countdown[0] <= 0) {
                ((Timer)shutdownEvent.getSource()).stop();
                shutdownDialog.dispose();
                try {
                    // Use platform-specific shutdown command
                    String os = System.getProperty("os.name").toLowerCase();
                    if (os.contains("win")) {
                        Runtime.getRuntime().exec("shutdown /s /t 0");
                    } else if (os.contains("nix") || os.contains("mac")) {
                        Runtime.getRuntime().exec("shutdown -h now");
                    } else {
                        JOptionPane.showMessageDialog(frame, "Shutdown not supported on this OS.", "Error", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(frame, "Failed to shutdown the PC: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        cancelButton.addActionListener(shutdownEvent -> {
            shutdownTimer.stop();
            shutdownDialog.dispose();
        });

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(countdownLabel, BorderLayout.CENTER);
        contentPanel.add(cancelButton, BorderLayout.SOUTH);

        shutdownDialog.add(contentPanel);
        shutdownDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        shutdownTimer.start();
        shutdownDialog.setVisible(true);
    }
    public void startTimer() {
        if (!isRunning) {
            hours = (int) hoursSpinner.getValue();
            minutes = (int) minutesSpinner.getValue();
            seconds = (int) secondsSpinner.getValue();
            totalTime = hours * 3600 + minutes * 60 + seconds;
            if (totalTime > 0) {
                isRunning = true;
                timer.start();
            }
        }
    }

    public void pauseTimer() {
        if (isRunning) {
            timer.stop();
            isRunning = false;
        }
    }

    public void resetTimer() {
        timer.stop();
        isRunning = false;
        hours = 0;
        minutes = 0;
        seconds = 0;
        totalTime = 0;
        hoursSpinner.setValue(0);
        minutesSpinner.setValue(0);
        secondsSpinner.setValue(0);
        progressBar.setValue(0);
        alertManager.stopAlerts();
    }

    private void updateProgressBar() {
        int elapsed = totalTime - (hours * 3600 + minutes * 60 + seconds);
        int progress = (int) (((double) elapsed / totalTime) * 100);
        progressBar.setValue(progress);
    }
}