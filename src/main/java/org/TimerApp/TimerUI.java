package org.TimerApp;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TimerUI {
    private final int FRAME_WIDTH = 260;
    private final int FRAME_HEIGHT = 150;
    private JFrame frame;
    private JButton startButton, pauseButton, resetButton;
    private JProgressBar progressBar;
    private JSpinner hoursSpinner, minutesSpinner, secondsSpinner;
    private TimerLogic timerLogic;
    private SystemTrayManager systemTrayManager;
    private AlertManager alertManager;
    private boolean isAlwaysOnTop = false;
    private boolean soundEnabled = true;
    private Timer idleTimer;
    private static final float TRANSPARENT_OPACITY = 0.7f;
    private static final float NORMAL_OPACITY = 1.0f;
    private Point mouseClickPoint;
    private boolean shouldShutdownOnComplete = false;


    public TimerUI() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception ex) {
            System.err.println("Failed to initialize LaF");
        }
        initializeFrame();
        initializeComponents();
        setupSystemTray();
        setupTimerLogic();
        setupButtonActions();
        setupMouseListeners();
        setupIdleTimer();
        addComponentsToFrame();
        setApplicationIcon();
        AppUtilities.createStartupShortcut();
        frame.setVisible(true);
    }

    private void setupMouseListeners() {
        frame.addMouseListener(new MouseAdapter() {
            private long lastClickTime = 0;
            private static final long DOUBLE_CLICK_TIME = 300;

            @Override
            public void mousePressed(MouseEvent e) {
                mouseClickPoint = e.getPoint(); // Store the mouse position when pressed
                if (SwingUtilities.isRightMouseButton(e)) {
                    showContextMenu(e);
                }

                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime <= DOUBLE_CLICK_TIME) {
                    alertManager.stopAlerts();
                }
                lastClickTime = currentTime;

                resetIdleTimer();
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                resetIdleTimer();
            }
        });

        frame.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                resetIdleTimer();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseClickPoint != null) { // Check if we have a valid click point
                    Point currentPoint = e.getLocationOnScreen();
                    frame.setLocation(currentPoint.x - mouseClickPoint.x,
                            currentPoint.y - mouseClickPoint.y);
                    resetIdleTimer();
                }
            }
        });
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

    private void showContextMenu(MouseEvent e) {
        JPopupMenu contextMenu = new JPopupMenu();
        //contextMenu.setPreferredSize(new Dimension(200, -1)); // Set width to 200px, height auto (-1)

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
            alertManager.setSoundEnabled(soundEnabled);
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
        contextMenu.show(frame, e.getX(), e.getY());
        contextMenu.add(shutdownOnCompleteMenuItem);

    }



    private void initializeFrame() {
        frame = new JFrame("Timer App");
        frame.setUndecorated(true);
        frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Create main panel with custom painting
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
                g2.fillRoundRect(0, 0, getWidth() - 6, getHeight() - 6, 15, 15);
                g2.dispose();
            }
        };
        mainPanel.setLayout(null);
        mainPanel.setOpaque(false);
        frame.setContentPane(mainPanel);
        frame.setBackground(new Color(0, 0, 0, 0));
    }

    private void setApplicationIcon() {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
            if (icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                frame.setIconImage(icon.getImage());
            } else {
                System.err.println("Failed to load icon: icon.png not found in resources");
            }
        } catch (Exception e) {
            System.err.println("Error setting application icon: " + e.getMessage());
        }
    }

    private void initializeComponents() {
        Font inputFont = new Font("Roboto Mono", Font.BOLD, 23);
        Font buttonFont = new Font("Roboto Mono", Font.BOLD, 10);
        Font progressBarFont = new Font("Roboto Mono", Font.BOLD, 9);

        // Initialize spinners
        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        // Configure spinners
        configureSpinner(hoursSpinner, inputFont, 12, 23, 75, 35);
        configureSpinner(minutesSpinner, inputFont, 90, 23, 75, 35);
        configureSpinner(secondsSpinner, inputFont, 168, 23, 75, 35);

        // Configure spinners' text fields
        configureSpinnerTextField(hoursSpinner);
        configureSpinnerTextField(minutesSpinner);
        configureSpinnerTextField(secondsSpinner);

        // Create and configure buttons
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

        // Add minimum width to properly display two digits
        Dimension prefSize = textField.getPreferredSize();
        prefSize.width = Math.max(prefSize.width, 60);
        textField.setPreferredSize(prefSize);
    }

    private void configureSpinner(JSpinner spinner, Font font, int x, int y, int width, int height) {
        spinner.setFont(font);
        spinner.setBounds(x, y, width, height);
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
    }

    private JButton createButton(String text, Font font, Color bgColor, Color fgColor, int x, int y, int width, int height) {
        JButton button = new JButton(text);
        button.setFont(font);
        button.setBackground(bgColor);
        button.setForeground(fgColor);
        button.setBounds(x, y, width, height);
        return button;
    }

    private void setupSystemTray() {
        systemTrayManager = new SystemTrayManager(frame);
    }

    private void setupTimerLogic() {
        alertManager = new AlertManager(frame);
        timerLogic = new TimerLogic(hoursSpinner, minutesSpinner, secondsSpinner,
                progressBar, alertManager, frame);
    }

    private void setupButtonActions() {
        startButton.addActionListener(e -> timerLogic.startTimer());
        pauseButton.addActionListener(e -> timerLogic.pauseTimer());
        resetButton.addActionListener(e -> timerLogic.resetTimer());
    }

    private void setupContextMenu() {
        // In your showContextMenu method, after setting shouldShutdownOnComplete
        timerLogic.setShouldShutdownOnComplete(shouldShutdownOnComplete);
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
}