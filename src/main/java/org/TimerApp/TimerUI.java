package org.TimerApp;

import com.formdev.flatlaf.FlatLightLaf;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class TimerUI {
    private JFrame frame;
    private JButton startButton, pauseButton, resetButton;
    private JProgressBar progressBar;
    private JSpinner hoursSpinner, minutesSpinner, secondsSpinner;
    private TimerLogic timerLogic;
    private SystemTrayManager systemTrayManager;
    private AlertManager alertManager;

    public TimerUI() {
        FlatLightLaf.setup(); // Initialize FlatLaf
        initializeFrame();
        initializeComponents();
        setupSystemTray();
        setupTimerLogic();
        setupButtonActions();
        addComponentsToFrame();
        frame.setVisible(true);
    }

    private void initializeFrame() {
        frame = new JFrame("Timer App");
        frame.setUndecorated(true);
        frame.setSize(256, 150);
        frame.setLayout(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setBackground(new Color(0, 0, 0, 0));
        frame.setContentPane(new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(0, 0, 0, 50));
                g2.fillRoundRect(6, 6, getWidth() - 12, getHeight() - 12, 30, 30);
                g2.setColor(Color.WHITE);
                g2.fillRoundRect(6, 6, getWidth() - 10, getHeight() - 10, 15, 15);
                g2.dispose();
            }
        });
    }

    private void initializeComponents() {
        Font inputFont = new Font("Roboto Mono", Font.BOLD, 23);
        Font buttonFont = new Font("Roboto Mono", Font.BOLD, 10);
        Font progressBarFont = new Font("Roboto Mono", Font.BOLD, 9);

        hoursSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 99, 1));
        minutesSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));
        secondsSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 59, 1));

        configureSpinner(hoursSpinner, inputFont, 12, 23, 75, 35);
        configureSpinner(minutesSpinner, inputFont, 90, 23, 75, 35);
        configureSpinner(secondsSpinner, inputFont, 168, 23, 75, 35);

        startButton = createButton("START", buttonFont, new Color(0, 200, 0), Color.WHITE, 15, 69, 69, 23);
        pauseButton = createButton("PAUSE", buttonFont, Color.LIGHT_GRAY, Color.BLACK, 93, 69, 69, 23);
        resetButton = createButton("RESET", buttonFont, new Color(200, 0, 0), Color.WHITE, 170, 69, 69, 23);

        progressBar = new JProgressBar();
        progressBar.setBounds(12, 104, 230, 23);
        progressBar.setFont(progressBarFont);
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(0, 120, 255));
    }

    private void configureSpinner(JSpinner spinner, Font font, int x, int y, int width, int height) {
        ((JSpinner.DefaultEditor) spinner.getEditor()).getTextField().setEditable(false);
        spinner.setFont(font);
        spinner.setBounds(x, y, width, height);
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
        timerLogic = new TimerLogic(hoursSpinner, minutesSpinner, secondsSpinner, progressBar, alertManager);
    }

    private void setupButtonActions() {
        startButton.addActionListener(e -> timerLogic.startTimer());
        pauseButton.addActionListener(e -> timerLogic.pauseTimer());
        resetButton.addActionListener(e -> timerLogic.resetTimer());
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