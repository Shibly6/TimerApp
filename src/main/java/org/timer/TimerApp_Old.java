package org.timer;


import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.formdev.flatlaf.FlatLightLaf;

public class TimerApp_Old {
    private JFrame frame;
    private JButton startButton, pauseButton, resetButton;
    private Timer timer;
    private int hours, minutes, seconds;
    private boolean isRunning = false;
    private JProgressBar progressBar;
    private int totalTime;
    private boolean isAlwaysOnTop = false;
    private boolean soundEnabled = true;
    private Timer idleTimer;
    private static final float TRANSPARENT_OPACITY = 0.7f;
    private static final float NORMAL_OPACITY = 1.0f;
    private boolean shouldShutdownOnComplete = false;
    private Timer shutdownTimer;
    private JDialog shutdownDialog;
    private Timer blinkTimer;
    private Timer beepTimer;
    private final int FRAME_WIDTH = 256;
    private final int FRAME_HEIGHT = 150;
    private JSpinner hoursSpinner, minutesSpinner, secondsSpinner;
    private TrayIcon trayIcon;
    private SystemTray systemTray;
    private JPanel mainPanel;

    public TimerApp_Old() {
        FlatLightLaf.setup();

        frame = new JFrame("Timer App");
        frame.setUndecorated(true);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add shadow and rounded corners
        frame.setBackground(new Color(0, 0, 0, 0));
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Paint shadow
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 30, 30);

                // Paint panel background
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(6, 6, getWidth() - 10, getHeight() - 10, 15, 15);
                g2.dispose();
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setOpaque(false);
        frame.setContentPane(mainPanel);

