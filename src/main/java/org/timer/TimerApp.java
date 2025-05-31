package org.timer;

import javax.swing.*;

public class TimerApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(TimerUI::new);
    }
}