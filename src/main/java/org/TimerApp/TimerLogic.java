package org.TimerApp;

import javax.swing.*;
import javax.swing.Timer;
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

    public TimerLogic(JSpinner hoursSpinner, JSpinner minutesSpinner, JSpinner secondsSpinner, JProgressBar progressBar, AlertManager alertManager) {
        this.hoursSpinner = hoursSpinner;
        this.minutesSpinner = minutesSpinner;
        this.secondsSpinner = secondsSpinner;
        this.progressBar = progressBar;
        this.alertManager = alertManager;
        setupTimer();
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
                }
                hoursSpinner.setValue(hours);
                minutesSpinner.setValue(minutes);
                secondsSpinner.setValue(seconds);
                updateProgressBar();
            }
        });
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