        // Add window state listener to handle display changes
        frame.addWindowStateListener(new WindowStateListener() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                updateFrameSize();
            }
        });

        // Add component listener to handle display transitions
        frame.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentMoved(ComponentEvent e) {
                updateFrameSize();
            }

            @Override
            public void componentResized(ComponentEvent e) {
                updateFrameSize();
            }
        });

        setApplicationIcon();

        frame.addMouseListener(new MouseAdapter() {
            private Point mouseClickPoint;
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME = 300;

            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint();
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime <= DOUBLE_CLICK_TIME) {
                    stopAlerts();
                }
                lastClickTime = currentTime;

                resetIdleTimer();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resetIdleTimer();
                updateFrameSize();
            }
        });

        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                resetIdleTimer();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                Point currentPoint = e.getLocationOnScreen();
                frame.setLocation(currentPoint.x, currentPoint.y);
                resetIdleTimer();
                updateFrameSize();
            }
        });

        setupSystemTray();
        initializeComponents();
        setupTimerLogic();
        setupButtonActions();
        addComponentsToFrame();
        setupIdleTimer();
        createStartupShortcut();

        frame.setVisible(true);
        updateFrameSize();
    }

    private void initializeComponents() {
        Font inputFont = new Font("Roboto Mono", Font.BOLD, 23);
        Font buttonFont = new Font("Roboto Mono", Font.BOLD, 10);
        Font progressBarFont = new Font("Roboto Mono", Font.BOLD, 9);

        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        // Configure spinners with wider width for two digits
        configureSpinner(hoursSpinner, inputFont, 12, 23, 75, 35);
        configureSpinner(minutesSpinner, inputFont, 90, 23, 75, 35);
        configureSpinner(secondsSpinner, inputFont, 168, 23, 75, 35);



        // Adjust JSpinner text field properties
        configureSpinnerTextField(hoursSpinner);
        configureSpinnerTextField(minutesSpinner);
        configureSpinnerTextField(secondsSpinner);


        // Configure buttons
        startButton = createButton("START", buttonFont, new Color(0, 200, 0), Color.WHITE, 15, 69, 69, 23);
        pauseButton = createButton("PAUSE", buttonFont, Color.LIGHT_GRAY, Color.BLACK, 93, 69, 69, 23);
        resetButton = createButton("RESET", buttonFont, new Color(200, 0, 0), Color.WHITE, 170, 69, 69, 23);

        // Configure progress bar
        progressBar = new JProgressBar();
        progressBar.setBounds(12, 104, 230, 23);
        progressBar.setFont(progressBarFont);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 120, 255));
    }
    private void configureSpinnerTextField(JSpinner spinner) {
        JSpinner.DefaultEditor editor = (JSpinner.DefaultEditor) spinner.getEditor();
        JTextField textField = editor.getTextField();
        textField.setHorizontalAlignment(JTextField.CENTER);
        textField.setFont(spinner.getFont());
        textField.setEditable(false);
    }


    private void configureSpinner(JSpinner spinner, Font font, int x, int y, int width, int height) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
        spinner.setFont(font);
        spinner.setBounds(x, y, width, height);
    }
    private void setupSystemTray() {
        if (!SystemTray.isSupported()) {
            System.out.println("SystemTray is not supported");
            return;
        }

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

        // Modify frame's close operation to minimize to tray instead of exit
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                frame.setVisible(false);
            }
        });
    }

    private void createStartupShortcut() {
        String username = System.getProperty("user.name");
        Path startupPath = Paths.get("C:", "Users", username, "AppData", "Roaming",
                "Microsoft", "Windows", "Start Menu", "Programs", "Startup");
        Path shortcutPath = startupPath.resolve("TimerApp.bat");

        try {
            String jarPath = new File(TimerApp_Old.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI()).getPath();
            String batchContent = "@echo off\n" +
                    "start javaw -jar \"" + jarPath + "\"";

            Files.write(shortcutPath, batchContent.getBytes());
        } catch (Exception e) {
            System.err.println("Error creating startup shortcut: " + e.getMessage());
        }
    }

    private JButton createButton(String text, Font font, Color bgColor, Color fgColor, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void setupTimerLogic() {
        timer = new Timer(1000, e -> {
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
                startBlinkingAndBeeping();
            }
            hoursSpinner.setValue(hours);
            minutesSpinner.setValue(minutes);
            secondsSpinner.setValue(seconds);
            updateProgressBar();
        });
    }

    private void setupButtonActions() {
        startButton.addActionListener(e -> {
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
        });

        pauseButton.addActionListener(e -> {
            if (isRunning) {
                timer.stop();
                isRunning = false;
            }
        });

        resetButton.addActionListener(e -> {
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
            stopAlerts();
            frame.getContentPane().setBackground(Color.WHITE);
        });
    }

    private void addComponentsToFrame() {
        frame.add(hoursSpinner);
        frame.add(minutesSpinner);
        frame.add(secondsSpinner);
        frame.add(startButton);
        frame.add(pauseButton);
        frame.add(resetButton);
        frame.add(progressBar);
    }

    private void updateFrameSize() {
        SwingUtilities.invokeLater(() -> {
            GraphicsConfiguration gc = frame.getGraphicsConfiguration();
            if (gc != null) {
                Rectangle bounds = gc.getBounds();
                double scalingFactor = gc.getDefaultTransform().getScaleX();

                int scaledWidth = (int)(FRAME_WIDTH * scalingFactor);
                int scaledHeight = (int)(FRAME_HEIGHT * scalingFactor);

                Point loc = frame.getLocation();
                if (loc.x + scaledWidth > bounds.x + bounds.width) {
                    loc.x = bounds.x + bounds.width - scaledWidth;
                }
                if (loc.y + scaledHeight > bounds.y + bounds.height) {
                    loc.y = bounds.y + bounds.height - scaledHeight;
                }

                frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
                frame.setLocation(loc);
                frame.revalidate();
                frame.repaint();
            }
        });
    }

    private void updateProgressBar() {
        int elapsed = totalTime - (hours * 3600 + minutes * 60 + seconds);
        int progress = (int) (((double) elapsed / totalTime) * 100);
        progressBar.setValue(progress);
    }

    private void startBlinkingAndBeeping() {
        if (shouldShutdownOnComplete) {
            initiateShutdown();
        } else {
            startVisualAndAudioAlerts();
        }
    }

    private void stopAlerts() {
        if (blinkTimer != null && blinkTimer.isRunning()) {
            blinkTimer.stop();
            frame.getContentPane().setBackground(Color.WHITE);
        }
        if (beepTimer != null && beepTimer.isRunning()) {
            beepTimer.stop();
        }
    }

    private void startVisualAndAudioAlerts() {
        blinkTimer = new Timer(200, null);
        blinkTimer.addActionListener(new ActionListener() {
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

        beepTimer = new Timer(500, null);
        beepTimer.addActionListener(new ActionListener() {
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

        blinkTimer.start();
        beepTimer.start();
    }

    private void initiateShutdown() {
        int[] countdown = {20};
        shutdownDialog = new JDialog(frame, "System Shutdown", true);
        shutdownDialog.setSize(350, 200); // Increased size to prevent text cropping
        shutdownDialog.setLayout(new BorderLayout());
        shutdownDialog.setLocationRelativeTo(frame);

        JLabel countdownLabel = new JLabel("System will shutdown in " + countdown[0] + " seconds", SwingConstants.CENTER);
        countdownLabel.setFont(new Font("Arial", Font.BOLD, 14)); // Larger, clearer font

        JButton cancelButton = new JButton("Cancel Shutdown");
        cancelButton.setPreferredSize(new Dimension(200, 40)); // Larger button

        cancelButton.addActionListener(e -> {
            if (shutdownTimer != null) {
                shutdownTimer.stop();
            }
            shutdownDialog.dispose();
            frame.getContentPane().setBackground(Color.WHITE);
        });

        shutdownTimer = new Timer(1000, e -> {
            countdown[0]--;
            countdownLabel.setText("System will shutdown in " + countdown[0] + " seconds");

            if (countdown[0] <= 0) {
                shutdownTimer.stop();
                shutdownDialog.dispose();
                try {
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

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        contentPanel.add(countdownLabel, BorderLayout.CENTER);
        contentPanel.add(cancelButton, BorderLayout.SOUTH);

        shutdownDialog.add(contentPanel);
        shutdownDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        shutdownTimer.start();
        shutdownDialog.setVisible(true);
    }

    private void setupIdleTimer() {
        idleTimer = new Timer(5000, e -> frame.setOpacity(TRANSPARENT_OPACITY));
        idleTimer.setRepeats(false);
        resetIdleTimer();
    }

    private void resetIdleTimer() {
        frame.setOpacity(NORMAL_OPACITY);
        idleTimer.restart();
    }

    private void setApplicationIcon() {
        try {
            Image icon = new ImageIcon(getClass().getResource("/icon.png")).getImage();
            frame.setIconImage(icon);
        } catch (Exception e) {
            System.err.println("Error setting PNG icon: " + e.getMessage());
        }
    }

    private void showContextMenu(MouseEvent e) {
        JPopupMenu contextMenu = new JPopupMenu();

        JMenuItem minimizeToTrayItem = new JMenuItem("Minimize to Tray");
        minimizeToTrayItem.addActionListener(event -> {
            frame.setVisible(false);
        });

        JCheckBoxMenuItem alwaysOnTopMenuItem = new JCheckBoxMenuItem("Toggle Always On Top");
        alwaysOnTopMenuItem.setState(isAlwaysOnTop);
        alwaysOnTopMenuItem.addActionListener(event -> {
            isAlwaysOnTop = !isAlwaysOnTop;
            frame.setAlwaysOnTop(isAlwaysOnTop);
            alwaysOnTopMenuItem.setState(isAlwaysOnTop);
        });

        JCheckBoxMenuItem soundMenuItem = new JCheckBoxMenuItem("Toggle Sound");
        soundMenuItem.setState(soundEnabled);
        soundMenuItem.addActionListener(event -> {
            soundEnabled = !soundEnabled;
            soundMenuItem.setState(soundEnabled);
        });

        JCheckBoxMenuItem shutdownOnCompleteMenuItem = new JCheckBoxMenuItem("Shutdown on Timer Complete");
        shutdownOnCompleteMenuItem.setState(shouldShutdownOnComplete);
        shutdownOnCompleteMenuItem.addActionListener(event -> {
            shouldShutdownOnComplete = !shouldShutdownOnComplete;
            shutdownOnCompleteMenuItem.setState(shouldShutdownOnComplete);
            if (shouldShutdownOnComplete) {
                JOptionPane.showMessageDialog(frame,
                        "PC will shutdown when timer completes.",
                        "Shutdown Scheduled",
                        JOptionPane.INFORMATION_MESSAGE);
            }
        });

        contextMenu.add(minimizeToTrayItem);
        contextMenu.addSeparator();
        contextMenu.add(alwaysOnTopMenuItem);
        contextMenu.add(soundMenuItem);
        contextMenu.add(shutdownOnCompleteMenuItem);
        contextMenu.show(frame, e.getX(), e.getY());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerApp_Old::new);
    }
}